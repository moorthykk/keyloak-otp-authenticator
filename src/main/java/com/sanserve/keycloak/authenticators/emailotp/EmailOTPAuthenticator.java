package com.sanserve.keycloak.authenticators.emailotp;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.sanserve.keycloak.authenticators.util.UserUtils;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sanserve.keycloak.authenticators.emailinput.EmailInputAuthenticator.EMAIL_ID_NOTE;

public class EmailOTPAuthenticator implements Authenticator {

  private static final String OTP_FORM = "otp-form.ftl";
  private static final String TOTP_EMAIL = "totp-email.ftl";
  private static final String AUTH_NOTE_CODE = "code";
  private static final String AUTH_NOTE_TTL = "ttl";
  private static final String AUTH_NOTE_REMAINING_RETRIES = "remainingRetries";
  private static final Logger logger = Logger.getLogger(EmailOTPAuthenticator.class);

  private static final String ALPHA_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String ALPHA_LOWER = "abcdefghijklmnopqrstuvwxyz";
  private static final String NUM = "0123456789";

  @Override
  public void authenticate(AuthenticationFlowContext context) {
    AuthenticatorConfigModel config = context.getAuthenticatorConfig();
    KeycloakSession session = context.getSession();
    String emailId = context.getAuthenticationSession().getAuthNote(EMAIL_ID_NOTE);


    int ttl = Integer.parseInt(config.getConfig().get(EmailOTPAuthenticatorFactory.CONFIG_PROP_TTL));
    String emailSubject = config.getConfig().get(EmailOTPAuthenticatorFactory.CONFIG_PROP_EMAIL_SUBJECT);
    Boolean isSimulation = Boolean.parseBoolean(
        config.getConfig()
            .getOrDefault(
                EmailOTPAuthenticatorFactory.CONFIG_PROP_SIMULATION,
                "false"));

    String code = getCode(config);
    int maxRetries = getMaxRetries(config);
    AuthenticationSessionModel authSession = context.getAuthenticationSession();
    authSession.setAuthNote(AUTH_NOTE_CODE, code);
    authSession.setAuthNote(AUTH_NOTE_TTL, Long.toString(System.currentTimeMillis() + (ttl * 1000L)));
    authSession.setAuthNote(AUTH_NOTE_REMAINING_RETRIES, Integer.toString(maxRetries));

    try {
      RealmModel realm = context.getRealm();

      if (isSimulation) {
        logger.warn(String.format(
            "***** SIMULATION MODE ***** Would send a TOTP email to %s with code: %s",
                emailId,
            code));
      } else {
        UserModel userModel = UserUtils.getOrCreateUser(context, null,emailId);
        String realmName = Strings.isNullOrEmpty(realm.getDisplayName()) ? realm.getName() : realm.getDisplayName();
        List<Object> subjAttr = ImmutableList.of(realmName);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("code", code);
        attributes.put("ttl", Math.floorDiv(ttl, 60));
        session.getProvider(EmailTemplateProvider.class)
            .setAuthenticationSession(authSession)
            .setRealm(realm)

            .setUser(userModel)
            .setAttribute("realmName", realmName)
            .send(
                emailSubject,
                subjAttr,
                TOTP_EMAIL,
                attributes);
      }

      context.challenge(context.form().setAttribute("realm", context.getRealm())
              .setAttribute("inputValue",emailId)
              .createForm(OTP_FORM));
    } catch (Exception e) {
      logger.error("An error occurred when attempting to email an TOTP auth:", e);
      context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
          context.form().setError("emailTOTPEmailNotSent", e.getMessage())
              .createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));

    }
  }

  @Override
  public void action(AuthenticationFlowContext context) {
    String enteredCode = context.getHttpRequest().getDecodedFormParameters().getFirst("code");

    AuthenticationSessionModel authSession = context.getAuthenticationSession();
    String code = authSession.getAuthNote(AUTH_NOTE_CODE);
    String ttl = authSession.getAuthNote(AUTH_NOTE_TTL);
    String remainingAttemptsStr = authSession.getAuthNote(AUTH_NOTE_REMAINING_RETRIES);
    int remainingAttempts = remainingAttemptsStr == null ? 0 : Integer.parseInt(remainingAttemptsStr);

    if (code == null || ttl == null) {
      context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
          context.form().createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
      return;
    }

    boolean isValid = enteredCode.equals(code);
    if (isValid) {
      if (Long.parseLong(ttl) < System.currentTimeMillis()) {
        // expired
        context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE,
            context.form().setError("emailTOTPCodeExpired").createErrorPage(Response.Status.BAD_REQUEST));
      } else {
        String emailId = context.getAuthenticationSession().getAuthNote(EMAIL_ID_NOTE);
        UserModel userModel = UserUtils.getOrCreateUser(context, null,emailId);
        context.setUser(userModel);
        // valid
        if (!context.getUser().isEmailVerified()) {
          context.getUser().setEmailVerified(true);
        }
        context.success();
      }
    } else {
      // Code is invalid
      if (remainingAttempts > 0) {
        authSession.setAuthNote(AUTH_NOTE_REMAINING_RETRIES, Integer.toString(remainingAttempts - 1));

        // Inform user of the remaining attempts
        context.failureChallenge(
            AuthenticationFlowError.INVALID_CREDENTIALS,
            context.form()
                .setAttribute("realm", context.getRealm())
                .setError("emailTOTPCodeInvalid", Integer.toString(remainingAttempts))
                .createForm(OTP_FORM));
      } else {
        context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
      }
    }
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
  public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
  }

  @Override
  public void close() {
  }

  private int getMaxRetries(AuthenticatorConfigModel config) {
    int maxRetries = Integer.parseInt(
        config.getConfig()
            .getOrDefault(
                EmailOTPAuthenticatorFactory.CONFIG_PROP_MAX_RETRIES,
                "3"));
    return maxRetries;
  }

  private String getCode(AuthenticatorConfigModel config) {
    int length = Integer.parseInt(config.getConfig().get(EmailOTPAuthenticatorFactory.CONFIG_PROP_LENGTH));

    return UserUtils.generateOtp(length);
  }

}
