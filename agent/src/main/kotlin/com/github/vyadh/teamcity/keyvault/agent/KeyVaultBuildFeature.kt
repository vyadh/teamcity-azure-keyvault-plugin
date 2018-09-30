package com.github.vyadh.teamcity.keyvault.agent

import com.github.vyadh.teamcity.keyvault.common.KeyVaultConstants
import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.AgentLifeCycleAdapter
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.log.Loggers
import jetbrains.buildServer.util.EventDispatcher

class KeyVaultBuildFeature(
      dispatcher: EventDispatcher<AgentLifeCycleListener>) : AgentLifeCycleAdapter() {

  companion object {
    val LOG = Logger.getInstance(
          Loggers.AGENT_CATEGORY + "." + KeyVaultBuildFeature::class.java.name)!!
  }

  init {
    dispatcher.addListener(this)
    LOG.info("Azure Key Vault integration enabled")
  }

  override fun buildStarted(build: AgentRunningBuild) {
    val token = build.sharedConfigParameters[KeyVaultConstants.ACCESS_TOKEN_PROPERTY]
    if (token == null || token.isNullOrBlank()) {
      return
    }

    // Hide token from shown properties and build logs
    build.passwordReplacer.addPassword(token)

    // Do not allow using the token directly (for now)
    build.addSharedConfigParameter(KeyVaultConstants.ACCESS_TOKEN_PROPERTY, "(obfuscated)")
  }

}
