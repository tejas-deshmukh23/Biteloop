package com.tiffin.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the Razorpay Java SDK client.
 *
 * RazorpayClient is the entry point for all Razorpay API calls:
 * - Creating orders
 * - Fetching payment details
 * - Initiating refunds
 *
 * Created as a Spring Bean so it can be injected anywhere needed.
 * Initialized once at startup with API credentials.
 */
@Configuration
public class RazorpayConfig {

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        return new RazorpayClient(keyId, keySecret);
    }
}