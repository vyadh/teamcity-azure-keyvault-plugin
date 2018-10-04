package com.github.vyadh.teamcity.keyvault.agent

import com.github.vyadh.teamcity.keyvault.agent.KotlinMockitoMatchers.any
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
import org.mockito.Mockito.*

internal class KeyVaultBuildFeatureTest {

  private val accessToken = "secret-value-123"

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
    val build = build()

    feature.buildStarted(build)

    verify(build).addSharedConfigParameter(
          KeyVaultConstants.ACCESS_TOKEN_PROPERTY, "(redacted)")
  }

  @Test
  internal fun tokenShouldBeObfuscated() {
    val feature = buildFeature()
    val build = build()
    val passwordReplacer = Mockito.mock(PasswordReplacer::class.java)
    `when`(build.passwordReplacer).thenReturn(passwordReplacer)

    feature.buildStarted(build)

    verify(build.passwordReplacer).addPassword(accessToken)
  }

  @Test
  internal fun allReferencesPresent() {
    val build = build(
          mapOf(
                "config1" to "%keyvault:myvault1/keyname1%",
                "config2" to "%keyvault:myvault2/keyname1%"
          ),
          mapOf(
                "build1" to "%keyvault:myvault1/keyname2%",
                "build2" to "%keyvault:myvault2/keyname2%"
          )
    )

    val refs = buildFeature().allReferences(build)

    assertThat(refs).containsOnly(
          KeyVaultRef("keyvault:myvault1/keyname1"),
          KeyVaultRef("keyvault:myvault1/keyname2"),
          KeyVaultRef("keyvault:myvault2/keyname1"),
          KeyVaultRef("keyvault:myvault2/keyname2")
    )
  }

  @Test
  internal fun secretsAreFetchedFromConnector() {
    val connector = connector()
    val configRef = KeyVaultRef("keyvault:myvault1/keyname1")
    val buildRef = KeyVaultRef("keyvault:myvault2/keyname2")
    val build = build(
          mapOf("config" to "%${configRef.ref}%"),
          mapOf("build" to "%${buildRef.ref}%"))

    buildFeature(connector).buildStarted(build)

    verify(connector).requestValue(configRef, accessToken)
    verify(connector).requestValue(buildRef, accessToken)
  }

  @Suppress("UNCHECKED_CAST")
  private fun buildFeature(connector: KeyVaultConnector = connector())
        : KeyVaultBuildFeature {
    val dispatcher = Mockito.mock(EventDispatcher::class.java) as EventDispatcher<AgentLifeCycleListener>
    return KeyVaultBuildFeature(dispatcher, connector)
  }

  private fun build(
        configParams: Map<String, String> = emptyMap(),
        buildParams: Map<String, String> = emptyMap()
  ): AgentRunningBuild {

    val configAndTokenParams = configParams
          .plus(KeyVaultConstants.ACCESS_TOKEN_PROPERTY to accessToken)

    val paramMap = Mockito.mock(BuildParametersMap::class.java)
    `when`(paramMap.allParameters).thenReturn(buildParams)

    val build = Mockito.mock(AgentRunningBuild::class.java)
    `when`(build.sharedBuildParameters).thenReturn(paramMap)
    `when`(build.sharedConfigParameters).thenReturn(configAndTokenParams)

    val passwordReplacer = Mockito.mock(PasswordReplacer::class.java)
    `when`(build.passwordReplacer).thenReturn(passwordReplacer)

    return build
  }

  private fun connector(): KeyVaultConnector {
    val connector = Mockito.mock(KeyVaultConnector::class.java)
    `when`(connector.requestValue(any(), any()))
          .thenReturn(SecretResponse(""))
    return connector
  }

}
