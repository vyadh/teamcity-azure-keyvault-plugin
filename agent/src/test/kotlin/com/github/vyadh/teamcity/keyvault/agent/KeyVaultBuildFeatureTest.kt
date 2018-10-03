package com.github.vyadh.teamcity.keyvault.agent

import com.github.vyadh.teamcity.keyvault.common.KeyVaultConstants
import com.github.vyadh.teamcity.keyvault.common.KeyVaultRef
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildParametersMap
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.PasswordReplacer
import org.assertj.core.api.Assertions.assertThat
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

  @Test
  internal fun allReferencesPresent() {
    val build = Mockito.mock(AgentRunningBuild::class.java)
    `when`(build.sharedConfigParameters)
          .thenReturn(mapOf("a" to "%keyvault:myvault1/keyname%"))

    val paramMap = Mockito.mock(BuildParametersMap::class.java)
    `when`(paramMap.allParameters)
          .thenReturn(mapOf("b" to "%keyvault:myvault2/keyname%"))
    `when`(build.sharedBuildParameters).thenReturn(paramMap)

    val refs = buildFeature().allReferences(build)

    assertThat(refs).containsOnly(
          KeyVaultRef("keyvault:myvault1/keyname"),
          KeyVaultRef("keyvault:myvault2/keyname")
    )
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
