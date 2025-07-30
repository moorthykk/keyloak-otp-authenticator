package com.sanserve.keycloak.authenticators.smsotp;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Arrays;
import java.util.List;

public class SMSOTPAuthenticatorFactory implements AuthenticatorFactory {
  public static final String CONFIG_PROP_LENGTH = "length";
  public static final String CONFIG_PROP_TTL = "ttl";
  public static final String CONFIG_PROP_MAX_RETRIES = "maxRetries";
  public static final String CONFIG_PROP_SIMULATION = "simulation";
  public static final String CONFIG_PROP_BSNL_BASE_URL = "baseUrl";
  public static final String CONFIG_PROP_BSNL_SERVICE_ID = "serviceId";
  public static final String CONFIG_PROP_BSNL_LOGIN_ID = "loginid";
  public static final String CONFIG_PROP_BSNL_PASSCODE = "passcode";
  public static final String CONFIG_PROP_BSNL_SMS_HEADER = "smsHeader";
  public static final String CONFIG_PROP_BSNL_DLT_ENTITY = "entityId";
  public static final String CONFIG_PROP_BSNL_DLT_TEMPLATE_ID = "templateId";
  public static final String CONFIG_PROP_BSNL_APP_NAME = "appName";

  @Override
  public String getId() {
    return "bsnlotp-authenticator";
  }

  @Override
  public String getDisplayType() {
    return "BSNL Phone SMS TOTP Authentication";
  }

  @Override
  public String getHelpText() {
    return "Validates a TOTP sent via phone number.";
  }

  @Override
  public String getReferenceCategory() {
    return "otp";
  }

  @Override
  public boolean isConfigurable() {
    return true;
  }

  @Override
  public boolean isUserSetupAllowed() {
    return false;
  }

  @Override
  public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
    return new AuthenticationExecutionModel.Requirement[] {
        AuthenticationExecutionModel.Requirement.REQUIRED,
        AuthenticationExecutionModel.Requirement.ALTERNATIVE,
        AuthenticationExecutionModel.Requirement.DISABLED,
    };
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return Arrays.asList(
        new ProviderConfigProperty(CONFIG_PROP_SIMULATION, "Simulation mode",
            "In simulation mode, the email won't be sent, but printed to the server logs.",
            ProviderConfigProperty.BOOLEAN_TYPE, true),
        new ProviderConfigProperty(CONFIG_PROP_BSNL_BASE_URL, "Base Url",
            "Rest API Base url for BSNL", ProviderConfigProperty.STRING_TYPE,
            ""),
        new ProviderConfigProperty(CONFIG_PROP_BSNL_SERVICE_ID, "Service Id", "Your service Id registered in BSNL",
            ProviderConfigProperty.STRING_TYPE, ""),
        new ProviderConfigProperty(CONFIG_PROP_BSNL_LOGIN_ID, "Login User name",
            "Bsnl bulksms user name ", ProviderConfigProperty.STRING_TYPE, ""),
        new ProviderConfigProperty(CONFIG_PROP_BSNL_PASSCODE, "Password",
            "Bsnl bulksms passcode", ProviderConfigProperty.PASSWORD,
            ""),
        new ProviderConfigProperty(CONFIG_PROP_BSNL_SMS_HEADER, "SMS Header",
            "SMS header registered in DLT", ProviderConfigProperty.STRING_TYPE, ""),
        new ProviderConfigProperty(CONFIG_PROP_BSNL_DLT_ENTITY, "DLT Entity ID",
            "Enter your entity id approved in dlt provider", ProviderConfigProperty.STRING_TYPE, ""),
        new ProviderConfigProperty(CONFIG_PROP_BSNL_DLT_TEMPLATE_ID, "Template Id", "Provide template Id registered in DLT",
            ProviderConfigProperty.STRING_TYPE, ""),
            new ProviderConfigProperty(CONFIG_PROP_BSNL_APP_NAME, "Application Name", "Name of the Application(optional), if it is used in template",
                    ProviderConfigProperty.STRING_TYPE, ""),
    new ProviderConfigProperty(CONFIG_PROP_LENGTH, "Code length", "The number of digits of the generated code.",
            ProviderConfigProperty.STRING_TYPE, 6),
    new ProviderConfigProperty(CONFIG_PROP_TTL, "Time-to-live",
            "The time to live in seconds for the code to be valid.", ProviderConfigProperty.STRING_TYPE, "300"),
            new ProviderConfigProperty(CONFIG_PROP_MAX_RETRIES, "Max Retries",
                    "This is the maximum number of retries, after the 1st attempt, before failing.", ProviderConfigProperty.STRING_TYPE,
                    5)

    );

  }

  @Override
  public Authenticator create(KeycloakSession session) {

    return new SMSOTPAuthenticator();
  }

  @Override
  public void init(Config.Scope config) {
  }

  @Override
  public void postInit(KeycloakSessionFactory factory) {
  }

  @Override
  public void close() {
  }

}
