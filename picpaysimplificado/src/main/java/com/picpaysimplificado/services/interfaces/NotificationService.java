package com.picpaysimplificado.services.interfaces;

import com.picpaysimplificado.domain.user.User;

public interface NotificationService {
    void sendNotification(User user, String message);
}
