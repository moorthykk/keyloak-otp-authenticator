package com.sanserve.keycloak.authenticators.smsotp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanserve.keycloak.authenticators.smsotp.dto.SMSSendRequest;
import com.sanserve.keycloak.authenticators.smsotp.dto.SMSToken;
import com.sanserve.keycloak.authenticators.smsotp.dto.SmsTokenRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Authenticator;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SMSService {
    public static final String TOKEN_ID_VALUE = "1";
    private String smsBaseUrl;
    private String serviceId;
    private String userId;
    private String password;
    private String header;
    private String entityId;
    private String contentTemplateId;
    private String appName;
    private HttpRequest tokenFetchRequest=null;
    private HttpClient restClient;
    private static String token;
    public SMSService(String smsBaseUrl,
                      String serviceId,
                      String userId,
                      String password,
                      String header,
                      String entityId,
                      String contentTemplateId,
                      String appName) {
        this.smsBaseUrl = smsBaseUrl;
        this.serviceId = serviceId;
        this.userId = userId;
        this.password = password;
        this.header = header;
        this.entityId = entityId;
        this.contentTemplateId = contentTemplateId;
        this.appName = appName;
        this.restClient = HttpClient.newHttpClient();
    }

    private  void createTokenFetchRequest() throws JsonProcessingException {
        if(tokenFetchRequest ==null) {
            SmsTokenRequest tokenRequest = new SmsTokenRequest();
            tokenRequest.setService_Id(serviceId);
            tokenRequest.setUsername(userId);
            tokenRequest.setPassword(password);
            tokenRequest.setToken_Id(TOKEN_ID_VALUE);
            ObjectMapper objectMapper = new ObjectMapper();
            byte[] bytes = objectMapper.writeValueAsBytes(tokenRequest);
            this.tokenFetchRequest = HttpRequest.newBuilder()
                    .uri(URI.create(smsBaseUrl + "/api/Create_New_API_Token"))
                    .timeout(Duration.ofMinutes(1))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                    .build();
        }
    }
    private  boolean validateTokenRequest() throws IOException, InterruptedException {
          if(token ==null){
              return false;
          }

        ObjectMapper objectMapper = new ObjectMapper();
        HttpRequest tokenFetchRequest = HttpRequest.newBuilder()
                .uri(URI.create(smsBaseUrl + "/api/Get_Token_Status"))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString("{\"Token_Id\":\"" + TOKEN_ID_VALUE + "\"}"))
                .build();
        HttpResponse<String> tokenResponse = restClient.send(tokenFetchRequest, HttpResponse.BodyHandlers.ofString());
        if (tokenResponse.statusCode() == 200) {
            SMSToken smsToken = objectMapper.readValue(tokenResponse.body(), SMSToken.class);
            if(smsToken.getError() == null){
                if(Integer.valueOf(smsToken.getValidity_Left_In_Days())>0) {
                    return true;
                }else{
                    return false;
                }
            }else{
                log.error("SMS API error response {}", smsToken.getError());
                throw new IOException(smsToken.getError());
            }

        }else{
            log.error("SMS API status error {}", tokenResponse.toString());
            throw new IOException(tokenResponse.toString());
        }

    }


    private String getOrCreateToken() throws IOException, InterruptedException {
        createTokenFetchRequest();

        if(!validateTokenRequest()) {

            HttpResponse<String> tokenResponse = restClient.send(tokenFetchRequest, HttpResponse.BodyHandlers.ofString());
            if(tokenResponse.statusCode()==200) {
                String stoken = tokenResponse.body();
                token = stoken.replace("\"", "");

            }
        }

        return token;
    }
    public String sendOTP(String phoneNumber, String otp) throws IOException, InterruptedException {
        SMSSendRequest smsSendRequest = getSmsSendRequest(phoneNumber, otp);
        ObjectMapper objectMapper = new ObjectMapper();
        HttpRequest tokenFetchRequest = HttpRequest.newBuilder()
                .uri(URI.create(smsBaseUrl + "/api/Send_SMS"))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + getOrCreateToken())
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(smsSendRequest)))
                .build();
        HttpResponse<String> otpResponse = restClient.send(tokenFetchRequest, HttpResponse.BodyHandlers.ofString());

        if(otpResponse.statusCode()== 200 ){
          return otp;
        }else{
            log.warn("Message send failed for {} with Msg : {}",phoneNumber,otpResponse.body());
            throw new RuntimeException(otpResponse.body());
        }

    }

    private SMSSendRequest getSmsSendRequest(String phoneNumber, String otp) {
        SMSSendRequest smsSendRequest = new SMSSendRequest();
        smsSendRequest.setHeader(header);
        smsSendRequest.setTarget(phoneNumber);
        smsSendRequest.setEntity_Id(entityId);
        smsSendRequest.setContent_Template_Id(contentTemplateId);

        ArrayList<Map<String,String>> list = new ArrayList<>(2);
        Map<String,String> appMap = new HashMap<>(2);
        appMap.put("Key","appName");
        appMap.put("Value",appName);
        list.add(appMap);
        Map<String,String> otpMap = new HashMap<>(2);
        otpMap.put("Key","otp");
        otpMap.put("Value", otp);
        list.add(otpMap);
        smsSendRequest.setTemplate_Keys_and_Values(list);
        return smsSendRequest;
    }
}
