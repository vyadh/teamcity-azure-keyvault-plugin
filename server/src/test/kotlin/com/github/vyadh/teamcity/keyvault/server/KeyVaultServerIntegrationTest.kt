package com.github.vyadh.teamcity.keyvault.server

import com.github.vyadh.teamcity.keyvault.common.KeyVaultConstants
import com.github.vyadh.teamcity.keyvault.common.TokenRequestSettings
import com.github.vyadh.teamcity.keyvault.server.BuildContexts.buildContextWith
import jetbrains.buildServer.parameters.impl.MapParametersProviderImpl
import jetbrains.buildServer.serverSide.*
import jetbrains.buildServer.serverSide.impl.ProjectFeatureDescriptorImpl
import jetbrains.buildServer.serverSide.oauth.OAuthConstants
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KeyVaultServerIntegrationTest {

  private val server = MockWebServer()

  @AfterEach
  internal fun shutdownServer() {
    server.shutdown()
  }

  @BeforeEach
  internal fun startServer() {
    server.start()
  }

  private val settings = TokenRequestSettings(
        "00000001-0001-0001-0001-000000000001",
        "00000002-0002-0002-0002-000000000002",
        "aaaabbbbccccddddeeeeffffgggghhhhiiiijjjj",
        "https://vault.azure.net"
  )

  @Test
  internal fun accessTokenIsPopulatedWhenKeyVaultVariableExists() {
    val context = buildContext()
    val connector = AzureTokenConnector(baseUrl())
    val processor = AzureBuildStartContextProcessor(connector)

    server.enqueue(MockResponse().setBody(tokenResponse))
    processor.updateParameters(context)

    // todo inspect build parameters for access token
    val request = server.takeRequest()
    Assertions.assertThat(request.path).isEqualTo("/${settings.tenantId}/oauth2/token")
  }

  private fun buildContext(): BuildStartContext {
    return buildContextWith(featureDescriptor(), parametersProvider())
  }

  private fun featureDescriptor(): ProjectFeatureDescriptorImpl {
    return ProjectFeatureDescriptorImpl(
          "id",
          "type",
          featureParams(),
          "someProjectId")
  }

  private fun featureParams(): HashMap<String, String> {
    val params = HashMap(settings.toMap())
    params[OAuthConstants.OAUTH_TYPE_PARAM] = KeyVaultConstants.FEATURE_TYPE
    return params
  }

  private fun parametersProvider(): MapParametersProviderImpl {
    return MapParametersProviderImpl(mapOf(
          "favourite.colour" to "is %keyvault:colour%!",
          "favourite.subject" to "is %keyvault:subject%!")
    )
  }

  private fun baseUrl() = server.url("/").toString()

  private val tokenResponse = """
        {
          "token_type": "Bearer",
          "expires_in": "3600",
          "ext_expires_in": "0",
          "expires_on": "1537617038",
          "not_before": "1537613138",
          "resource": "https://vault.azure.net",
          "access_token": "${"long-string-".padEnd(1000, 'x')}"
        }
      """.trimIndent()
}
