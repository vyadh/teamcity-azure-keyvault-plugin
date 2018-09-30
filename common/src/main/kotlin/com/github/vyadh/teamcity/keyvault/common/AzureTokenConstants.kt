package com.github.vyadh.teamcity.keyvault.common

/** Used to request tokens from Azure Active Directory. */
object AzureTokenConstants {

  // Parameter keys from AzureTokenJspKeys

  @JvmField val TENANT_ID = "tenant-id"
  @JvmField val CLIENT_ID = "client-id"
  @JvmField val CLIENT_SECRET = "secure:client-secret"
  @JvmField val RESOURCE_URI = "resource-uri"

  /** Can get tokens for different things in Azure, but show KV by default. */
  @JvmField val DEFAULT_RESOURCE_URI = "https://vault.azure.net"

}
