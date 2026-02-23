package com.hanyoonsoo.mfa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class MfaEmailService {
    private final JavaMailSender javaMailSender;

    public void sendMagicLink(String email, String magicLink) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(magicLink)) {
            throw new IllegalArgumentException("email and magicLink must not be blank");
        }

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setTo(email);
            helper.setSubject("[toy-mfa-system] MFA 인증 링크");
            helper.setText(buildHtmlContent(magicLink), true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("MFA 이메일 전송에 실패했습니다.", e);
        }
    }

    private String buildHtmlContent(String magicLink) {
        return """
                <div style="font-family:Arial,sans-serif;line-height:1.5;color:#111827;padding:20px;">
                  <h2 style="margin:0 0 12px;">MFA 인증 요청</h2>
                  <p style="margin:0 0 16px;">아래 버튼을 눌러 인증을 완료하세요.</p>
                  <a href="%s" style="display:inline-block;padding:12px 18px;background:#0f4c81;color:#ffffff;text-decoration:none;border-radius:8px;">
                    MFA 인증하기
                  </a>
                </div>
                """.formatted(magicLink);
    }
}
