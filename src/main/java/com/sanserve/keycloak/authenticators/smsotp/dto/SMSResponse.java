package com.sanserve.keycloak.authenticators.smsotp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SMSResponse {
    @JsonProperty("Error")
    private String error;
    @JsonProperty("Message_Id")
    private String Message_Id;
}
