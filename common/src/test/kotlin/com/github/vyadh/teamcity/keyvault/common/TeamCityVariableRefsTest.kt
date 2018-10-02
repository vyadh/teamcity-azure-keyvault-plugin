package com.github.vyadh.teamcity.keyvault.common

import com.github.vyadh.teamcity.keyvault.common.TeamCityVariableRefs.containsRef
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.stream.Stream

internal class TeamCityVariableRefsTest {

  @Test
  internal fun noReferenceExists() {
    assertThat(containsRef(emptyMap())).isFalse()
    assertThat(containsRef(mapOf("key" to "value"))).isFalse()
  }

  @Test
  internal fun irrelevantReferenceAreIgnored() {
    assertThat(containsRef(mapOf("key" to "%var%"))).isFalse()
    assertThat(containsRef(mapOf("key" to "Pan %var% Gargleblaster"))).isFalse()
    assertThat(containsRef(mapOf("key" to "Never lose your %key:object%"))).isFalse()
  }

  @Test
  internal fun keyVaultReferenceExists() {
    assertThat(containsRef(mapOf("key" to "%keyvault:%"))).isTrue()
    assertThat(containsRef(mapOf("key" to "The answer is a %keyvault:number%..."))).isTrue()
  }

  @Test
  internal fun searchExtractsReferences() {
    val paramValues = Stream.of(
          "a) %keyvault:storeA/name1%, b) %keyvault:storeA/name2%",
          "c) %keyvault:storeB/name% and that's all"
    )

    val refs = TeamCityVariableRefs.searchRefs(paramValues)

    assertThat(refs).containsExactly(
          "keyvault:storeA/name1",
          "keyvault:storeA/name2",
          "keyvault:storeB/name"
    )
  }

  @Test
  internal fun searchRemovesDuplicates() {
    val paramValues = Stream.of(
          "a) %keyvault:storeA/name%",
          "b) %keyvault:storeA/name%",
          "c) %keyvault:storeB/name%",
          "d) %keyvault:storeB/name%"
    )

    val refs = TeamCityVariableRefs.searchRefs(paramValues)

    assertThat(refs).containsExactly(
          "keyvault:storeA/name",
          "keyvault:storeB/name"
    )
  }

  @Test
  internal fun searchSortsItemsForPossiblyMoreEfficientQueries() {
    val paramValues = Stream.of(
          "a) %keyvault:storeA/name2%",
          "c) %keyvault:storeB/name%",
          "b) %keyvault:storeA/name1%"
    )

    val refs = TeamCityVariableRefs.searchRefs(paramValues)

    assertThat(refs).containsExactly(
          "keyvault:storeA/name1",
          "keyvault:storeA/name2",
          "keyvault:storeB/name"
    )
  }
}
