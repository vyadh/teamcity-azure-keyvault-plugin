package com.github.vyadh.teamcity.keyvault.common

import com.github.vyadh.teamcity.keyvault.common.KeyVaultConstants.CLIENT_ID
import com.github.vyadh.teamcity.keyvault.common.KeyVaultConstants.CLIENT_SECRET
import com.github.vyadh.teamcity.keyvault.common.KeyVaultConstants.RESOURCE_URI
import com.github.vyadh.teamcity.keyvault.common.KeyVaultConstants.TENANT_ID

data class TokenRequestSettings(
      val tenantId: String,
      val clientId: String,
      val clientSecret: String,
      val resourceUri: String) {

  companion object {
    fun fromMap(map: Map<String, String>) = TokenRequestSettings(
          map[TENANT_ID] ?: "",
          map[CLIENT_ID] ?: "",
          map[CLIENT_SECRET] ?: "",
          map[RESOURCE_URI] ?: ""
    )
  }

  fun toMap() = hashMapOf(
          TENANT_ID to tenantId,
          CLIENT_ID to clientId,
          CLIENT_SECRET to clientSecret,
          RESOURCE_URI to resourceUri
  )

}
