package com.github.vyadh.teamcity.keyvault.common

import com.github.vyadh.teamcity.keyvault.common.KeyVaultConstants.RESOURCE_URI
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KeyVaultFeatureSettingsTest {

  @Test
  fun symmetricData() {
    val map = hashMapOf(
          KeyVaultConstants.TENANT_ID to "t",
          KeyVaultConstants.CLIENT_ID to "cid",
          KeyVaultConstants.CLIENT_SECRET to "cs",
          RESOURCE_URI to "res")

    val resultMap = KeyVaultFeatureSettings.fromMap(map).toMap()

    assertThat(resultMap).isEqualTo(map)
  }

  @Test
  fun defaultedToEmptyStrings() {
    val map = emptyMap<String, String>()

    val resultMap = KeyVaultFeatureSettings.fromMap(map).toMap()

    assertThat(resultMap).isEqualTo(hashMapOf(
          KeyVaultConstants.TENANT_ID to "",
          KeyVaultConstants.CLIENT_ID to "",
          KeyVaultConstants.CLIENT_SECRET to "",
          RESOURCE_URI to ""))
  }

}
