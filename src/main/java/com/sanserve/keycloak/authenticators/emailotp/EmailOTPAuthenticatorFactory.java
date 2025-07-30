package com.sanserve.keycloak.authenticators.emailotp;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Arrays;
import java.util.List;

public class EmailOTPAuthenticatorFactory implements AuthenticatorFactory {
  public static final String CONFIG_PROP_LENGTH = "length";
  public static final String CONFIG_PROP_TTL = "ttl";
  public static final String CONFIG_PROP_EMAIL_SUBJECT = "emailSubject";
  public static final String CONFIG_PROP_SIMULATION = "simulation";

  public static final String CONFIG_PROP_MAX_RETRIES = "maxRetries";

  @Override
  public String getId() {
    return "email-otp-authenticator";
  }

  @Override
  public String getDisplayType() {
    return "Email OTP Authenticator";
  }

  @Override
  public String getHelpText() {
    return "Validates a OTP sent via email to the users email address.";
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
        new ProviderConfigProperty(CONFIG_PROP_EMAIL_SUBJECT, "Email Subject",
            "The subject of the email that sent to the user.", ProviderConfigProperty.STRING_TYPE,
            "Temporary Authentication Code"),
        new ProviderConfigProperty(CONFIG_PROP_LENGTH, "Code length", "The number of digits of the generated code.",
            ProviderConfigProperty.STRING_TYPE, 6),
        new ProviderConfigProperty(CONFIG_PROP_TTL, "Time-to-live",
            "The time to live in seconds for the code to be valid.", ProviderConfigProperty.STRING_TYPE, "300"),
        new ProviderConfigProperty(CONFIG_PROP_MAX_RETRIES, "Max Retries",
            "This is the maximum number of retries, after the 1st attempt, before failing.", ProviderConfigProperty.STRING_TYPE,
            5));

  }

  @Override
  public Authenticator create(KeycloakSession session) {
    return new EmailOTPAuthenticator();
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
