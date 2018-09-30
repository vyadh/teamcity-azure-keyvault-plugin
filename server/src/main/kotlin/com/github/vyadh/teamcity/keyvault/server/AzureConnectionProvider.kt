package com.github.vyadh.teamcity.keyvault.server

import com.github.vyadh.teamcity.keyvault.common.AzureTokenConstants
import com.github.vyadh.teamcity.keyvault.common.KeyVaultConstants
import com.github.vyadh.teamcity.keyvault.common.TokenRequestSettings
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.oauth.OAuthConnectionDescriptor
import jetbrains.buildServer.serverSide.oauth.OAuthProvider
import jetbrains.buildServer.web.openapi.PluginDescriptor

class AzureConnectionProvider(private val descriptor: PluginDescriptor) : OAuthProvider() {

  override fun getType(): String = KeyVaultConstants.FEATURE_TYPE

  override fun getDisplayName(): String = "Azure Key Vault"

  override fun describeConnection(connection: OAuthConnectionDescriptor): String {
    val settings = TokenRequestSettings.fromMap(connection.parameters)
    return "Connection to Azure KeyVault server for ${settings.resourceUri}"
  }

  override fun getDefaultProperties(): Map<String, String> {
    return mapOf(AzureTokenConstants.RESOURCE_URI to AzureTokenConstants.DEFAULT_RESOURCE_URI
    )
  }

  override fun getEditParametersUrl(): String? {
    return descriptor.getPluginResourcesPath("editConnectionKeyVault.jsp")
  }

  override fun getPropertiesProcessor(): PropertiesProcessor {
    return KeyVaultPropertiesProcessor()
  }

  class KeyVaultPropertiesProcessor : PropertiesProcessor {

    override fun process(properties: MutableMap<String, String>): MutableCollection<InvalidProperty> {
      val keys = listOf(
            AzureTokenConstants.TENANT_ID,
            AzureTokenConstants.CLIENT_ID,
            AzureTokenConstants.CLIENT_SECRET,
            AzureTokenConstants.RESOURCE_URI)

      val errors = keys.mapNotNull {
        if (properties[it].isNullOrBlank()) InvalidProperty(it, "Should not be empty")
        else null
      }

      val settings = TokenRequestSettings.fromMap(properties)
      properties.putAll(settings.toMap())

      return errors.toMutableList()
    }
  }

}
