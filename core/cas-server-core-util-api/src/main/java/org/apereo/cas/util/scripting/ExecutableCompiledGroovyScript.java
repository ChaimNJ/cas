package org.apereo.cas.util.scripting;

import java.util.Map;

/**
 * This is {@link ExecutableCompiledGroovyScript}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public interface ExecutableCompiledGroovyScript extends AutoCloseable {

    /**
     * Execute t.
     *
     * @param <T>   the type parameter
     * @param args  the args
     * @param clazz the clazz
     * @return the t
     */
    <T> T execute(Object[] args, Class<T> clazz) throws Throwable;

    /**
     * Execute.
     *
     * @param args the args
     */
    void execute(Object[] args) throws Throwable;

    /**
     * Execute t.
     *
     * @param <T>         the type parameter
     * @param args        the args
     * @param clazz       the clazz
     * @param failOnError the fail on error
     * @return the t
     * @throws Throwable the throwable
     */
    <T> T execute(Object[] args, Class<T> clazz, boolean failOnError) throws Throwable;

    /**
     * Execute t.
     *
     * @param <T>        the type parameter
     * @param methodName the method name
     * @param clazz      the clazz
     * @param args       the args
     * @return the t
     */
    <T> T execute(String methodName, Class<T> clazz, Object... args) throws Throwable;

    /**
     * Sets binding.
     *
     * @param args the args
     */
    default void setBinding(final Map<String, Object> args) {
    }

    @Override
    default void close() {
    }
}
