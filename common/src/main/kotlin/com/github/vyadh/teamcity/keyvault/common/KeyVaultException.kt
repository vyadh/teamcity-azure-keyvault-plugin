package com.github.vyadh.teamcity.keyvault.common

class KeyVaultException(message: String, cause: Throwable?) : Exception(message, cause) {

  constructor(message: String): this(message, null)

}
