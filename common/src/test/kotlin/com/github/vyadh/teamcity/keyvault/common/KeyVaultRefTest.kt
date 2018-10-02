package com.github.vyadh.teamcity.keyvault.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KeyVaultRefTest {

  @Test
  internal fun originalReferenceAvailable() {
    val ref = "keyvault:store/name"

    assertThat(KeyVaultRef(ref).ref).isEqualTo(ref)
  }

  @Test
  internal fun instanceName() {
    val ref = KeyVaultRef("keyvault:instance/keyname")

    assertThat(ref.instance).isEqualTo("instance")
  }

  @Test
  internal fun keyName() {
    val ref = KeyVaultRef("keyvault:instance/keyname")

    assertThat(ref.name).isEqualTo("keyname")
  }

  @Test
  internal fun validatity() {
    assertThat(KeyVaultRef("keyvault:a/b").valid()).isTrue()
    assertThat(KeyVaultRef("keyvault=a/b").valid()).isFalse()
    assertThat(KeyVaultRef("keyvault:a-b").valid()).isFalse()
    assertThat(KeyVaultRef("keyvault/a:b").valid()).isFalse()
  }

}
