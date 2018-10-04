package com.github.vyadh.teamcity.keyvault.server

import com.github.vyadh.teamcity.keyvault.common.TokenRequestSettings
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AzureTokenConnectorTest {

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
    val connector = AzureTokenConnector(baseUrl())

    server.enqueue(MockResponse().setBody(tokenResponse))
    connector.requestToken(settings)

    val request = server.takeRequest()
    assertThat(request.method).isEqualTo("POST")
    assertThat(request.path).isEqualTo("/${settings.tenantId}/oauth2/token")
    assertThat(request.headers["Accept"]).isEqualTo("application/json")
    assertThat(request.headers["Content-Type"]).isEqualTo("application/x-www-form-urlencoded")
    assertThat(request.body.readUtf8()).isEqualTo(expectedFormData)
  }

  @Test
  internal fun extractedResponse() {
    val connector = AzureTokenConnector(baseUrl())

    server.enqueue(MockResponse().setBody(tokenResponse))
    val response = connector.requestToken(settings)

    assertThat(response.accessToken).isEqualTo(accessToken)
  }

  //todo test possible error responses

  private fun baseUrl() = server.url("/").toString()

  private val settings = TokenRequestSettings(
        "00000001-0001-0001-0001-000000000001",
        "00000002-0002-0002-0002-000000000002",
        "aaaabbbbccccddddeeeeffffgggghhhhiiiijjjj",
        "https://vault.azure.net"
  )

  private val expectedFormData =
        "grant_type=client_credentials&" +
        "client_id=00000002-0002-0002-0002-000000000002&" +
        "client_secret=aaaabbbbccccddddeeeeffffgggghhhhiiiijjjj&" +
        "resource=https%3A%2F%2Fvault.azure.net"

  private val accessToken =
        "long-string-".padEnd(1000, 'x')

  private val tokenResponse = """
        {
          "token_type": "Bearer",
          "access_token": "$accessToken"
        }
      """.trimIndent()

}
