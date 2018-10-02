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
          "a) %keyvault:storeA/path1%, b) %keyvault:storeA/path2%",
          "c) %keyvault:storeB/path% and that's all"
    )

    val refs = TeamCityVariableRefs.searchRefs(paramValues)

    assertThat(refs).containsExactly(
          "keyvault:storeA/path1",
          "keyvault:storeA/path2",
          "keyvault:storeB/path"
    )
  }

  @Test
  internal fun searchRemovesDuplicates() {
    val paramValues = Stream.of(
          "a) %keyvault:storeA/path%",
          "b) %keyvault:storeA/path%",
          "c) %keyvault:storeB/path%",
          "d) %keyvault:storeB/path%"
    )

    val refs = TeamCityVariableRefs.searchRefs(paramValues)

    assertThat(refs).containsExactly(
          "keyvault:storeA/path",
          "keyvault:storeB/path"
    )
  }

  @Test
  internal fun searchSortsItemsForPossiblyMoreEfficientQueries() {
    val paramValues = Stream.of(
          "a) %keyvault:storeA/path2%",
          "c) %keyvault:storeB/path%",
          "b) %keyvault:storeA/path1%"
    )

    val refs = TeamCityVariableRefs.searchRefs(paramValues)

    assertThat(refs).containsExactly(
          "keyvault:storeA/path1",
          "keyvault:storeA/path2",
          "keyvault:storeB/path"
    )
  }
}
