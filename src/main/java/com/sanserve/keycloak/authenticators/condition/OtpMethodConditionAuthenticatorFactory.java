package com.sanserve.keycloak.authenticators.condition;

import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

public class OtpMethodConditionAuthenticatorFactory implements AuthenticatorFactory {

    public static final String ID = "otp-method-condition-authenticator";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayType() {
        return "Condition - OTP Method (authNote)";
    }

    @Override
    public String getHelpText() {
        return "Executes subflow only if the authNote 'otp_method' matches configured value.";

    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new OtpMethodConditionAuthenticator();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        ProviderConfigProperty expected = new ProviderConfigProperty();
        expected.setName("expectedOtpMethod");
        expected.setLabel("Expected OTP Method");
        expected.setType(ProviderConfigProperty.STRING_TYPE);
        expected.setHelpText("Expected value of 'otp_method' authNote (e.g., 'email', 'phone').");
        return List.of(expected);
    }

    @Override public void init(org.keycloak.Config.Scope config) {}
    @Override public void postInit(org.keycloak.models.KeycloakSessionFactory factory) {}
    @Override public void close() {}
    @Override public boolean isConfigurable() { return true; }

    /**
     * What requirement settings are allowed.
     *
     * @return
     */
    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[] {
                AuthenticationExecutionModel.Requirement.CONDITIONAL,
                AuthenticationExecutionModel.Requirement.REQUIRED,
                AuthenticationExecutionModel.Requirement.DISABLED
        };
    }

    @Override
    public boolean isUserSetupAllowed() { return false; }
    @Override
    public String getReferenceCategory() { return null; }

}
