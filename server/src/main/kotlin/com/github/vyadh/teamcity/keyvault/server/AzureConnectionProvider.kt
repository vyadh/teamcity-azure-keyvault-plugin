package com.github.vyadh.teamcity.keyvault.server

import com.github.vyadh.teamcity.keyvault.common.KeyVaultConstants
import com.github.vyadh.teamcity.keyvault.common.KeyVaultFeatureSettings
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.oauth.OAuthConnectionDescriptor
import jetbrains.buildServer.serverSide.oauth.OAuthProvider
import jetbrains.buildServer.web.openapi.PluginDescriptor

class AzureConnectionProvider(private val descriptor: PluginDescriptor) : OAuthProvider() {

  override fun getType(): String = KeyVaultConstants.FEATURE_TYPE

  override fun getDisplayName(): String = "Azure Key Vault"

  override fun describeConnection(connection: OAuthConnectionDescriptor): String {
    val settings = KeyVaultFeatureSettings.fromMap(connection.parameters)
    return "Connection to Azure KeyVault server for ${settings.resourceUri}"
  }

  override fun getDefaultProperties(): Map<String, String> {
    return mapOf(KeyVaultConstants.RESOURCE_URI to KeyVaultConstants.DEFAULT_RESOURCE_URI
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
            KeyVaultConstants.TENANT_ID,
            KeyVaultConstants.CLIENT_ID,
            KeyVaultConstants.CLIENT_SECRET,
            KeyVaultConstants.RESOURCE_URI)

      val errors = keys.mapNotNull {
        if (properties[it].isNullOrBlank()) InvalidProperty(it, "Should not be empty")
        else null
      }

      val settings = KeyVaultFeatureSettings.fromMap(properties)
      properties.putAll(settings.toMap())

      return errors.toMutableList()
    }
  }

}
