package org.apereo.cas.pm;

/**
 * This is {@link PasswordValidationService}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public interface PasswordValidationService {
    /**
     * Validate password.
     *
     * @param bean the bean
     * @return true /false
     * @throws Throwable the throwable
     */
    boolean isValid(PasswordChangeRequest bean) throws Throwable;

    /**
     * Does password comply with password policy.
     *
     * @param password the password
     * @return the boolean
     * @throws Throwable the throwable
     */
    boolean isAcceptedByPasswordPolicy(String password) throws Throwable;
}
