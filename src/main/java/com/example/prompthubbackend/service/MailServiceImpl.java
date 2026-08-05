package com.example.prompthubbackend.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements MailService{

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Value("${spring.mail.password:}")
    private String smtpPassword;

    @Override
    public void sendSimpleMail(String to, String subject, String content) {
        validateConfiguration();
        SimpleMailMessage message = new SimpleMailMessage();

        try{
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);

        } catch (MailAuthenticationException exception) {
            throw authenticationFailure(to, exception);
        } catch (Exception exception) {
            throw deliveryFailure(to, exception);
        }

    }

    @Override
    public void sendHtmlMail(String to, String subject, String htmlContent) {
        validateConfiguration();
        try{
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    false,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);


        } catch (MailAuthenticationException exception) {
            throw authenticationFailure(to, exception);
        } catch (Exception exception) {
            throw deliveryFailure(to, exception);
        }
    }

    private void validateConfiguration() {
        if (smtpUsername == null || smtpUsername.isBlank() || smtpPassword == null || smtpPassword.isBlank()) {
            throw new MailDeliveryException(
                    "SMTP is not configured. Set MAIL_USERNAME and MAIL_PASSWORD in the backend process environment."
            );
        }
        if (fromEmail == null || fromEmail.isBlank()) {
            throw new MailDeliveryException("SMTP sender is not configured. Set MAIL_FROM or MAIL_USERNAME.");
        }
    }

    private MailDeliveryException authenticationFailure(String to, Exception exception) {
        log.error("Gmail SMTP authentication failed while sending to {}: {}", to, exception.getMessage(), exception);
        return new MailDeliveryException(
                "Gmail rejected the SMTP credentials. Use the Gmail address as MAIL_USERNAME and a 16-character App Password as MAIL_PASSWORD.",
                exception
        );
    }

    private MailDeliveryException deliveryFailure(String to, Exception exception) {
        log.error("SMTP delivery failed while sending to {}: {}", to, exception.getMessage(), exception);
        return new MailDeliveryException("SMTP could not deliver the invitation email. Check the backend mail logs.", exception);
    }
}
