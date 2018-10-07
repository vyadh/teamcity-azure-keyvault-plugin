package com.github.vyadh.teamcity.keyvault.agent

import com.github.vyadh.teamcity.keyvault.agent.KotlinMockitoMatchers.any
import com.github.vyadh.teamcity.keyvault.common.KeyVaultConstants
import com.github.vyadh.teamcity.keyvault.common.KeyVaultException
import com.github.vyadh.teamcity.keyvault.common.KeyVaultRef
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.PasswordReplacer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.*

internal class KeyVaultBuildFeatureTest {

  private val accessToken = "access-token-123"

  @Test
  internal fun alwaysActivatePlugin() {
    @Suppress("UNCHECKED_CAST")
    val dispatcher = Mockito.mock(EventDispatcher::class.java) as EventDispatcher<AgentLifeCycleListener>

    val feature = KeyVaultBuildFeature(dispatcher, connector())

    verify(dispatcher).addListener(feature)
  }

  @Test
  internal fun startingBuildBlanksOutToken() {
    val feature = buildFeature()
    val build = build()

    feature.buildStarted(build)

    verify(build).addSharedConfigParameter(
          KeyVaultConstants.ACCESS_TOKEN_PROPERTY, "*** (redacted)")
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

  @Test
  internal fun secretsAreAddedAsPasswordsToObfuscate() {
    val connector = connector(
          "keyvault:vaultA/keyA1" to "secretA1",
          "keyvault:vaultB/keyB1" to "secretB1",
          "keyvault:vaultB/keyB2" to "secretB2"
    )
    val configRef = mapOf(
          "usage-a" to " %keyvault:vaultA/keyA1% ",
          "usage-b" to " %keyvault:vaultB/keyB1% ",
          "usage-c" to " %keyvault:vaultB/keyB2% "
    )
    val passwords = Mockito.mock(PasswordReplacer::class.java)
    val build = build(buildParams = configRef, passwordReplacer = passwords)

    buildFeature(connector).buildStarted(build)

    verify(passwords).addPassword("secretA1")
    verify(passwords).addPassword("secretB1")
    verify(passwords).addPassword("secretB2")
  }

  @Test
  internal fun secretsAreAddedAsBuildParameters() {
    val connector = connector(
          "keyvault:vault/keyA" to "secretA",
          "keyvault:vault/keyB" to "secretB",
          "keyvault:vault/keyC" to "secretC"
    )
    val configRef = mapOf(
          "usage-a" to " %keyvault:vault/keyA% ",
          "usage-b" to " %keyvault:vault/keyB% ",
          "usage-c" to " %keyvault:vault/keyC% "
    )
    val build = build(buildParams = configRef)

    buildFeature(connector).buildStarted(build)

    verify(build).addSharedConfigParameter("keyvault:vault/keyA", "secretA")
    verify(build).addSharedConfigParameter("keyvault:vault/keyB", "secretB")
    verify(build).addSharedConfigParameter("keyvault:vault/keyC", "secretC")
  }

  @Test
  internal fun reportBuildErrorWhenFailedToFetchSecret() {
    val connector = Mockito.mock(KeyVaultConnector::class.java)
    `when`(connector.requestValue(any(), any()))
          .thenThrow(KeyVaultException("Something went wrong"))
    val build = build(buildParams = mapOf("param-name" to " %keyvault:vault/secret-name% "))

    buildFeature(connector).buildStarted(build)

    verify(build.buildLogger).internalError(
          eq(KeyVaultConstants.FEATURE_TYPE),
          eq("Error processing parameters for Azure Key Vault: Something went wrong"),
          any()
    )
  }

  @Suppress("UNCHECKED_CAST")
  private fun buildFeature(connector: KeyVaultConnector = connector())
        : KeyVaultBuildFeature {
    val dispatcher = Mockito.mock(EventDispatcher::class.java) as EventDispatcher<AgentLifeCycleListener>
    return KeyVaultBuildFeature(dispatcher, connector)
  }

  private fun build(
        configParams: Map<String, String> = emptyMap(),
        buildParams: Map<String, String> = emptyMap(),
        passwordReplacer: PasswordReplacer = Mockito.mock(PasswordReplacer::class.java)
  ): AgentRunningBuild {

    val configAndTokenParams = configParams
          .plus(KeyVaultConstants.ACCESS_TOKEN_PROPERTY to accessToken)

    val paramMap = Mockito.mock(BuildParametersMap::class.java)
    `when`(paramMap.allParameters).thenReturn(buildParams)

    val build = Mockito.mock(AgentRunningBuild::class.java)
    `when`(build.sharedBuildParameters).thenReturn(paramMap)
    `when`(build.sharedConfigParameters).thenReturn(configAndTokenParams)

    `when`(build.passwordReplacer).thenReturn(passwordReplacer)

    val buildLogger = Mockito.mock(BuildProgressLogger::class.java)
    `when`(build.buildLogger).thenReturn(buildLogger)

    return build
  }

  private fun connector(): KeyVaultConnector {
    val connector = Mockito.mock(KeyVaultConnector::class.java)
    `when`(connector.requestValue(any(), any()))
          .thenReturn(SecretResponse(""))
    return connector
  }

  private fun connector(vararg pairs: Pair<String, String>): KeyVaultConnector {
    val connector = Mockito.mock(KeyVaultConnector::class.java)
    for (pair in pairs) {
      val (ref, secret) = pair
      `when`(connector.requestValue(KeyVaultRef(ref), accessToken))
            .thenReturn(SecretResponse(secret))
    }
    return connector
  }

}
