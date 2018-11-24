package com.github.vyadh.teamcity.keyvault.agent

import com.github.vyadh.teamcity.keyvault.common.KeyVaultConstants
import com.github.vyadh.teamcity.keyvault.common.KeyVaultRef
import com.github.vyadh.teamcity.keyvault.common.KeyVaultRefs
import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.AgentLifeCycleAdapter
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.log.Loggers
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.StringUtil
import java.util.stream.Collectors
import java.util.stream.Stream

class KeyVaultBuildFeature(
      dispatcher: EventDispatcher<AgentLifeCycleListener>,
      private val connector: KeyVaultConnector)
  : AgentLifeCycleAdapter() {

  companion object {
    val LOG = Logger.getInstance(
          Loggers.AGENT_CATEGORY + "." + KeyVaultBuildFeature::class.java.name)!!
  }

  init {
    dispatcher.addListener(this)
    LOG.info("Azure Key Vault integration enabled")
  }

  override fun buildStarted(build: AgentRunningBuild) {
    try {
      val token = consumeToken(build)
      val refs = allReferences(build)
      val secretByRef = fetchSecrets(refs, token)
      obfuscatePasswords(secretByRef, build)
      populateBuildSecrets(secretByRef, build)
    } catch (e: Exception) {
      build.buildLogger.internalError(
            KeyVaultConstants.FEATURE_TYPE,
            "Error processing parameters for Azure Key Vault: ${e.message}",
            e
      )
      BuildProblemData.createBuildProblem(
            "KVA_${build.buildTypeId}",
            "KeyVaultBuildFeature",
            "Error processing parameters for Azure Key Vault: ${e.message}," +
                  " see teamcity-server.log for details"
      )
    }
  }

  private fun consumeToken(build: AgentRunningBuild): String? {
    val token = build.sharedConfigParameters[KeyVaultConstants.ACCESS_TOKEN_PROPERTY]
    if (token == null || token.isNullOrBlank()) {
      LOG.debug("No token available/required for Azure Key Vault")
      return null
    }
    build.buildLogger.message("Retrieved access token for Azure Key Vault")

    // Hide token from shown properties and build logs
    build.passwordReplacer.addPassword(token)

    // Do not allow using the token directly (for now)
    build.addSharedConfigParameter(KeyVaultConstants.ACCESS_TOKEN_PROPERTY, "********")

    return token
  }

  internal fun allReferences(build: AgentRunningBuild): Stream<KeyVaultRef> {
    val paramValues = Stream.concat(
          build.sharedConfigParameters.values.stream(),
          build.sharedBuildParameters.allParameters.values.stream()
    )

    return KeyVaultRefs.searchRefs(paramValues)
  }

  private fun fetchSecrets(refs: Stream<KeyVaultRef>, token: String?): Map<KeyVaultRef, String> {
    if (token == null) {
      LOG.warn("Could not fetch Azure AD access token")
      return emptyMap()
    }

    return refs.collect(Collectors.toMap(
          { it },
          { connector.requestValue(it, token).value }
    ))
  }

  private fun obfuscatePasswords(secretByRef: Map<KeyVaultRef, String>, build: AgentRunningBuild) {
    secretByRef.values.forEach { build.passwordReplacer.addPassword(it) }
  }

  private fun populateBuildSecrets(secretsByRef: Map<KeyVaultRef, String>, build: AgentRunningBuild) {
    val items = StringUtil.pluralize("secret", secretsByRef.size)
    build.buildLogger.message("Retrieved ${secretsByRef.size} $items from Azure Key Vault")

    for ((ref, secret) in secretsByRef) {
      build.addSharedConfigParameter(ref.ref, secret)
    }
  }

}
