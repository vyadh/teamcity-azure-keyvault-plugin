package com.github.vyadh.teamcity.keyvault.agent

import com.github.vyadh.teamcity.keyvault.common.KeyVaultRef
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.Request

class AzureKeyVaultConnector(
      private val baseUrl: String = defaultBaseUrl) : KeyVaultConnector {

  companion object {
    const val defaultBaseUrl = "https://$(instance).vault.azure.net"
    val client = OkHttpClient()
  }

  override fun requestValue(ref: KeyVaultRef, accessToken: String): SecretResponse {
    val url = baseUrl.replace("$(instance)", ref.instance)
    val endpoint = "$url/secrets/${ref.name}?api-version=2016-10-01"

    val request = Request.Builder()
          .get()
          .url(endpoint)
          .header("Authorization", "Bearer $accessToken")
          .header("Accept", "application/json")
          .build()

    return executeRequest(request)
  }

  //todo dup
  private fun executeRequest(request: Request): SecretResponse {
    val moshi = Moshi.Builder().build()
    val adapter = moshi.adapter(SecretResponse::class.java)

    val result = client.newCall(request).execute().use { response ->
      val body = response.body()!!.source()
      adapter.fromJson(body)
    }

    if (result == null) {
      throw IllegalArgumentException("Could not fetch token")
    } else {
      return result
    }
  }

}
