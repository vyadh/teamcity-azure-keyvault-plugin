package com.github.vyadh.teamcity.keyvault.common

object KeyVaultConstants {

  @JvmField val FEATURE_TYPE = "teamcity-keyvault"
  @JvmField val VAR_PREFIX = "keyvault:"

  @JvmField val TENANT_ID = "tenant-id"
  @JvmField val CLIENT_ID = "client-id"
  @JvmField val CLIENT_SECRET = "secure:client-secret"
  @JvmField val RESOURCE_URI = "resource-uri"

  @JvmField val DEFAULT_RESOURCE_URI = "https://vault.azure.net"

}
