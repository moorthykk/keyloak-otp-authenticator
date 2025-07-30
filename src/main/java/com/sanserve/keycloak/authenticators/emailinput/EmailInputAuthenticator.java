package com.sanserve.keycloak.authenticators.emailinput;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.regex.Pattern;

public class EmailInputAuthenticator implements Authenticator {

    public static final String FORM_EMAIL_FIELD = "email_id";
    public static final String EMAIL_ID_NOTE = "email_id";

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        Response challenge = context.form()
            .createForm("email-input.ftl");  // Custom form template
        context.challenge(challenge);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String email = formData.getFirst(FORM_EMAIL_FIELD);

        if (!isValidEmail(email)) {
            Response challenge = context.form()
                .setError("Invalid e-mail id")
                .createForm("email-input.ftl");
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            return;
        }

        context.getAuthenticationSession().setAuthNote(EMAIL_ID_NOTE, email);
        context.success();
    }

    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
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
