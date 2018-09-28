package com.github.vyadh.teamcity.keyvault.server

import org.mockito.Mockito

object KotlinMockitoMatchers {

  fun <T> any(): T {
    Mockito.anyObject<T>()
    return uninitialized()
  }

  private fun <T> uninitialized(): T = null as T

}
