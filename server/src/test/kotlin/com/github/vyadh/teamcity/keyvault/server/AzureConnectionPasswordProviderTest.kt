package com.github.vyadh.teamcity.keyvault.server

import com.github.vyadh.teamcity.keyvault.common.AzureTokenConstants
import com.github.vyadh.teamcity.keyvault.server.BuildContexts.buildWith
import com.github.vyadh.teamcity.keyvault.server.BuildContexts.featureDescriptor
import com.github.vyadh.teamcity.keyvault.server.BuildContexts.featureParamsWith
import com.github.vyadh.teamcity.keyvault.server.BuildContexts.parametersProvider
import jetbrains.buildServer.serverSide.*
import jetbrains.buildServer.serverSide.oauth.OAuthConstants
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class AzureConnectionPasswordProviderTest {

  @Test
  fun connectionSecretIsAddedToPasswordProvider() {
    val secret = "service-principal-password"
    val build = buildWithSecret(secret)
    val passwordProvider = AzureConnectionPasswordProvider()

    val results = passwordProvider.getPasswordParameters(build)

    assertThat(results)
          .hasOnlyOneElementSatisfying {
            it.name == AzureTokenConstants.CLIENT_SECRET && it.value == secret
          }
  }

  @Test
  fun connectionSecretIsNotAddedToPasswordProviderWhenEmpty() {
    val secret = "  "
    val build = buildWithSecret(secret)
    val passwordProvider = AzureConnectionPasswordProvider()

    val results = passwordProvider.getPasswordParameters(build)

    assertThat(results).isEmpty()
  }

  @Test
  fun connectionSecretIsAddedToPasswordProviderWhenMultipleFeatureDescriptors() {
    val secret = "service-principal-password"
    val descriptors = listOf(
          featureDescriptor(mapOf(OAuthConstants.OAUTH_TYPE_PARAM to "other type")),
          descriptorWithSecret(secret)
    )
    val build = buildWith(descriptors, parametersProvider(mapOf()))
    val passwordProvider = AzureConnectionPasswordProvider()

    val results = passwordProvider.getPasswordParameters(build)

    assertThat(results)
          .hasOnlyOneElementSatisfying {
            it.name == AzureTokenConstants.CLIENT_SECRET && it.value == secret
          }
  }

  private fun buildWithSecret(secret: String): SBuild {
    return buildWith(descriptorWithSecret(secret), parametersProvider(mapOf()))
  }

  private fun descriptorWithSecret(secret: String): SProjectFeatureDescriptor {
    return featureDescriptor(
          featureParamsWith(AzureTokenConstants.CLIENT_SECRET to secret))
  }

}
