package com.github.vyadh.teamcity.keyvault.server

import com.github.vyadh.teamcity.keyvault.common.TokenRequestSettings

interface TokenConnector {

  fun requestToken(settings: TokenRequestSettings): TokenResponse

}
