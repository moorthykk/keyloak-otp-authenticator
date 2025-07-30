package com.sanserve.keycloak.authenticators.util;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.*;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.utils.KeycloakModelUtils;

import java.security.SecureRandom;

@Slf4j
public class UserUtils {
    private static SecureRandom secureRandom = new SecureRandom();
    /**
     * Creates or finds a user by phone or email.
     *
     * @param context The authentication flow context
     * @param phone The phone number (can be used as username)
     * @param email The phone number (can be used as username)

     * @return UserModel The created or found user
     */
    public static UserModel getOrCreateUser(AuthenticationFlowContext context, String phone,String email) {
        RealmModel realm = context.getRealm();
        KeycloakSession session = context.getSession();
        String userName=null;
          if(phone != null){
              userName= phone;
          }else if(email != null){
            userName= email;
        }
          if(userName == null){

              return null;
          }
        // Try to find user by username (phone number)
        UserModel user = session.users().getUserByUsername(realm, userName);

        if (user == null) {

            // Create user
            user = session.users().addUser(realm, null,userName,true,true);
            log.info("Creating user : {} , id: {}",user.getUsername(),user.getId());
            user.setEnabled(true);
            user.setUsername(userName);
            if(email != null) {
                user.setEmail(email);
            }
            if(phone != null){
                user.setSingleAttribute("phone_number",phone);
            }

        }

        return user;
    }
    public static String generateOtp(int length) {
        if (length <= 0) throw new IllegalArgumentException("Length must be > 0");
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int digit = secureRandom.nextInt(10); // 0–9

            sb.append(digit);
        }

        return sb.toString();
    }
}
