package com.aakash.goalkeeper.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Default channel: no SMTP required to run the app locally. */
@Component
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class ConsoleNotificationChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(ConsoleNotificationChannel.class);

    @Override
    public void send(String toEmail, String subject, String body) {
        log.info("[email:console] to={} subject=\"{}\" body=\"{}\"", toEmail, subject, body);
    }
}
