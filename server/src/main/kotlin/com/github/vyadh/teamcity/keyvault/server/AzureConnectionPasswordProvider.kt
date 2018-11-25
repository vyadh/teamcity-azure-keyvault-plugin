package com.github.vyadh.teamcity.keyvault.server

import com.github.vyadh.teamcity.keyvault.common.AzureTokenConstants
import com.github.vyadh.teamcity.keyvault.common.KeyVaultConstants
import jetbrains.buildServer.serverSide.Parameter
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SimpleParameter
import jetbrains.buildServer.serverSide.oauth.OAuthConstants
import jetbrains.buildServer.serverSide.parameters.types.PasswordsProvider
import jetbrains.buildServer.util.StringUtil

class AzureConnectionPasswordProvider : PasswordsProvider {

  override fun getPasswordParameters(build: SBuild): MutableCollection<Parameter> {
    val secret = findSecret(build)

    return if (StringUtil.isEmptyOrSpaces(secret)) {
      mutableListOf()
    } else {
      mutableListOf(SimpleParameter(AzureTokenConstants.CLIENT_SECRET, secret!!))
    }
  }

  private fun findSecret(build: SBuild): String? {
    return build.buildType
          ?.project
          ?.getAvailableFeaturesOfType(OAuthConstants.FEATURE_TYPE)
          ?.first { it.parameters[OAuthConstants.OAUTH_TYPE_PARAM] == KeyVaultConstants.FEATURE_TYPE }
          ?.parameters
          ?.get(AzureTokenConstants.CLIENT_SECRET)
  }

}
