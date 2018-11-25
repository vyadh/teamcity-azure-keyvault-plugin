package com.github.vyadh.teamcity.keyvault.agent

import com.github.vyadh.teamcity.keyvault.common.KeyVaultConstants
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildParametersMap
import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.PasswordReplacer
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KeyVaultAgentIntegrationTest {

  private val server = MockWebServer()

  @AfterEach
  internal fun shutdownServer() {
    server.shutdown()
  }

  @BeforeEach
  internal fun startServer() {
    server.start()
  }

  @Test
  internal fun secretsAreAddedAsBuildParameters() {
    val connector = AzureKeyVaultConnector(baseUrl())

    val buildRef = mapOf("usage-a" to " %keyvault:vault1/keyA% ")
    val configRef = mapOf("usage-b" to " %keyvault:vault2/keyB% ")
    val build = build(buildParams = buildRef, configParams = configRef)
    val feature = buildFeature(connector)

    server.enqueue(MockResponse().setBody(secretResponse("vault1", "keyA", "secretA")))
    server.enqueue(MockResponse().setBody(secretResponse("vault2", "keyB", "secretB")))
    feature.buildStarted(build)

    verify(build).addSharedConfigParameter("teamcity.azurekeyvault.access_token", "********")
    verify(build).addSharedConfigParameter("keyvault:vault1/keyA", "secretA")
    verify(build).addSharedConfigParameter("keyvault:vault2/keyB", "secretB")
  }

  private fun baseUrl() = server.url("/$(instance)").toString()

  private fun build(
        configParams: Map<String, String> = emptyMap(),
        buildParams: Map<String, String> = emptyMap()
  ): AgentRunningBuild {

    val configAndTokenParams = configParams
          .plus(KeyVaultConstants.ACCESS_TOKEN_PROPERTY to "access-token")

    val paramMap: BuildParametersMap = mock {
      on { allParameters }.doReturn(buildParams)
    }

    val passwordReplacer: PasswordReplacer = mock()
    val buildLogger: BuildProgressLogger = mock()

    // Unfortunately there doesn't seem to be a TeamCity agent build implementation class we can use
    return mock {
      on { it.sharedBuildParameters }.doReturn(paramMap)
      on { it.sharedConfigParameters }.doReturn<Map<String, String>>(configAndTokenParams)
      on { it.passwordReplacer }.doReturn(passwordReplacer)
      on { it.buildLogger }.doReturn(buildLogger)
    }
  }

  private fun buildFeature(connector: KeyVaultConnector): KeyVaultBuildFeature {
    val dispatcher: EventDispatcher<AgentLifeCycleListener> = mock()
    return KeyVaultBuildFeature(dispatcher, connector)
  }

  private fun secretResponse(vault: String, name: String, value: String): String {
    return """
      {
          "value": "$value",
          "id": "https://$vault.vault.azure.net/secrets/$name/<unique-id>",
          "attributes": {
              "enabled": true,
              "created": 1537045402,
              "updated": 1537045402,
              "recoveryLevel": "Purgeable"
          }
      }
    """.trimIndent()
  }

}
