package com.github.vyadh.teamcity.keyvault.server

import com.github.vyadh.teamcity.keyvault.common.KeyVaultConstants
import com.github.vyadh.teamcity.keyvault.common.KeyVaultFeatureSettings
import com.github.vyadh.teamcity.keyvault.server.BuildContexts.buildContextWith
import jetbrains.buildServer.parameters.impl.MapParametersProviderImpl
import jetbrains.buildServer.serverSide.*
import jetbrains.buildServer.serverSide.impl.ProjectFeatureDescriptorImpl
import jetbrains.buildServer.serverSide.oauth.OAuthConstants
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class KeyVaultServerIntegrationTest {

  private val settings = KeyVaultFeatureSettings(
        "00000001-0001-0001-0001-000000000001",
        "00000002-0002-0002-0002-000000000002",
        "aaaabbbbccccddddeeeeffffgggghhhhiiiijjjj",
        "https://vault.azure.net"
  )

  @Test
  internal fun accessTokenIsPopulatedWhenKeyVaultVariableExists() {
    val context = buildContext()

    // todo replace with test against stubbed HTTP server impl
    val connector = Mockito.mock(AzureConnector::class.java)
    val processor = AzureBuildStartContextProcessor(connector)

    processor.updateParameters(context)

    // todo inspect build parameters for token
    Mockito.verify(connector).requestToken(settings)
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

}
