package com.github.vyadh.teamcity.keyvault.agent

import com.github.vyadh.teamcity.keyvault.common.KeyVaultException
import com.github.vyadh.teamcity.keyvault.common.KeyVaultRef
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Duration

class AzureKeyVaultConnector(
      private val baseUrl: String = defaultBaseUrl) : KeyVaultConnector {

  companion object {
    const val defaultBaseUrl = "https://$(instance).vault.azure.net"
    val client = createHttpClient()

    private fun createHttpClient(): OkHttpClient {
      return OkHttpClient().newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .readTimeout(Duration.ofSeconds(30))
            .build()
    }
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

  //todo dup AzureTokenConnector
  private fun executeRequest(request: Request): SecretResponse {
    val moshi = Moshi.Builder().build()
    val adapter = moshi.adapter(SecretResponse::class.java)

    val token = client.newCall(request).execute().use { response ->
      if (response.isSuccessful) {
        val body = response.body()!!.source()
        adapter.fromJson(body)
      } else {
        throw KeyVaultException("Could not fetch secret, received " +
              "response code ${response.code()} for url: ${request.url()}")
      }
    }

    if (token == null) {
      throw KeyVaultException("Could not fetch secret")
    } else {
      return token
    }
  }

}
