[![Build Status](https://travis-ci.org/vyadh/teamcity-azure-keyvault-plugin.svg?branch=master)](https://travis-ci.org/vyadh/teamcity-azure-keyvault-plugin)

Azure Key Vault TeamCity Plugin
===============================

A plugin to TeamCity (>= 2018.1) to integrate with Azure Key Vault to make
managing secrets in TeamCity easier and more secure.

Many thanks to JetBrains for their [Hashicorp Vault plugin][1] which provided
a lot of guidance on how to accomplish similar functionality with Azure Key Vault.
While this plugin was written from scratch, it would have been far more difficult
to implement without it.

Also thanks to RodM for the [TeamCity Gradle Plugin][2] which provided a great
head start to development.


Getting Started
---------------

1. Install plugin from the [TeamCity plugin repository][3].
2. The Azure Key Vault plugin is installed as a TeamCity 'connection'.
   Go to the administrator screen for the desired project, then to 'Connections'.
3. Add a connection, specify Azure Key Vault, and fill in the fields as described.
4. Click Save. This connection will be available for all build configurations
   in this project and below.
3. Define paths to secrets within the build parameters as required.
   See format below.


Secret Variable Format
----------------------

In order to reference Key Vault secrets, use the following within other build
parameters:

```
  %keyvault:<vault name>/<secret name>%
```

Where `<vault>` is the name of the required Key Vault, the same name used in
the normal Key Vault URL `https://<vault name>.vault.azure.net`.


How the plugin works...
-----------------------

1. When a build is triggered on the TeamCity server, the plugin requests an
   access token from Azure AD, limited to the Key Vault resource. This allows
   fetching secrets from any vault within the tenant that the configured
   service principal has access to.
   
2. The access token is encoded as a build parameter 'password' and provided to
   build agents.

3. When a build starts on an agent and before any steps are executed, the
   access token is read into memory and then removed so not accessible from
   the build steps themselves.
   
4. References to Key Vault secrets in build parameters are used to query the
   respective Key Vaults concerned.
    
5. The secrets obtained from Key Vault are then populated as passwords for
   the build. This ensures that any inadvertent exposure in build logs will
   be redacted.
   

Azure Key Vault Limitations
---------------------------

Azure Key Vault is currently a limited-functionality service. The following
describes some of the challenges, and how the plugin tries to mitigate them.

**No real RBAC (Role-Based Access Control) in Azure Key Vault.** Access control
is applied at the vault level, so multiple Key Vaults are required in order to
control access between secrets. To make this more manageable in the plugin, the
specific vault required is specified as part of the path to the secret.

**Limited access token validity functionality in Azure AD.** The access token
from Azure AD lasts for an hour by default and this validity period cannot be
changed from the requesting entity. Tasks that run only for a few seconds
cannot indicate they only need a token valid for a couple of minutes they will
always receive the configured default.
 
**No support for one-time use tokens from Azure AD.** The access token can be
used many times. To mitigate this risk, the plugin removes the token from the
build parameters sent to an agent before the build starts. It is used for a
limited time in memory to fetch the secrets and is not accessible from build
steps.


Azure Service Principal Configuration Tips
------------------------------------------

To limit the impact of a compromised Azure service principal used for the Key
Vault plugin, ensure it is configured to only access the required vaults with
no access to other resources.

To mitigate access to secrets if the service principal is exposed, ensure it
has an access policy limited to the 'Get' operation on each vault. This will
ensure secret names cannot be listed and therefore slightly more difficult for
an attacker to access the secret values.

Store keys and certificates in separate key vault instances to the ones used
for automation secrets in TeamCity. While this can be controlled by RBAC in
Azure it is better to use a service principal dedicated to automation secrets.


Possible Future Features
------------------------

Also known as 'not currently supported'.

* Support TeamCity proxy configuration parameters.
* Support accessing Key Vault instances with separate Azure service principals.
  While one vault 'connection' in TeamCity can access multiple Key Vault
  instances, sometimes it's useful to have more flexible access control within
  TeamCity itself.
  For example adding a root-level Key Vault connection with general secrets
  that are accessed by multiple teams, and then team-specific connections placed
  further down the TeamCity project hierarchy that have access to segregated
  secrets only permitted to that team.
* An option or separate connector to allow using the Azure AD token in
  build steps directly.
* Azure Managed Identity support for requesting access tokens. This would
  have to be configurable so that the TeamCity Server can still be run
  on-premise (useful for cloud environment bootstrap).
* Storing the connector service principal credentials (client secret) in OS
  keyring rather than as a TeamCity secret. Probably best implemented as a
  separate plugin.


[1]: https://github.com/JetBrains/teamcity-hashicorp-vault-plugin
[2]: https://github.com/rodm/gradle-teamcity-plugin
[3]: https://plugins.jetbrains.com/teamcity
