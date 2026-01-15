package org.overcode250204.iamservice.utils;

import lombok.experimental.UtilityClass;
import org.overcode250204.iamservice.exceptions.ErrorCode;
import org.overcode250204.iamservice.exceptions.IamServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


@UtilityClass
public class AuthUtils {
    public static Authentication getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication;
        }
        throw new IamServiceException(ErrorCode.USER_UNAUTHENTICATED);
    }

}
