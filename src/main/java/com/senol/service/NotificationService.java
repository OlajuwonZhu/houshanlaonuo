package com.senol.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void notifyGroupApplicationSubmitted(Long groupId, Long userId) {}

    public void notifyGroupApplicationApproved(Long groupId, Long userId) {}

    public void notifyGroupApplicationRejected(Long groupId, Long userId) {}

    public void notifyEventPublished(Long eventId) {}

    public void notifyEventCanceled(Long eventId) {}

    public void notifySignupReceived(Long eventId, Long signupId) {}

    public void notifySignupApproved(Long eventId, Long signupId) {}

    public void notifySignupRejected(Long eventId, Long signupId) {}

    public void notifyWaitlistPromoted(Long eventId, Long signupId) {}

    public void notifySignupCanceled(Long eventId, Long signupId) {}

    public void notifyCheckIn(Long eventId, Long signupId) {}
}
