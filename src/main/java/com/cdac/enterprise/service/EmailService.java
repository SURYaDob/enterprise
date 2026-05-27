package com.cdac.enterprise.service;

public interface EmailService {

    /**
     * Sends a password reset email to the given recipient.
     *
     * @param to          the recipient email address
     * @param resetToken  the UUID reset token
     * @param firstName   the user's first name (for personalization)
     */
    void sendPasswordResetEmail(String to, String resetToken, String firstName);
}
