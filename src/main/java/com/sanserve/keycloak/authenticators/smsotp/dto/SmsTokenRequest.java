package com.sanserve.keycloak.authenticators.smsotp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SmsTokenRequest {
    @JsonProperty("Service_Id")
    String Service_Id;
    @JsonProperty("Username")
    String Username;
    @JsonProperty("Password")
    String Password;
    @JsonProperty("Token_Id")
    String Token_Id;
    @JsonProperty("IP_Addresses")
    List<String> IP_Addresses;
}
