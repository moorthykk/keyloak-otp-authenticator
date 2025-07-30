package com.sanserve.keycloak.authenticators.smsotp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SMSToken {
    @JsonProperty("Error")
    private String Error=null;
    @JsonProperty("Token_Id")
    private String Token_Id;
    @JsonProperty("Validity_Left_In_Days")
    private String Validity_Left_In_Days;
    @JsonProperty("Expiry_Time")
    private String Expiry_Time;

    private String token;
}
