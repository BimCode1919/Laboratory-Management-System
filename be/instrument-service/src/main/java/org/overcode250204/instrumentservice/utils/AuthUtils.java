package org.overcode250204.instrumentservice.utils;

import lombok.experimental.UtilityClass;
import org.overcode250204.instrumentservice.exception.ErrorCode;
import org.overcode250204.instrumentservice.exception.InstrumentException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


@UtilityClass
public class AuthUtils {
    public static Authentication getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication;
        }
        throw new InstrumentException(ErrorCode.USER_UNAUTHENTICATED);
    }

}
