package org.overcode250204.iamservice.services.auth.impls;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.overcode250204.iamservice.services.auth.MailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    public void sendLoginLink(String to, String url) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Login Link - HemoLabManager");

            helper.setText("""
            <div style="font-family: Arial; max-width: 600px; margin: auto;">
                <h2>HemoLabManager – One-Time Login</h2>
                <p>Click the button below to sign in:</p>

                <div style="text-align: center; margin: 30px 0;">
                    <a href="%s"
                       style="background: #007bff; color: white; padding: 12px 25px;
                       text-decoration: none; border-radius: 6px; font-size: 18px;">
                        Login Now
                    </a>
                </div>

                <p>This link is valid for 5 minutes.</p>
            </div>
        """.formatted(url), true);

            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
