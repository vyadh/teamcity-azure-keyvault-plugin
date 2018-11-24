package com.github.vyadh.teamcity.keyvault.server

import com.github.vyadh.teamcity.keyvault.common.KeyVaultConstants
import com.github.vyadh.teamcity.keyvault.common.TokenRequestSettings
import com.github.vyadh.teamcity.keyvault.common.KeyVaultRefs
import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.log.Loggers
import jetbrains.buildServer.serverSide.BuildStartContext
import jetbrains.buildServer.serverSide.BuildStartContextProcessor
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor
import jetbrains.buildServer.serverSide.SRunningBuild
import jetbrains.buildServer.serverSide.oauth.OAuthConstants

class AzureBuildStartContextProcessor(
      private val connector: TokenConnector = AzureTokenConnector()
) : BuildStartContextProcessor  {

  companion object {
    val LOG = Logger.getInstance(
          Loggers.SERVER_CATEGORY + "." + AzureBuildStartContextProcessor::class.java.name)!!
  }

  init {
    LOG.info("Azure Key Vault integration enabled for fetching access tokens")
  }

  override fun updateParameters(context: BuildStartContext) {
    val feature = findKeyVaultFeature(context)
    if (feature != null) {
      if (requiresAccessToken(feature, context.build)) {
        updateParametersWithToken(feature, context)
      } else {
        LOG.debug("No key vault variables found for for build ${context.build}")
      }
    } else {
      LOG.debug("No key vault feature enabled for build ${context.build}")
    }
  }

  private fun findKeyVaultFeature(context: BuildStartContext): SProjectFeatureDescriptor? {
    val project = context.build.buildType?.project

    return project
          ?.getAvailableFeaturesOfType(OAuthConstants.FEATURE_TYPE)
          ?.firstOrNull { isKeyVaultType(it) }
  }

  private fun isKeyVaultType(it: SProjectFeatureDescriptor) =
        it.parameters[OAuthConstants.OAUTH_TYPE_PARAM] == KeyVaultConstants.FEATURE_TYPE

  private fun requiresAccessToken(
        feature: SProjectFeatureDescriptor, build: SRunningBuild): Boolean {

    return containsProvideTokenParam(feature) ||
          KeyVaultRefs.containsRef(build.parametersProvider.all)
  }

  private fun containsProvideTokenParam(feature: SProjectFeatureDescriptor): Boolean {
    val override = feature.parameters[KeyVaultConstants.PROVIDE_TOKEN_PROPERTY]
    return override?.toBoolean() ?: false
  }

  private fun updateParametersWithToken(
        feature: SProjectFeatureDescriptor, context: BuildStartContext) {

    val settings = TokenRequestSettings.fromMap(feature.parameters)

    try {
      LOG.debug("Fetch Azure AD token for Key Vault")
      val token = connector.requestToken(settings)

      context.addSharedParameter(
            KeyVaultConstants.ACCESS_TOKEN_PROPERTY,
            token.accessToken)
    } catch (e: Throwable) {
      reportError("Error fetching Azure access token: ${e.message}", e, context.build)
    }
  }

  private fun reportError(message: String, e: Throwable, build: SRunningBuild) {
    LOG.warn(message, e)
    build.addBuildProblem(
          BuildProblemData.createBuildProblem(
                "KVS_${build.buildTypeId}",
                "AzureBuildStartContextProcessor",
                message + ": " + e.toString() + ", see teamcity-server.log for details"
          ))
  }

}
