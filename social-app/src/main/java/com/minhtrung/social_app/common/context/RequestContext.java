package com.minhtrung.social_app.common.context;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestContext {
    private String deviceType;
    private String deviceOS;
    private String browser;
    private String ipAddr;
    private String country;
    private String city;
    private UUID userId;
    private UUID sessionId;
    private UUID deviceId;
}
