package com.github.vyadh.teamcity.keyvault.agent

import com.github.vyadh.teamcity.keyvault.common.KeyVaultConstants
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
import org.mockito.Mockito.*

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

    verify(build).addSharedConfigParameter("teamcity.keyvault.access_token", "*** (redacted)")
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

    val paramMap = mock(BuildParametersMap::class.java)
    `when`(paramMap.allParameters).thenReturn(buildParams)

    // Unfortunately there doesn't seem to be one of these we can use
    val build = mock(AgentRunningBuild::class.java)
    `when`(build.sharedBuildParameters).thenReturn(paramMap)
    `when`(build.sharedConfigParameters).thenReturn(configAndTokenParams)

    val passwordReplacer = mock(PasswordReplacer::class.java)
    `when`(build.passwordReplacer).thenReturn(passwordReplacer)

    val buildLogger = mock(BuildProgressLogger::class.java)
    `when`(build.buildLogger).thenReturn(buildLogger)

    return build
  }

  @Suppress("UNCHECKED_CAST")
  private fun buildFeature(connector: KeyVaultConnector): KeyVaultBuildFeature {
    val dispatcher = mock(EventDispatcher::class.java) as EventDispatcher<AgentLifeCycleListener>
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
