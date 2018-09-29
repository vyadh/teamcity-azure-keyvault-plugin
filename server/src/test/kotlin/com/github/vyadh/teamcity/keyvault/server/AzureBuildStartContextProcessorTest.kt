package com.github.vyadh.teamcity.keyvault.server

import com.github.vyadh.teamcity.keyvault.common.KeyVaultConstants
import com.github.vyadh.teamcity.keyvault.common.TokenRequestSettings
import com.github.vyadh.teamcity.keyvault.server.BuildContexts.buildContextWith
import com.github.vyadh.teamcity.keyvault.server.BuildContexts.buildContextWithParams
import com.github.vyadh.teamcity.keyvault.server.BuildContexts.featureDescriptor
import com.github.vyadh.teamcity.keyvault.server.BuildContexts.featureParams
import com.github.vyadh.teamcity.keyvault.server.BuildContexts.featureParamsWith
import com.github.vyadh.teamcity.keyvault.server.BuildContexts.parametersProvider
import com.github.vyadh.teamcity.keyvault.server.KotlinMockitoMatchers.any
import jetbrains.buildServer.parameters.ParametersProvider
import jetbrains.buildServer.serverSide.BuildStartContext
import jetbrains.buildServer.serverSide.oauth.OAuthConstants
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.*

internal class AzureBuildStartContextProcessorTest {

  @Test
  fun tokenNotRequestedBuildDoesNotHaveKeyVaultFeature() {
    val context = buildContextWithIrrelevantOAuthFeature()
    val connector = mock(TokenConnector::class.java)
    val processor = AzureBuildStartContextProcessor(connector)

    processor.updateParameters(context)

    verify(connector, never()).requestToken(any())
  }

  @Test
  fun tokenNotWhenBuildDoesNotHaveRelevantParameters() {
    val params = mapOf(
          "key" to "some irrelevant %other:secret% message"
    )
    val context = buildContextWithParams(params)
    val connector = mock(TokenConnector::class.java)
    val processor = AzureBuildStartContextProcessor(connector)

    processor.updateParameters(context)

    verify(connector, never()).requestToken(any())
  }

  @Test
  fun tokenRequestedWhenBuildFeatureAndParametersExist() {
    val params = mapOf(
          "key" to "some relevant %keyvault:secret% message"
    )
    val context = buildContextWithParams(params)
    val connector = mock(TokenConnector::class.java)
    val processor = AzureBuildStartContextProcessor(connector)

    processor.updateParameters(context)

    verify(connector).requestToken(TokenRequestSettings.fromMap(featureParams()))
  }

  @Test
  fun tokenRequestedWhenProvideTokenIsSpecified() {
    val featureParams = featureParamsWith(
          KeyVaultConstants.PROVIDE_TOKEN to "true")

    val context = buildContextWith(
          featureDescriptor(featureParams),
          parametersProvider(emptyMap())) // No key vault params

    val connector = mock(TokenConnector::class.java)
    val processor = AzureBuildStartContextProcessor(connector)

    processor.updateParameters(context)

    verify(connector).requestToken(TokenRequestSettings.fromMap(featureParams))
  }

  private fun buildContextWithIrrelevantOAuthFeature(): BuildStartContext {
    val params = mapOf(OAuthConstants.OAUTH_TYPE_PARAM to "irrelevant")
    val descriptor = featureDescriptor(params)

    val paramsProvider = Mockito.mock(ParametersProvider::class.java)
    Mockito.`when`(paramsProvider.all).thenReturn(emptyMap())

    return BuildContexts.buildContextWith(descriptor, paramsProvider)
  }

}
