package com.github.vyadh.teamcity.keyvault.server

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TokenResponse(
      @Json(name = "access_token") val accessToken: String)
