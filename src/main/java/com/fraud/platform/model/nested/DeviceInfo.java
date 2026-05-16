package com.fraud.platform.model.nested;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Nested device details used for internal fraud analysis payloads.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfo {

    /**
     * Unique device identifier.
     */
    @NotBlank(message = "Device ID is required")
    private String deviceId;

    /**
     * Device type such as MOBILE, DESKTOP, or TABLET.
     */
    private String deviceType;

    /**
     * Device fingerprint used for identity correlation.
     */
    private String deviceFingerprint;

    /**
     * Indicates whether the device is trusted.
     */
    private Boolean isTrusted;

    /**
     * Source IP address observed for the device.
     */
    private String ipAddress;

    /**
     * Indicates whether VPN usage was detected.
     */
    private Boolean vpnDetected;

    /**
     * Indicates whether proxy usage was detected.
     */
    private Boolean proxyDetected;

    /**
     * Raw user agent string for the device.
     */
    private String userAgent;
}

// Made with Bob