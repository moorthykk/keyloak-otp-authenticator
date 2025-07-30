package com.sanserve.keycloak.authenticators.smsotp;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.sanserve.keycloak.authenticators.util.UserUtils;
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

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sanserve.keycloak.authenticators.phoneinput.PhoneNumberInputAuthenticator.PHONE_NUMBER_NOTE;
import static com.sanserve.keycloak.authenticators.util.UserUtils.generateOtp;

@Slf4j
public class SMSOTPAuthenticator implements Authenticator {

  private static final String OTP_FORM = "otp-form.ftl";
  private static final String TOTP_EMAIL = "totp-email.ftl";
  private static final String AUTH_NOTE_CODE = "code";
  private static final String AUTH_NOTE_TTL = "ttl";
  private static final String AUTH_NOTE_REMAINING_RETRIES = "remainingRetries";

  private static final String ALPHA_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String ALPHA_LOWER = "abcdefghijklmnopqrstuvwxyz";
  private static final String NUM = "0123456789";

  private SMSService smsService;
  public  SMSOTPAuthenticator() {

  }
  @Override
  public void authenticate(AuthenticationFlowContext context) {
    AuthenticatorConfigModel config = context.getAuthenticatorConfig();
    KeycloakSession session = context.getSession();
    UserModel user = context.getUser();

    int ttl = Integer.parseInt(config.getConfig().get(SMSOTPAuthenticatorFactory.CONFIG_PROP_TTL));
    String smsBaseUrl = config.getConfig().get(SMSOTPAuthenticatorFactory.CONFIG_PROP_BSNL_BASE_URL);
    String serviceId =  config.getConfig().get(SMSOTPAuthenticatorFactory.CONFIG_PROP_BSNL_SERVICE_ID);
    String loginId = config.getConfig().get(SMSOTPAuthenticatorFactory.CONFIG_PROP_BSNL_LOGIN_ID);
    String passcode = config.getConfig().get(SMSOTPAuthenticatorFactory.CONFIG_PROP_BSNL_PASSCODE);
    String header = config.getConfig().get(SMSOTPAuthenticatorFactory.CONFIG_PROP_BSNL_SMS_HEADER);
    String entity = config.getConfig().get(SMSOTPAuthenticatorFactory.CONFIG_PROP_BSNL_DLT_ENTITY);
    String template = config.getConfig().get(SMSOTPAuthenticatorFactory.CONFIG_PROP_BSNL_DLT_TEMPLATE_ID);
    String appName = config.getConfig().get(SMSOTPAuthenticatorFactory.CONFIG_PROP_BSNL_APP_NAME);

    Boolean isSimulation = Boolean.parseBoolean(
        config.getConfig()
            .getOrDefault(
                SMSOTPAuthenticatorFactory.CONFIG_PROP_SIMULATION,
                "false"));

    String code = getCode(config);
    int maxRetries = getMaxRetries(config);
    AuthenticationSessionModel authSession = context.getAuthenticationSession();
    String phoneNumber = authSession.getAuthNote(PHONE_NUMBER_NOTE);
    authSession.setAuthNote(AUTH_NOTE_CODE, code);
    authSession.setAuthNote(AUTH_NOTE_TTL, Long.toString(System.currentTimeMillis() + (ttl * 1000L)));
    authSession.setAuthNote(AUTH_NOTE_REMAINING_RETRIES, Integer.toString(maxRetries));

    try {
      RealmModel realm = context.getRealm();

      if (isSimulation) {
        log.warn(String.format(
            "***** SIMULATION MODE ***** Would send a TOTP phone to %s with code: %s",
                phoneNumber,
            code));
      } else {
        log.debug("Login user : {}",phoneNumber);
        String realmName = Strings.isNullOrEmpty(realm.getDisplayName()) ? realm.getName() : realm.getDisplayName();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("code", code);
        attributes.put("ttl", Math.floorDiv(ttl, 60));
        this.smsService= new SMSService(smsBaseUrl,serviceId,loginId,passcode,header,entity,template,appName);
        smsService.sendOTP(phoneNumber,code);
      }
      Response response = context.form().setAttribute("realm", context.getRealm())
                .setAttribute("inputValue",phoneNumber)
              .createForm(OTP_FORM);
      context.challenge(response);
    } catch (Exception e) {
      log.error("An error occurred when attempting to email an TOTP auth:", e);
      context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
          context.form().setError("BsnlSmsOTPEmailNotSent", e.getMessage())
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
      log.warn("Code and ttl are not found in auth context ");
      context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
          context.form().createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
      return;
    }

    boolean isValid = enteredCode.equals(code);
    if (isValid) {
      if (Long.parseLong(ttl) < System.currentTimeMillis()) {
        // expired
        context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE,
                context.form().setError("BsnlSmsOTPCodeExpired")
                        .setAttribute("displayGoBack",Boolean.TRUE)
                        .createErrorPage(Response.Status.BAD_REQUEST));
      } else {
        // valid
        String phoneNumber = authSession.getAuthNote(PHONE_NUMBER_NOTE);
        UserModel userModel = UserUtils.getOrCreateUser(context, phoneNumber,null);
        context.setUser(userModel);
        log.info("User otp validated successfully for user - {}",phoneNumber);
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
                .setError("BsnlSmsOTPCodeInvalid", Integer.toString(remainingAttempts))
                    .setAttribute("displayGoBack",Boolean.TRUE)
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
                SMSOTPAuthenticatorFactory.CONFIG_PROP_MAX_RETRIES,
                "3"));
    return maxRetries;
  }

  private String getCode(AuthenticatorConfigModel config) {
    int length = Integer.parseInt(config.getConfig().get(SMSOTPAuthenticatorFactory.CONFIG_PROP_LENGTH));
    return generateOtp(length);

  }



}
