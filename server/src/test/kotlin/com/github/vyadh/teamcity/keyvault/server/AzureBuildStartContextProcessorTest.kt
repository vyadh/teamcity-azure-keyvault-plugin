package com.github.vyadh.teamcity.keyvault.server

import com.github.vyadh.teamcity.keyvault.common.KeyVaultConstants
import com.github.vyadh.teamcity.keyvault.common.KeyVaultFeatureSettings
import com.github.vyadh.teamcity.keyvault.server.KotlinMockitoMatchers.any
import jetbrains.buildServer.parameters.ParametersProvider
import jetbrains.buildServer.serverSide.*
import jetbrains.buildServer.serverSide.oauth.OAuthConstants
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

internal class AzureBuildStartContextProcessorTest {

  @Test
  fun tokenNotRequestedBuildDoesNotHaveKeyVaultFeature() {
    val context = contextWithIrrelevantOAuthFeature()
    val connector = mock(AzureConnector::class.java)
    val processor = AzureBuildStartContextProcessor(connector)

    processor.updateParameters(context)

    verify(connector, never()).requestToken(any())
  }

  @Test
  fun tokenNotWhenBuildDoesNotHaveRelevantParameters() {
    val params = mapOf(
          "key" to "some irrelevant %other:secret% message"
    )
    val context = contextWithParams(params)
    val connector = mock(AzureConnector::class.java)
    val processor = AzureBuildStartContextProcessor(connector)

    processor.updateParameters(context)

    verify(connector, never()).requestToken(any())
  }

  @Test
  fun tokenRequestedWhenBuildFeatureAndParametersExist() {
    val params = mapOf(
          "key" to "some relevant %keyvault:secret% message"
    )
    val context = contextWithParams(params)
    val connector = mock(AzureConnector::class.java)
    val processor = AzureBuildStartContextProcessor(connector)

    processor.updateParameters(context)

    verify(connector).requestToken(KeyVaultFeatureSettings.fromMap(featureParams()))
  }

  private fun contextWithParams(params: Map<String, String>): BuildStartContext {
    val descriptor = mock(SProjectFeatureDescriptor::class.java)
    `when`(descriptor.parameters).thenReturn(featureParams())

    val paramsProvider = mock(ParametersProvider::class.java)
    `when`(paramsProvider.all).thenReturn(params)

    return contextWith(descriptor, paramsProvider)
  }

  private fun featureParams(): Map<String, String> {
    return mapOf(
          OAuthConstants.OAUTH_TYPE_PARAM to KeyVaultConstants.FEATURE_TYPE,
          KeyVaultConstants.TENANT_ID to "a",
          KeyVaultConstants.CLIENT_ID to "b",
          KeyVaultConstants.CLIENT_SECRET to "c",
          KeyVaultConstants.RESOURCE_URI to "d"
    )
  }

  private fun contextWithIrrelevantOAuthFeature(): BuildStartContext {
    val params = mapOf(OAuthConstants.OAUTH_TYPE_PARAM to "irrelevant")

    val descriptor = mock(SProjectFeatureDescriptor::class.java)
    `when`(descriptor.parameters).thenReturn(params)

    val paramsProvider = mock(ParametersProvider::class.java)
    `when`(paramsProvider.all).thenReturn(emptyMap())

    return contextWith(descriptor, paramsProvider)
  }

  private fun contextWith(
        featureDescriptor: SProjectFeatureDescriptor,
        paramsProvider: ParametersProvider): BuildStartContext {

    val project = mock(SProject::class.java)
    `when`(project.getAvailableFeaturesOfType(OAuthConstants.FEATURE_TYPE))
          .thenReturn(listOf(featureDescriptor))

    val buildType = mock(SBuildType::class.java)
    `when`(buildType.project).thenReturn(project)

    val build = mock(SRunningBuild::class.java)
    `when`(build.parametersProvider).thenReturn(paramsProvider)
    `when`(build.buildType).thenReturn(buildType)

    val context = mock(BuildStartContext::class.java)
    `when`(context.build).thenReturn(build)

    return context
  }

}
