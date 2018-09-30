package com.github.vyadh.teamcity.keyvault.server

import com.github.vyadh.teamcity.keyvault.common.KeyVaultConstants
import com.github.vyadh.teamcity.keyvault.common.TokenRequestSettings
import com.github.vyadh.teamcity.keyvault.server.BuildContexts.buildWith
import jetbrains.buildServer.parameters.impl.MapParametersProviderImpl
import jetbrains.buildServer.serverSide.BuildStartContext
import jetbrains.buildServer.serverSide.RunTypesProvider
import jetbrains.buildServer.serverSide.impl.ProjectFeatureDescriptorImpl
import jetbrains.buildServer.serverSide.impl.build.steps.BuildStartContextImpl
import jetbrains.buildServer.serverSide.oauth.OAuthConstants
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito

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

    verifyTokenConnectorRequest()
    verifyAccessTokenParameterPopulated(context)
  }

  private fun verifyTokenConnectorRequest() {
    val request = server.takeRequest()
    assertThat(request.path).isEqualTo("/${settings.tenantId}/oauth2/token")
  }

  private fun verifyAccessTokenParameterPopulated(context: BuildStartContext) {
    assertThat(
          context.sharedParameters[KeyVaultConstants.ACCESS_TOKEN_PROPERTY]
    ).isEqualTo(accessToken)
  }

  private fun buildContext(): BuildStartContext {
    val build = buildWith(featureDescriptor(), parametersProvider())
    val runTypesProvider = Mockito.mock(RunTypesProvider::class.java)
    val parameters = HashMap<String, String>()
    // Using real TeamCity objects where possible for integration test
    return BuildStartContextImpl(runTypesProvider, build, parameters)
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

  private val accessToken =
        "long-string-".padEnd(1000, 'x')

  private val tokenResponse = """
        {
          "token_type": "Bearer",
          "expires_in": "3600",
          "ext_expires_in": "0",
          "expires_on": "1537617038",
          "not_before": "1537613138",
          "resource": "https://vault.azure.net",
          "access_token": "$accessToken"
        }
      """.trimIndent()
}
