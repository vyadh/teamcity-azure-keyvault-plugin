package com.github.vyadh.teamcity.keyvault.common

object KeyVaultConstants {

  /** Indicates should provide the Azure access token itself to running builds. */
  val PROVIDE_TOKEN_PROPERTY = "teamcity.azurekeyvault.allow_token"
  /** Property to hold the access token gained from Azure AD. */
  val ACCESS_TOKEN_PROPERTY = "teamcity.azurekeyvault.access_token"

  /** Users specify this special variable prefix to obtain KV secrets.
   *  For example: %keyvault:<vault ref>/path% */
  @JvmField val VAR_PREFIX = "keyvault:"

  /** Allows associating the KV TeamCity feature with the corresponding params. */
  @JvmField val FEATURE_TYPE = "teamcity-azurekeyvault"

}
