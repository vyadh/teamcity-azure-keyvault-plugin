package com.github.vyadh.teamcity.keyvault.server

import com.github.vyadh.teamcity.keyvault.common.TokenRequestSettings

interface AzureConnector {

  fun requestToken(settings: TokenRequestSettings)

}
