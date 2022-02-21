package org.apereo.cas.support.events.listener;

import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.apereo.cas.support.events.config.CasConfigurationModifiedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * This is {@link DefaultCasConfigurationEventListener}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultCasConfigurationEventListener implements CasConfigurationEventListener {

    private final CasConfigurationPropertiesEnvironmentManager configurationPropertiesEnvironmentManager;

    private final ConfigurationPropertiesBindingPostProcessor binder;

    private final ContextRefresher contextRefresher;

    private final ApplicationContext applicationContext;

    @Override
    public void onEnvironmentChangedEvent(final EnvironmentChangeEvent event) {
        LOGGER.trace("Received event [{}]", event);
        rebind();
    }

    @Override
    public void onRefreshScopeRefreshed(final RefreshScopeRefreshedEvent event) {
        LOGGER.info("Refreshing application context beans eagerly...");
        initializeBeansEagerly();
    }

    @Override
    public void handleConfigurationModifiedEvent(final CasConfigurationModifiedEvent event) {
        if (event.isEligibleForContextRefresh()) {
            LOGGER.info("Received event [{}]. Refreshing CAS configuration...", event);
            Collection<String> keys = null;
            try {
                keys = contextRefresher.refresh();
                LOGGER.debug("Refreshed the following settings: [{}].", keys);
            } catch (final Exception e) {
                LOGGER.trace(e.getMessage(), e);
            } finally {
                rebind();
                LOGGER.info("CAS finished rebinding configuration with new settings [{}]",
                    ObjectUtils.defaultIfNull(keys, new ArrayList<>(0)));
            }
        }
    }

    private void initializeBeansEagerly() {
        for (val beanName : applicationContext.getBeanDefinitionNames()) {
            Objects.requireNonNull(applicationContext.getBean(beanName).getClass());
        }
    }

    private void rebind() {
        LOGGER.info("Refreshing CAS configuration. Stand by...");
        if (configurationPropertiesEnvironmentManager != null) {
            configurationPropertiesEnvironmentManager.rebindCasConfigurationProperties(this.applicationContext);
        } else {
            CasConfigurationPropertiesEnvironmentManager.rebindCasConfigurationProperties(this.binder, this.applicationContext);
        }
        initializeBeansEagerly();
    }
}
