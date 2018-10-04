package com.github.vyadh.teamcity.keyvault.agent

import com.github.vyadh.teamcity.keyvault.common.KeyVaultRef

interface KeyVaultConnector {
  fun requestValue(ref: KeyVaultRef, accessToken: String): SecretResponse
}