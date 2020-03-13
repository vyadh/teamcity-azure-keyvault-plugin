package com.github.vyadh.teamcity.keyvault.server

import com.github.vyadh.teamcity.keyvault.common.KeyVaultException
import com.github.vyadh.teamcity.keyvault.common.TokenRequestSettings
import com.squareup.moshi.Moshi
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class AzureTokenConnector(
      private val baseUrl: String = defaultBaseUrl) : TokenConnector {

  companion object {
    const val defaultBaseUrl = "https://login.microsoftonline.com/"
    val client = OkHttpClient()
  }

  override fun requestToken(settings: TokenRequestSettings): TokenResponse {
    val endpoint = "$baseUrl${settings.tenantId}/oauth2/token"

    val form = FormBody.Builder()
          .add("grant_type", "client_credentials")
          .add("client_id", settings.clientId)
          .add("client_secret", settings.clientSecret)
          .add("resource", settings.resourceUri)
          .build()

    val request = Request.Builder()
          .post(form)
          .url(endpoint)
          .header("Accept", "application/json")
          .build()

    return executeRequest(request)
  }

  private fun executeRequest(request: Request): TokenResponse {
    val moshi = Moshi.Builder().build()
    val adapter = moshi.adapter(TokenResponse::class.java)

    val token = client.newCall(request).execute().use { response ->
      if (response.isSuccessful) {
        val body = response.body!!.source()
        adapter.fromJson(body)
      } else {
        throw KeyVaultException("Could not fetch Azure token, received " +
              "response code ${response.code} for url: ${request.url}")
      }
    }

    if (token == null) {
      throw KeyVaultException("Could not fetch Azure token")
    } else {
      return token
    }
  }

}
