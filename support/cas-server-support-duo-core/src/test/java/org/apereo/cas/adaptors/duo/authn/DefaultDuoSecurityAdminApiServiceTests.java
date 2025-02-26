package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.adaptors.duo.DuoSecurityUserAccount;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import com.duosecurity.Client;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultDuoSecurityAdminApiServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class
},
    properties = "cas.http-client.host-name-verifier=none")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("DuoSecurity")
class DefaultDuoSecurityAdminApiServiceTests {
    @Autowired
    @Qualifier("noRedirectHttpClient")
    private HttpClient httpClient;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyCodes() throws Throwable {
        val properties = new DuoSecurityMultifactorAuthenticationProperties();
        try (val webServer = new MockWebServer(true, new ClassPathResource("duoAdminApiResponse-bypassCodes.json"))) {
            webServer.start();
            properties.setDuoApiHost("localhost:%s".formatted(webServer.getPort()))
                .setDuoAdminIntegrationKey(UUID.randomUUID().toString())
                .setDuoAdminSecretKey(UUID.randomUUID().toString());
            val service = getDuoSecurityAdminApiService(properties);
            val codes = service.getDuoSecurityBypassCodesFor("DU3RP9I2WOC59VZX672N");
            assertFalse(codes.isEmpty());
        }
    }

    @Test
    void verifyAccountModification() throws Throwable {
        val properties = new DuoSecurityMultifactorAuthenticationProperties();
        try (val webServer = new MockWebServer(true, new ClassPathResource("duoAdminApiResponse-user.json"))) {
            webServer.start();
            properties.setDuoApiHost("localhost:%s".formatted(webServer.getPort()))
                .setDuoAdminIntegrationKey(UUID.randomUUID().toString())
                .setDuoAdminSecretKey(UUID.randomUUID().toString());
            val service = getDuoSecurityAdminApiService(properties);
            val userAccount = service.modifyDuoSecurityUserAccount(new DuoSecurityUserAccount("casuser"));
            assertFalse(userAccount.isEmpty());
        }
    }

    private DuoSecurityAdminApiService getDuoSecurityAdminApiService(
        final DuoSecurityMultifactorAuthenticationProperties properties) {
        val service = new DefaultDuoSecurityAdminApiService(this.httpClient, properties);
        val duoService = new UniversalPromptDuoSecurityAuthenticationService(properties, httpClient,
            mock(Client.class), List.of(), Caffeine.newBuilder().build());
        val bean = mock(DuoSecurityMultifactorAuthenticationProvider.class);
        when(bean.getId()).thenReturn(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        when(bean.getDuoAuthenticationService()).thenReturn(duoService);
        when(bean.matches(eq(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER))).thenReturn(true);
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext, bean, "duoProvider");
        return service;
    }
}
