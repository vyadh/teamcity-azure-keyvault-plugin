package com.github.vyadh.teamcity.keyvault.server

import com.github.vyadh.teamcity.keyvault.common.KeyVaultConstants
import jetbrains.buildServer.parameters.ParametersProvider
import jetbrains.buildServer.serverSide.*
import jetbrains.buildServer.serverSide.oauth.OAuthConstants
import org.mockito.Mockito

object BuildContexts {

  internal fun buildContextWithParams(params: Map<String, String>): BuildStartContext {
    val descriptor = Mockito.mock(SProjectFeatureDescriptor::class.java)
    Mockito.`when`(descriptor.parameters).thenReturn(featureParams())

    val paramsProvider = Mockito.mock(ParametersProvider::class.java)
    Mockito.`when`(paramsProvider.all).thenReturn(params)

    return buildContextWith(descriptor, paramsProvider)
  }

  internal fun featureParams(): Map<String, String> {
    return mapOf(
          OAuthConstants.OAUTH_TYPE_PARAM to KeyVaultConstants.FEATURE_TYPE,
          KeyVaultConstants.TENANT_ID to "a",
          KeyVaultConstants.CLIENT_ID to "b",
          KeyVaultConstants.CLIENT_SECRET to "c",
          KeyVaultConstants.RESOURCE_URI to "d"
    )
  }

  internal fun buildContextWith(
        featureDescriptor: SProjectFeatureDescriptor,
        paramsProvider: ParametersProvider): BuildStartContext {

    val project = Mockito.mock(SProject::class.java)
    Mockito.`when`(project.getAvailableFeaturesOfType(OAuthConstants.FEATURE_TYPE))
          .thenReturn(listOf(featureDescriptor))

    val buildType = Mockito.mock(SBuildType::class.java)
    Mockito.`when`(buildType.project).thenReturn(project)

    val build = Mockito.mock(SRunningBuild::class.java)
    Mockito.`when`(build.parametersProvider).thenReturn(paramsProvider)
    Mockito.`when`(build.buildType).thenReturn(buildType)

    val context = Mockito.mock(BuildStartContext::class.java)
    Mockito.`when`(context.build).thenReturn(build)

    return context
  }

}
