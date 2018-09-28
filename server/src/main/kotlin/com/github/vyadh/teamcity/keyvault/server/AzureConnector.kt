package com.github.vyadh.teamcity.keyvault.server

import com.github.vyadh.teamcity.keyvault.common.KeyVaultFeatureSettings

interface AzureConnector {

  fun requestToken(settings: KeyVaultFeatureSettings)

}
