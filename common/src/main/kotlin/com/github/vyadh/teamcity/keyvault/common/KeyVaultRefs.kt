package com.github.vyadh.teamcity.keyvault.common

import jetbrains.buildServer.parameters.ReferencesResolverUtil
import java.util.stream.Stream

object KeyVaultRefs {

  private val prefix = "%${KeyVaultConstants.VAR_PREFIX}"

  fun containsRef(map: Map<String, String>): Boolean {
    return map.values.any { it.contains(prefix) }
  }

  fun searchRefs(paramValues: Stream<String>): Stream<KeyVaultRef> {
    val prefixes = arrayOf(KeyVaultConstants.VAR_PREFIX)

    return paramValues
          .filter { value -> value.contains(KeyVaultConstants.VAR_PREFIX) }
          .flatMap { value -> references(value, prefixes) }
          .distinct()
          .sorted()
          .map(::KeyVaultRef)
  }

  private fun references(value: String, prefixes: Array<String>) =
        ReferencesResolverUtil
              .getReferences(value, prefixes, true)
              .stream()

}
