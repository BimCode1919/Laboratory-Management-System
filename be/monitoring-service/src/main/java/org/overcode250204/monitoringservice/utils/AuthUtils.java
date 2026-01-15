package org.overcode250204.monitoringservice.utils;

import lombok.experimental.UtilityClass;

import org.overcode250204.monitoringservice.exceptions.ErrorCode;
import org.overcode250204.monitoringservice.exceptions.MonitoringException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


@UtilityClass
public class AuthUtils {
    public static Authentication getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication;
        }
        throw new MonitoringException(ErrorCode.USER_UNAUTHENTICATED);
    }

}
