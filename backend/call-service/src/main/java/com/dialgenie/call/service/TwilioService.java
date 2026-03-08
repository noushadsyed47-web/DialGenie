package com.dialgenie.call.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;

@Service
public class TwilioService {
    private static final Logger logger = LoggerFactory.getLogger(TwilioService.class);

    @Value("${twilio.account-sid:}")
    private String twilioAccountSid;

    @Value("${twilio.auth-token:}")
    private String twilioAuthToken;

    @Value("${twilio.phone-number:}")
    private String twilioPhoneNumber;

    @Value("${twilio.webhook-url:}")
    private String webhookUrl;

    public void initializeTwilio() {
        if (twilioAccountSid != null && !twilioAccountSid.isEmpty()) {
            Twilio.init(twilioAccountSid, twilioAuthToken);
            logger.info("Twilio initialized successfully");
        } else {
            logger.warn("Twilio credentials not configured");
        }
    }

    public String initiateCall(String toPhoneNumber, String greetingMessage) {
        try {
            initializeTwilio();

            String twimlUrl = buildTwimlUrl(greetingMessage);

            Call call = Call.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(twilioPhoneNumber))
                    .setUrl(URI.create(twimlUrl))
                    .setStatusCallbackUrl(URI.create(webhookUrl))
                    .create();

            logger.info("Call initiated with SID: {}", call.getSid());
            return call.getSid();
        } catch (Exception e) {
            logger.error("Failed to initiate call: {}", e.getMessage());
            throw new RuntimeException("Failed to initiate Twilio call", e);
        }
    }

    public void endCall(String callSid) {
        try {
            initializeTwilio();

            Call call = Call.fetcher(callSid)
                    .fetch();

            call.update()
                    .setStatus(Call.Status.COMPLETED)
                    .update();

            logger.info("Call ended: {}", callSid);
        } catch (Exception e) {
            logger.error("Failed to end call: {}", e.getMessage());
        }
    }

    private String buildTwimlUrl(String greetingMessage) {
        // This would return a dynamic TwiML generation URL
        // In production, this should call the AI service to generate dynamic TwiML
        return webhookUrl + "/twiml/greeting?message=" + greetingMessage;
    }
}
