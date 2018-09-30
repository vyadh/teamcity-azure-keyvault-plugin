<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/include-internal.jsp" %>

<jsp:useBean id="project" type="jetbrains.buildServer.serverSide.SProject" scope="request"/>
<jsp:useBean id="oauthConnectionBean" type="jetbrains.buildServer.serverSide.oauth.OAuthConnectionBean" scope="request"/>
<jsp:useBean id="propertiesBean" type="jetbrains.buildServer.serverSide.oauth.OAuthConnectionBean" scope="request"/>
<jsp:useBean id="keys" class="com.github.vyadh.teamcity.keyvault.common.AzureTokenJspKeys"/>

<tr>
  <td><label for="displayName">Display name:</label><l:star/></td>
  <td>
    <props:textProperty name="displayName" className="longField"/>
    <span class="smallNote">Use a name to distinguish this connection from others.</span>
    <span class="error" id="error_displayName"></span>
  </td>
</tr>

<tr>
  <td><label for="${keys.TENANT_ID}">Azure Tenant Id:</label></td>
  <td>
    <props:textProperty name="${keys.TENANT_ID}" className="longField textProperty_max-width js_max-width"/>
    <span class="error" id="error_${keys.TENANT_ID}"/>
    <span class="smallNote">Azure Tenant UUID.
      Format: 00000000-0000-0000-0000-000000000000</span>
  </td>
</tr>

<tr class="advancedSetting">
  <td><label for="${keys.CLIENT_ID}">Client id:</label></td>
  <td>
    <props:textProperty name="${keys.CLIENT_ID}" className="longField textProperty_max-width js_max-width"/>
    <span class="error" id="error_${keys.CLIENT_ID}"/>
    <span class="smallNote">Also known as service principal name.
      Format: 00000000-0000-0000-0000-000000000000</span>
  </td>
</tr>

<tr class="noBorder">
  <td><label for="${keys.CLIENT_SECRET}">Client secret:</label></td>
  <td>
    <props:passwordProperty name="${keys.CLIENT_SECRET}" className="longField textProperty_max-width js_max-width"/>
    <span class="error" id="error_${keys.CLIENT_SECRET}"/>
    <span class="smallNote">Service principal secret.</span>
  </td>
</tr>

<tr>
  <td><label for="${keys.RESOURCE_URI}">Resource URI:</label></td>
  <td>
    <props:textProperty name="${keys.RESOURCE_URI}" className="longField textProperty_max-width js_max-width"/>
    <span class="error" id="error_${keys.RESOURCE_URI}"/>
    <span class="smallNote">The Azure resource we are requesting the access token for.
      Leave as default to access any Key Vault, or change to get a token for other
      resources (an Azure resource URI or App URI).</span>
  </td>
</tr>
