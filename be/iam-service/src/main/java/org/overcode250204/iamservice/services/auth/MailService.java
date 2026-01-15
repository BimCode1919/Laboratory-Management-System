package org.overcode250204.iamservice.services.auth;

public interface MailService {
    void sendLoginLink(String to, String url);
}
