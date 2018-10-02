package com.github.vyadh.teamcity.keyvault.common

data class KeyVaultRef(val ref: String) {

  private val colon = ref.indexOf(':')
  private val slash = ref.indexOf('/')

  val instance: String =
        if (valid()) ref.substring(colon + 1, slash)
        else ""

  val name: String =
        if (valid()) ref.substring(slash + 1)
        else ""

  fun valid(): Boolean {
    return colon != -1 && slash != -1 && colon < slash
  }

}
