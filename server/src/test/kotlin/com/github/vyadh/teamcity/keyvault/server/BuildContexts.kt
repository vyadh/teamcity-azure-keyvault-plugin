package com.github.vyadh.teamcity.keyvault.server

import com.github.vyadh.teamcity.keyvault.common.AzureTokenConstants
import com.github.vyadh.teamcity.keyvault.common.KeyVaultConstants
import jetbrains.buildServer.parameters.ParametersProvider
import jetbrains.buildServer.serverSide.*
import jetbrains.buildServer.serverSide.oauth.OAuthConstants
import org.mockito.Mockito

object BuildContexts {

  internal fun featureDescriptor(featureParams: Map<String, String>): SProjectFeatureDescriptor {
    val descriptor = Mockito.mock(SProjectFeatureDescriptor::class.java)
    Mockito.`when`(descriptor.parameters).thenReturn(featureParams)
    return descriptor
  }

  internal fun parametersProvider(params: Map<String, String>): ParametersProvider {
    val paramsProvider = Mockito.mock(ParametersProvider::class.java)
    Mockito.`when`(paramsProvider.all).thenReturn(params)
    return paramsProvider
  }

  internal fun featureParams(): Map<String, String> {
    return mapOf(
          OAuthConstants.OAUTH_TYPE_PARAM to KeyVaultConstants.FEATURE_TYPE,
          AzureTokenConstants.TENANT_ID to "a",
          AzureTokenConstants.CLIENT_ID to "b",
          AzureTokenConstants.CLIENT_SECRET to "c",
          AzureTokenConstants.RESOURCE_URI to "d"
    )
  }

  internal fun featureParamsWith(pair: Pair<String, String>): Map<String, String> {
    val params = HashMap(featureParams())
    params[pair.first] = pair.second
    return params
  }

  internal fun buildContextWithRelevantParams(): BuildStartContext {
    val params = mapOf(
          "example-key" to "some relevant %keyvault:secret%"
    )
    return buildContextWithParams(params)
  }

  internal fun buildContextWithParams(params: Map<String, String>): BuildStartContext {
    val descriptor = featureDescriptor(featureParams())
    val paramsProvider = parametersProvider(params)

    return buildContextWith(descriptor, paramsProvider)
  }

  internal fun buildContextWith(
        featureDescriptor: SProjectFeatureDescriptor,
        paramsProvider: ParametersProvider): BuildStartContext {

    val build = buildWith(featureDescriptor, paramsProvider)

    val context = Mockito.mock(BuildStartContext::class.java)
    Mockito.`when`(context.build).thenReturn(build)
    return context
  }

  internal fun buildWith(
        featureDescriptor: SProjectFeatureDescriptor,
        paramsProvider: ParametersProvider): SRunningBuild {

    val project = Mockito.mock(SProject::class.java)
    Mockito.`when`(project.getAvailableFeaturesOfType(OAuthConstants.FEATURE_TYPE))
          .thenReturn(listOf(featureDescriptor))

    val buildType = Mockito.mock(SBuildType::class.java)
    Mockito.`when`(buildType.project).thenReturn(project)

    val build = Mockito.mock(SRunningBuild::class.java)
    Mockito.`when`(build.parametersProvider).thenReturn(paramsProvider)
    Mockito.`when`(build.buildType).thenReturn(buildType)
    return build
  }

}
