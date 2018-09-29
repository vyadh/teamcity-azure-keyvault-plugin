package com.github.vyadh.teamcity.keyvault.common

object KeyVaultConstants {

  /** Allow associating the KV TeamCity feature with their params. */
  @JvmField val FEATURE_TYPE = "teamcity-keyvault"

  /** Users specify this special variable prefix to obtain KV secrets.
   *  For example: %keyvault:<vault ref>/path% todo finalise format */
  @JvmField val VAR_PREFIX = "keyvault:"

  /** Provide the Azure access token itself to running builds. */
  @JvmField val PROVIDE_TOKEN = "teamcity.keyvault.allow_token"

  // The following are the parameter keys from KeyVaultJspKeys

  @JvmField val TENANT_ID = "tenant-id"
  @JvmField val CLIENT_ID = "client-id"
  @JvmField val CLIENT_SECRET = "secure:client-secret"
  @JvmField val RESOURCE_URI = "resource-uri"

  /** Can get tokens for different things in Azure, but show KV by default. */
  @JvmField val DEFAULT_RESOURCE_URI = "https://vault.azure.net"

}
