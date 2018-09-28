package com.github.vyadh.teamcity.keyvault.server

import com.github.vyadh.teamcity.keyvault.common.KeyVaultConstants
import com.github.vyadh.teamcity.keyvault.common.KeyVaultFeatureSettings
import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.log.Loggers
import jetbrains.buildServer.serverSide.BuildStartContext
import jetbrains.buildServer.serverSide.BuildStartContextProcessor
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor
import jetbrains.buildServer.serverSide.SRunningBuild
import jetbrains.buildServer.serverSide.oauth.OAuthConstants

class AzureBuildStartContextProcessor(
      private val connector: AzureConnector) : BuildStartContextProcessor  {

  companion object {
    val LOG = Logger.getInstance(
          Loggers.SERVER_CATEGORY + "." + AzureBuildStartContextProcessor::class.java.name)!!
  }

  override fun updateParameters(context: BuildStartContext) {
    val feature = findKeyVaultFeature(context)
    if (feature != null && requiresAccessToken(context.build)) {
      val settings = KeyVaultFeatureSettings.fromMap(feature.parameters)
      val token = connector.requestToken(settings)
    } else {
      LOG.debug("No key vault token required for build ${context.build}")
    }
  }

  private fun findKeyVaultFeature(context: BuildStartContext): SProjectFeatureDescriptor? {
    val project = context.build.buildType?.project ?: return null
    return project
          .getAvailableFeaturesOfType(OAuthConstants.FEATURE_TYPE)
          .firstOrNull { isKeyVaultType(it) }
  }

  private fun isKeyVaultType(it: SProjectFeatureDescriptor) =
        it.parameters[OAuthConstants.OAUTH_TYPE_PARAM] == KeyVaultConstants.FEATURE_TYPE

  private fun requiresAccessToken(build: SRunningBuild): Boolean {
    //todo
    return true
  }

}
