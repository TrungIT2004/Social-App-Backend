package com.minhtrung.social_app.dtos;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ClientInfo {
    private String deviceType;
    private String deviceOS;
    private String browser;
    private String ipAddr;
    private String country;
    private String city;
}
