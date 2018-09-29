package com.github.vyadh.teamcity.keyvault.server

import org.mockito.Mockito

/**
 * Mockito-Kotlin compatibility class, a hat-tip to Sergey:
 * https://stackoverflow.com/questions/30305217/is-it-possible-to-use-mockito-in-kotlin
 */
object KotlinMockitoMatchers {

  fun <T> any(): T {
    Mockito.anyObject<T>()
    return uninitialized()
  }

  @Suppress("UNCHECKED_CAST")
  private fun <T> uninitialized(): T = null as T

}
