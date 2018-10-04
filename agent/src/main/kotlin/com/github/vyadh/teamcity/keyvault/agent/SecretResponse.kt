package com.github.vyadh.teamcity.keyvault.agent

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SecretResponse(
      @Json(name = "value") val value: String)
