package com.sanserve.keycloak.authenticators.smsotp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
@Getter
@Setter
public class SMSSendRequest {
    @JsonProperty("Header")
    private String Header;
    @JsonProperty("Target")
    private String Target;
    @JsonProperty("Is_Unicode")
    private String Is_Unicode="0";
    @JsonProperty("Is_Flash")
    private String Is_Flash="0";
    @JsonProperty("Message_Type")
    private String Message_Type= "SI";
    @JsonProperty("Entity_Id")
    private String Entity_Id;
    @JsonProperty("Content_Template_Id")
    private String Content_Template_Id;
    @JsonProperty("Consent_Template_Id")
    private String Consent_Template_Id;
    @JsonProperty("Template_Keys_and_Values")
    private List<Map<String,String>> Template_Keys_and_Values;
}
