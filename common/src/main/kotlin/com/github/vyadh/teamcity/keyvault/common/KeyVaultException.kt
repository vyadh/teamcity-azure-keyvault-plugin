package com.github.vyadh.teamcity.keyvault.common

import java.lang.RuntimeException

class KeyVaultException(message: String, cause: Throwable?)
      : RuntimeException(message, cause) {

  constructor(message: String): this(message, null)

}
