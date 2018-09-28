package com.github.vyadh.teamcity.keyvault.server

import com.github.vyadh.teamcity.keyvault.common.KeyVaultConstants
import com.github.vyadh.teamcity.keyvault.common.KeyVaultFeatureSettings
import com.github.vyadh.teamcity.keyvault.server.KotlinMockitoMatchers.any
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
    //todo
  }

  @Test
  fun tokenRequestedWhenBuildFeatureAndParametersExist() {
    val params = mapOf(
          OAuthConstants.OAUTH_TYPE_PARAM to KeyVaultConstants.FEATURE_TYPE,
          KeyVaultConstants.TENANT_ID to "a",
          KeyVaultConstants.CLIENT_ID to "b",
          KeyVaultConstants.CLIENT_SECRET to "c",
          KeyVaultConstants.RESOURCE_URI to "d"
    )
    val context = contextWithParameters(params)
    val connector = mock(AzureConnector::class.java)
    val processor = AzureBuildStartContextProcessor(connector)

    processor.updateParameters(context)

    verify(connector).requestToken(KeyVaultFeatureSettings.fromMap(params))
  }

  private fun contextWithParameters(map: Map<String, String>): BuildStartContext {
    val descriptor = mock(SProjectFeatureDescriptor::class.java)
    `when`(descriptor.parameters).thenReturn(map)
    return contextWith(descriptor)
  }

  private fun contextWithIrrelevantOAuthFeature(): BuildStartContext {
    val params = mapOf("value" to "irrelevant")

    val descriptor = mock(SProjectFeatureDescriptor::class.java)
    `when`(descriptor.parameters).thenReturn(params)

    return contextWith(descriptor)
  }

  private fun contextWith(descriptor: SProjectFeatureDescriptor): BuildStartContext {
    val project = mock(SProject::class.java)
    `when`(project.getAvailableFeaturesOfType(OAuthConstants.FEATURE_TYPE))
          .thenReturn(listOf(descriptor))

    val buildType = mock(SBuildType::class.java)
    `when`(buildType.project).thenReturn(project)

    val build = mock(SRunningBuild::class.java)
    `when`(build.buildType).thenReturn(buildType)

    val context = mock(BuildStartContext::class.java)
    `when`(context.build).thenReturn(build)

    return context
  }

}
