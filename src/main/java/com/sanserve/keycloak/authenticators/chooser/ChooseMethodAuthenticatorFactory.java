package com.sanserve.keycloak.authenticators.chooser;

import com.sanserve.keycloak.authenticators.emailinput.EmailInputAuthenticator;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

public class ChooseMethodAuthenticatorFactory implements AuthenticatorFactory {

    public static final String ID = "emailOrphoneChooserauthenticator";
    public static final String GEOLITE_DB_LOCATION = "geoliteDBLocation";
    public static final String ENABLE_PHONE_DEFAULT = "enablePhoneDefault";

    @Override
    public Authenticator create(KeycloakSession session) {
        return new ChooseMethodAuthenticator();
    }

    @Override
    public void init(org.keycloak.Config.Scope config) {}

    @Override
    public void postInit(KeycloakSessionFactory factory) {}

    @Override
    public void close() {}

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayType() {
        return "Choose Email Or Phone Login";
    }

    @Override
    public String getHelpText() {
        return "Choose Email Or Phone Login";
    }

    @Override
    public String getReferenceCategory() {
        return "choose";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of(
                new ProviderConfigProperty(GEOLITE_DB_LOCATION, "Geo lite db file location",
                        "Geo lite db file location",
                        ProviderConfigProperty.STRING_TYPE, ""),
                new ProviderConfigProperty(ENABLE_PHONE_DEFAULT, "Enable phone OTP by default",
                        "Enable phone OTP by default for User testing",
                        ProviderConfigProperty.BOOLEAN_TYPE, false)
        );
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
            AuthenticationExecutionModel.Requirement.DISABLED
        };
    }
}
