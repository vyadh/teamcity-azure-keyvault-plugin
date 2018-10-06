package com.github.vyadh.teamcity.keyvault.agent

import com.github.vyadh.teamcity.keyvault.common.KeyVaultException
import com.github.vyadh.teamcity.keyvault.common.KeyVaultRef
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AzureKeyVaultConnectorTest {

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
  internal fun submittedRequest() {
    val connector = AzureKeyVaultConnector(baseUrl())
    val ref = KeyVaultRef("keyvault:instance/name")
    val accessToken = "access-token"

    server.enqueue(MockResponse().setBody(secretResponse))
    connector.requestValue(ref, accessToken)

    val request = server.takeRequest()
    assertThat(request.method).isEqualTo("GET")
    assertThat(request.path).isEqualTo("/${ref.instance}/secrets/${ref.name}?api-version=2016-10-01")
    assertThat(request.headers["Authorization"]).isEqualTo("Bearer $accessToken")
    assertThat(request.headers["Accept"]).isEqualTo("application/json")
  }

  @Test
  internal fun submittedRequestWithErrorResponse() {
    val connector = AzureKeyVaultConnector(baseUrl())
    val ref = KeyVaultRef("keyvault:instance/name")
    val accessToken = "access-token"

    server.enqueue(MockResponse().setResponseCode(404))
    val throwable = catchThrowable { connector.requestValue(ref, accessToken) }

    assertThat(throwable)
          .isInstanceOf(KeyVaultException::class.java)
          .hasMessageContaining("Could not fetch secret, received response code 404")
  }

  @Test
  internal fun extractedResponse() {
    val connector = AzureKeyVaultConnector(baseUrl())
    val ref = KeyVaultRef("keyvault:instance/name")
    val accessToken = "token"

    server.enqueue(MockResponse().setBody(secretResponse))
    val response = connector.requestValue(ref, accessToken)

    assertThat(response.value).isEqualTo(secretValue)
  }

  private fun baseUrl() = server.url("/$(instance)").toString()

  private val secretValue =
        "ask not for the answer, ask for the question"

  private val secretResponse = """
        {
            "value": "$secretValue",
            "id": "https://tckeyvault.vault.azure.net/secrets/auth-other-sample-secret/fbf689f9ad6f4e7e985ce9404990bb4b",
            "attributes": {
                "enabled": true,
                "created": 1537045402,
                "updated": 1537045402,
                "recoveryLevel": "Purgeable"
            }
        }
      """.trimIndent()

}
