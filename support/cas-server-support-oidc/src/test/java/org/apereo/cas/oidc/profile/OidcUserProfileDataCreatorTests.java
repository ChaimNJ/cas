package org.apereo.cas.oidc.profile;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.jee.context.JEEContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcUserProfileDataCreatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDC")
class OidcUserProfileDataCreatorTests {

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.oauth.core.user-profile-view-type=FLAT",
        "cas.authn.oidc.core.claims-map.email=email-address"
    })
    class WithClaimMappingsTests extends AbstractOidcTests {
        @Test
        void verifyOperation() throws Throwable {
            val context = new JEEContext(new MockHttpServletRequest(), new MockHttpServletResponse());
            val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("email", List.of("casuser@example.org"),
                    "email-address", List.of("casuser@apereo.org")));
            val accessToken = getAccessToken(principal);
            val data = oidcUserProfileDataCreator.createFrom(accessToken, context);
            val attrs = (Map) data.get(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ATTRIBUTES);
            assertFalse(attrs.containsKey("email-address"));
            assertTrue(attrs.containsKey("email"));
            assertEquals("casuser@apereo.org", CollectionUtils.firstElement(attrs.get("email")).get());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.oauth.access-token.crypto.encryption-enabled=false",
        "cas.authn.oidc.id-token.include-id-token-claims=true"
    })
    class DefaultTests extends AbstractOidcTests {
        @Test
        void verifyOperation() throws Throwable {
            val context = new JEEContext(new MockHttpServletRequest(), new MockHttpServletResponse());
            val accessToken = getAccessToken();
            val data = oidcUserProfileDataCreator.createFrom(accessToken, context);
            assertFalse(data.isEmpty());
            assertEquals(((AuthenticationAwareTicket) accessToken.getTicketGrantingTicket()).getAuthentication()
                .getAuthenticationDate().toEpochSecond(), (long) data.get(OidcConstants.CLAIM_AUTH_TIME));
            assertTrue(data.containsKey(OidcConstants.CLAIM_SUB));
            assertTrue(data.containsKey(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ID));
            assertTrue(data.containsKey(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_CLIENT_ID));
            assertTrue(data.containsKey(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ATTRIBUTES));
            assertTrue(data.containsKey(CasProtocolConstants.PARAMETER_SERVICE));

            val attrs = (Map) data.get(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ATTRIBUTES);
            assertTrue(attrs.containsKey("email"));
        }

        @Test
        void verifyTokenWithClaims() throws Throwable {
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            val context = new JEEContext(request, response);
            val claims = "\"userinfo\": {\"given_name\": {\"essential\": true}}";
            request.addParameter(OAuth20Constants.CLAIMS, claims);
            val result = oauthRequestParameterResolver.resolveRequestClaims(context);

            val accessToken = getAccessToken();
            when(accessToken.getClaims()).thenReturn(result);
            val data = oidcUserProfileDataCreator.createFrom(accessToken, context);
            assertFalse(data.isEmpty());
            assertTrue(data.containsKey(OidcConstants.CLAIM_SUB));
        }
    }
}
