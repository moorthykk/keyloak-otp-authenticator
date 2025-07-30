package com.sanserve.keycloak.authenticators.chooser;

import com.maxmind.geoip2.DatabaseReader;
import com.sanserve.keycloak.authenticators.util.GeoIPCache;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;

import java.io.File;
import java.io.IOException;
@Slf4j
public class ChooseMethodAuthenticator implements Authenticator {
    public static final String OTP_METHOD_NOTE = "otp_method";
    public static String getClientIp(AuthenticationFlowContext context) {
        String ip = context.getHttpRequest().getHttpHeaders().getHeaderString("X-Forwarded-For");
        if (ip == null) {
            ip = context.getConnection().getRemoteAddr();
        } else {
            ip = ip.split(",")[0]; // if multiple IPs
        }
        return ip;
    }
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        log.debug("Entering authenticate");
        String country = null;
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        String dbLocation = config.getConfig().get(ChooseMethodAuthenticatorFactory.GEOLITE_DB_LOCATION);
        Boolean isPhoneOtpEnabledByDefault =Boolean.parseBoolean( config.getConfig()
                .getOrDefault(ChooseMethodAuthenticatorFactory.ENABLE_PHONE_DEFAULT,"false"));
        try {


            String clientIp = getClientIp(context);
             country = GeoIPCache.getInstance(dbLocation).getCountry(clientIp);
        }catch (Exception e){
            log.warn("System could not determine the location");
        }
        log.debug("Country : {}",country);

        // Decide the method based on country
        if ("IN".equalsIgnoreCase(country)||isPhoneOtpEnabledByDefault) {
            Response challenge = context.form()

                    .createForm("choose-method.ftl");
            context.challenge(challenge);
        } else {
            log.info("Country : {}",country);
            context.getAuthenticationSession().setAuthNote(OTP_METHOD_NOTE, "email");
            context.success();
        }

  // Execution continues to next flow
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String selectedMethod = formData.getFirst("method");

        if (selectedMethod == null || (!selectedMethod.equals("email") && !selectedMethod.equals("phone"))) {
            context.challenge(
                    context.form()
                            .setError("Invalid method selected.")
                            .createForm("choose-method.ftl")
            );
            return;
        }
        log.debug("selectedMethod : {}",selectedMethod);
        // Store user's choice in the authentication session
        AuthenticationSessionModel session = context.getAuthenticationSession();
        session.setAuthNote(OTP_METHOD_NOTE, selectedMethod);

        // Proceed to next step
        context.success();
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
