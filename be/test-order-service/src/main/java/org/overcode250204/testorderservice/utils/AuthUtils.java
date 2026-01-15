package org.overcode250204.testorderservice.utils;

import lombok.experimental.UtilityClass;
import org.overcode250204.testorderservice.exceptions.ErrorCode;
import org.overcode250204.testorderservice.exceptions.TestOrderException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@UtilityClass
public class AuthUtils {
    public static Authentication getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication;
        }
        throw new TestOrderException(ErrorCode.USER_UNAUTHENTICATED);
    }

}
