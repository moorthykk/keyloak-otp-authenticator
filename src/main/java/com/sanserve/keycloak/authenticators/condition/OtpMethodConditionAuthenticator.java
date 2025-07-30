package com.sanserve.keycloak.authenticators.condition;

import com.sanserve.keycloak.authenticators.chooser.ChooseMethodAuthenticator;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

@Slf4j
public class OtpMethodConditionAuthenticator implements ConditionalAuthenticator {


    @Override
    public boolean matchCondition(AuthenticationFlowContext context) {
        // Read expected value from config
        String expectedMethod = context.getAuthenticatorConfig().getConfig().get("expectedOtpMethod");
        String actualMethod = context.getAuthenticationSession().getAuthNote(ChooseMethodAuthenticator.OTP_METHOD_NOTE);
        log.debug("Attempting authenticate : {}",actualMethod);

        return expectedMethod != null && expectedMethod.equalsIgnoreCase(actualMethod);
    }

    /**
     * Called from a form action invocation.
     *
     * @param context
     */
    @Override
    public void action(AuthenticationFlowContext context) {

    }

    /**
     * Does this authenticator require that the user has already been identified?  That AuthenticatorContext.getUser() is not null?
     *
     * @return
     */
    @Override
    public boolean requiresUser() {
        return false;
    }

    /**
     * Set actions to configure authenticator
     *
     * @param session
     * @param realm
     * @param user
     */
    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public void close() {

    }


}
