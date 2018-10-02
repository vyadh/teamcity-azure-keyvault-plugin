package com.github.vyadh.teamcity.keyvault.common

import jetbrains.buildServer.parameters.ReferencesResolverUtil
import java.util.stream.Collectors
import java.util.stream.Stream

object TeamCityVariableRefs {

  private val prefix = "%${KeyVaultConstants.VAR_PREFIX}"

  fun containsRef(map: Map<String, String>): Boolean {
    return map.values.any { it.contains(prefix) }
  }

  fun searchRefs(paramValues: Stream<String>): Set<String> {
    val prefixes = arrayOf(KeyVaultConstants.VAR_PREFIX)

    val vars = paramValues
          .filter { value -> value.contains(KeyVaultConstants.VAR_PREFIX) }
          .flatMap { value -> references(value, prefixes) }

    return vars.collect(Collectors.toSet<String>())
  }

  private fun references(value: String, prefixes: Array<String>) =
        ReferencesResolverUtil
              .getReferences(value, prefixes, true)
              .stream()

}
