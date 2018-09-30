package com.github.vyadh.teamcity.keyvault.common

object KeyVaultConstants {

  /** Indicates should provide the Azure access token itself to running builds. */
  val PROVIDE_TOKEN_PROPERTY = "teamcity.keyvault.allow_token"
  //todo not obfuscated if output to log
  /** Property to hold the access token gained from Azure AD. */
  val ACCESS_TOKEN_PROPERTY = "secure:teamcity.keyvault.access_token"

  /** Users specify this special variable prefix to obtain KV secrets.
   *  For example: %keyvault:<vault ref>/path% todo finalise format */
  @JvmField val VAR_PREFIX = "keyvault:"

  /** Allows associating the KV TeamCity feature with the corresponding params. */
  @JvmField val FEATURE_TYPE = "teamcity-keyvault"

  // The following are the parameter keys from KeyVaultJspKeys

  @JvmField val TENANT_ID = "tenant-id"
  @JvmField val CLIENT_ID = "client-id"
  @JvmField val CLIENT_SECRET = "secure:client-secret"
  @JvmField val RESOURCE_URI = "resource-uri"

  /** Can get tokens for different things in Azure, but show KV by default. */
  @JvmField val DEFAULT_RESOURCE_URI = "https://vault.azure.net"

}
