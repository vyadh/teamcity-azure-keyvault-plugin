package com.github.vyadh.teamcity.keyvault.common

import com.github.vyadh.teamcity.keyvault.common.KeyVaultVariableRefs.containsVars
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class KeyVaultVariableRefsTest {

  @Test
  internal fun noReferenceExists() {
    assertThat(containsVars(emptyMap())).isFalse()
    assertThat(containsVars(mapOf("key" to "value"))).isFalse()
  }

  @Test
  internal fun irrelevantReferenceAreIgnored() {
    assertThat(containsVars(mapOf("key" to "%var%"))).isFalse()
    assertThat(containsVars(mapOf("key" to "Pan %var% Gargleblaster"))).isFalse()
    assertThat(containsVars(mapOf("key" to "Never lose your %key:object%"))).isFalse()
  }

  @Test
  internal fun keyVaultReferenceExists() {
    assertThat(containsVars(mapOf("key" to "%keyvault:%"))).isTrue()
    assertThat(containsVars(mapOf("key" to "The answer is a %keyvault:number%..."))).isTrue()
  }

}
