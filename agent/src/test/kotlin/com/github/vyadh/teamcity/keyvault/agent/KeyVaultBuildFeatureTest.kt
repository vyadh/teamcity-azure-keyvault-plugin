package com.github.vyadh.teamcity.keyvault.agent

import com.github.vyadh.teamcity.keyvault.common.KeyVaultConstants
import com.github.vyadh.teamcity.keyvault.common.KeyVaultException
import com.github.vyadh.teamcity.keyvault.common.KeyVaultRef
import com.nhaarman.mockitokotlin2.*
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.PasswordReplacer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class KeyVaultBuildFeatureTest {

  private val accessToken = "access-token-123"

  @Test
  internal fun alwaysActivatePlugin() {
    val dispatcher: EventDispatcher<AgentLifeCycleListener> = mock()

    val feature = KeyVaultBuildFeature(dispatcher, connector())

    verify(dispatcher).addListener(feature)
  }

  @Test
  internal fun startingBuildBlanksOutToken() {
    val feature = buildFeature()
    val build = build()

    feature.buildStarted(build)

    verify(build).addSharedConfigParameter(
          KeyVaultConstants.ACCESS_TOKEN_PROPERTY, "********")
  }

  @Test
  internal fun tokenShouldBeObfuscated() {
    val feature = buildFeature()
    val passwordReplacer: PasswordReplacer = mock()
    val build = build()
    whenever(build.passwordReplacer).thenReturn(passwordReplacer)

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
    val passwords: PasswordReplacer = mock()
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
    val connector: KeyVaultConnector = mock()
    whenever(connector.requestValue(any(), any()))
          .thenThrow(KeyVaultException("Something went wrong"))
//    val connector: KeyVaultConnector = mock {
//      on { it.requestValue(any(), any()) }.doThrow(KeyVaultException("Something went wrong"))
//    }
    val build = build(buildParams = mapOf("param-name" to " %keyvault:vault/secret-name% "))

    buildFeature(connector).buildStarted(build)

    verify(build.buildLogger).internalError(
          eq(KeyVaultConstants.FEATURE_TYPE),
          eq("Error processing parameters for Azure Key Vault: Something went wrong"),
          any()
    )
  }

  private fun buildFeature(connector: KeyVaultConnector = connector()): KeyVaultBuildFeature {
    val dispatcher: EventDispatcher<AgentLifeCycleListener> = mock() 
    return KeyVaultBuildFeature(dispatcher, connector)
  }

  private fun build(
        configParams: Map<String, String> = emptyMap(),
        buildParams: Map<String, String> = emptyMap(),
        passwordReplacer: PasswordReplacer = mock()
  ): AgentRunningBuild {

    val configAndTokenParams = configParams
          .plus(KeyVaultConstants.ACCESS_TOKEN_PROPERTY to accessToken)

    val paramMap: BuildParametersMap = mock {
      on { allParameters }.doReturn(buildParams)
    }

    val buildLogger: BuildProgressLogger = mock()

    return mock {
      on { it.sharedBuildParameters }.doReturn(paramMap)
      on { it.sharedConfigParameters }.doReturn(configAndTokenParams)
      on { it.passwordReplacer }.doReturn(passwordReplacer)
      on { it.buildLogger }.doReturn(buildLogger)
    }
  }

  private fun connector(): KeyVaultConnector {
    return mock {
      on { requestValue(any(), any()) }.doReturn(SecretResponse(""))
    }
  }

  private fun connector(vararg pairs: Pair<String, String>): KeyVaultConnector {
    val connector: KeyVaultConnector = mock()
    for (pair in pairs) {
      val (ref, secret) = pair
      whenever(connector.requestValue(KeyVaultRef(ref), accessToken))
            .thenReturn(SecretResponse(secret))
    }
    return connector
  }

}
