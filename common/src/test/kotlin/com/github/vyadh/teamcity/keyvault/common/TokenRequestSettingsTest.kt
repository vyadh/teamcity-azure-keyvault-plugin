package com.github.vyadh.teamcity.keyvault.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TokenRequestSettingsTest {

  @Test
  fun symmetricData() {
    val map = hashMapOf(
          AzureTokenConstants.TENANT_ID to "t",
          AzureTokenConstants.CLIENT_ID to "cid",
          AzureTokenConstants.CLIENT_SECRET to "cs",
          AzureTokenConstants.RESOURCE_URI to "res")

    val resultMap = TokenRequestSettings.fromMap(map).toMap()

    assertThat(resultMap).isEqualTo(map)
  }

  @Test
  fun defaultedToEmptyStrings() {
    val map = emptyMap<String, String>()

    val resultMap = TokenRequestSettings.fromMap(map).toMap()

    assertThat(resultMap).isEqualTo(hashMapOf(
          AzureTokenConstants.TENANT_ID to "",
          AzureTokenConstants.CLIENT_ID to "",
          AzureTokenConstants.CLIENT_SECRET to "",
          AzureTokenConstants.RESOURCE_URI to ""))
  }

}
