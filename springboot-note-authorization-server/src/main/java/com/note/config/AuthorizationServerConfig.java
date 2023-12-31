/*
 * Copyright 2020-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.note.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.note.jose.Jwks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.*;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import java.time.Duration;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfig {
    @Value("${note.issuerURI}")
    String issuerURI;
    @Value("${note.redirectURI}")
    String redirectURI;
    @Value("${note.postLogoutURI}")
    String postLogoutURI;
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {

        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();
        RequestMatcher endpointsMatcher = authorizationServerConfigurer
                .getEndpointsMatcher();

        Function<OidcUserInfoAuthenticationContext, OidcUserInfo> userInfoMapper = (context) -> {
            //OAuth2AccessToken accessToken = context.getAccessToken();
            OidcUserInfoAuthenticationToken authentication = context.getAuthentication();
            JwtAuthenticationToken principal = (JwtAuthenticationToken) authentication.getPrincipal();

            return new OidcUserInfo(principal.getToken().getClaims()); // same as access token
        };

        authorizationServerConfigurer
                .oidc((oidc) -> oidc.userInfoEndpoint((userInfo) -> userInfo
                                .userInfoMapper(userInfoMapper)));
        //                .authorizationEndpoint(authorizationEndpoint ->
//                        authorizationEndpoint.errorResponseHandler(
//                                new SimpleUrlAuthenticationFailureHandler("/error")));

        http
                .securityMatcher(endpointsMatcher)
                .authorizeHttpRequests((authorize) -> authorize
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))
                //.cors().configurationSource(corsConfigurationSource())
                //.cors(cors -> cors.disable())
                .cors(Customizer.withDefaults()) // overwritten with CorsConfig class
//                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
//                        new LoginUrlAuthenticationEntryPoint("/login")))
                // Redirect to the login page when not authenticated from the
                // authorization endpoint
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                .oauth2ResourceServer(oauth2ResourceServer ->
                        oauth2ResourceServer.jwt(Customizer.withDefaults()))
                .apply(authorizationServerConfigurer);
        // @formatter:on
        //http.cors(Customizer.withDefaults());
        return http.build();
    }



    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("note-client") //secret with basic auth
                .clientSecret("{bcrypt}$2a$10$2RjplWIrwNu5LHAi4v9YquSjjbvJP1EQtAJ8j9RaGWFNO/awbO3dS") //note_secret
                .clientAuthenticationMethods(s -> {
                    s.add(ClientAuthenticationMethod.CLIENT_SECRET_POST);
//                    s.add(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
//                    s.add(ClientAuthenticationMethod.NONE);// client authentication set to none for pkce
                })
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
//                .redirectUri("http://127.0.0.1:3000/callback")
//                .postLogoutRedirectUri("http://127.0.0.1:3000/logout")
                .redirectUri(redirectURI) //matched with react frontend app
                .postLogoutRedirectUri(postLogoutURI)
                .scope(OidcScopes.OPENID).scope(OidcScopes.PROFILE)
                .scope("note:read").scope("note:write")
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(true)
                        .requireProofKey(true) //Only PKCE is supported
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                        .idTokenSignatureAlgorithm(SignatureAlgorithm.RS256)
                        .authorizationCodeTimeToLive(Duration.ofSeconds(30))
                        .accessTokenTimeToLive(Duration.ofSeconds(30 * 60))
                        .refreshTokenTimeToLive(Duration.ofSeconds(60 * 60))
//                        .reuseRefreshTokens(true)
                        // when returning the access token response, return new refresh token for security
                        .reuseRefreshTokens(false)
                        .build())
                .build();

        // Save registered client in db as if in-memory
        JdbcRegisteredClientRepository registeredClientRepository = new JdbcRegisteredClientRepository(jdbcTemplate);
        registeredClientRepository.save(registeredClient);

        return registeredClientRepository;
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
    }

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(JdbcTemplate jdbcTemplate,
            RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = Jwks.generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    // Reference:
    // https://docs.spring.io/spring-authorization-server/docs/current/reference/html/core-model-components.html#oauth2-token-customizer
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
//            JwsHeader.Builder headers = context.getJwsHeader();
//            JwtClaimsSet.Builder claims = context.getClaims();
            if (context.getTokenType().equals(OAuth2TokenType.ACCESS_TOKEN)) {
                context.getClaims().claims((claims) -> {
                    // Customize headers/claims for access_token
                    claims.put(OidcScopes.EMAIL, "dxchen1999@gmail.com");
                    claims.put("authority", context.getPrincipal().getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority).collect(Collectors.toSet()));
                });
            }
            if (context.getTokenType().getValue().equals(OidcParameterNames.ID_TOKEN)) {
                context.getClaims().claims((claims) -> {
                    // Customize headers/claims for id_token
                    claims.put(OidcScopes.EMAIL, "dxchen1999@gmail.com");
                    claims.put("authority", context.getPrincipal().getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority).collect(Collectors.toSet()));
                });
            }
        };
    }

    // for docker config
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {

        return AuthorizationServerSettings.builder()
                .issuer(issuerURI)
                .build();
    }

    @Bean
    public EmbeddedDatabase embeddedDatabase() {
        return new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.H2)
                .setScriptEncoding("UTF-8")
                .addScript("org/springframework/security/oauth2/server/authorization/oauth2-authorization-schema.sql")
                .addScript("org/springframework/security/oauth2/server/authorization/oauth2-authorization-consent-schema.sql")
                .addScript("org/springframework/security/oauth2/server/authorization/client/oauth2-registered-client-schema.sql")
                .build();
    }

}
