package com.github.vyadh.teamcity.keyvault.server

import com.github.vyadh.teamcity.keyvault.common.AzureTokenConstants
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class AzureConnectionProviderTest {

  @Test
  internal fun populateEmptyValuesWhenMissing() {
    val processor = propertiesProcessor()
    val mutableMap = HashMap<String, String>()

    processor.process(mutableMap)

    assertThat(mutableMap)
          .containsOnlyKeys(
                AzureTokenConstants.TENANT_ID,
                AzureTokenConstants.CLIENT_ID,
                AzureTokenConstants.CLIENT_SECRET,
                AzureTokenConstants.RESOURCE_URI
          )
          .containsValues("", "", "", "")
  }

  @Test
  internal fun populateExistingValues() {
    val processor = propertiesProcessor()
    val mutableMap = hashMapOf(
          AzureTokenConstants.TENANT_ID to "t",
          AzureTokenConstants.CLIENT_ID to "cid",
          AzureTokenConstants.CLIENT_SECRET to "cs",
          AzureTokenConstants.RESOURCE_URI to "res")
    val originalMap = HashMap(mutableMap)

    processor.process(mutableMap)

    assertThat(mutableMap).isEqualTo(originalMap)
  }

  private fun propertiesProcessor(): PropertiesProcessor {
    val provider = AzureConnectionProvider(Mockito.mock(PluginDescriptor::class.java))
    return provider.propertiesProcessor
  }

}
