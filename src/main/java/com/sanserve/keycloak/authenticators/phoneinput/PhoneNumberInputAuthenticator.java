package com.sanserve.keycloak.authenticators.phoneinput;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public class PhoneNumberInputAuthenticator implements Authenticator {

    public static final String FORM_PHONE_FIELD = "phone_number";
    public static final String PHONE_NUMBER_NOTE = "phone_number";

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        Response challenge = context.form()
            .createForm("phone-number.ftl");  // Custom form template
        context.challenge(challenge);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String phone = formData.getFirst(FORM_PHONE_FIELD);

        if (phone == null || !isValidPhoneNumber(phone)) {
            Response challenge = context.form()
                .setError("Invalid phone number")
                .createForm("phone-number.ftl");
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            return;
        }

        context.getAuthenticationSession().setAuthNote(PHONE_NUMBER_NOTE, phone);
        context.success();
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone.matches("^\\+?[1-9]\\d{6,15}$");
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {}

    @Override
    public void close() {}
}
