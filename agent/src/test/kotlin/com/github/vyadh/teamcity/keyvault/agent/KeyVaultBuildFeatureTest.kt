package com.github.vyadh.teamcity.keyvault.agent

import com.github.vyadh.teamcity.keyvault.common.KeyVaultConstants
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.PasswordReplacer
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify

internal class KeyVaultBuildFeatureTest {

  @Test
  internal fun alwaysActivatePlugin() {
    @Suppress("UNCHECKED_CAST")
    val dispatcher = Mockito.mock(EventDispatcher::class.java) as EventDispatcher<AgentLifeCycleListener>

    val feature = KeyVaultBuildFeature(dispatcher)

    verify(dispatcher).addListener(feature)
  }

  @Test
  internal fun startingBuildBlanksOutToken() {
    val feature = buildFeature()
    val build = buildWithToken("token")

    feature.buildStarted(build)

    verify(build).addSharedConfigParameter(
          KeyVaultConstants.ACCESS_TOKEN_PROPERTY, "(obfuscated)")
  }

  @Test
  internal fun tokenShouldBeObfuscated() {
    val feature = buildFeature()
    val build = buildWithToken("my-access-token")
    val passwordReplacer = Mockito.mock(PasswordReplacer::class.java)
    `when`(build.passwordReplacer).thenReturn(passwordReplacer)

    feature.buildStarted(build)

    verify(build.passwordReplacer).addPassword("my-access-token")
  }

  @Suppress("UNCHECKED_CAST")
  private fun buildFeature(): KeyVaultBuildFeature {
    val dispatcher = Mockito.mock(EventDispatcher::class.java) as EventDispatcher<AgentLifeCycleListener>
    return KeyVaultBuildFeature(dispatcher)
  }

  private fun buildWithToken(token: String): AgentRunningBuild {
    val build = Mockito.mock(AgentRunningBuild::class.java)

    `when`(build.sharedConfigParameters).thenReturn(
          mapOf(KeyVaultConstants.ACCESS_TOKEN_PROPERTY to token))

    val passwordReplacer = Mockito.mock(PasswordReplacer::class.java)
    `when`(build.passwordReplacer).thenReturn(passwordReplacer)

    return build
  }

}
