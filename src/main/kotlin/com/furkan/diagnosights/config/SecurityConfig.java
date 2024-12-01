package com.furkan.diagnosights.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final String ALLOWED_HEADERS = "x-requested-with,authorization,Content-Type,Authorization,credential,X-XSRF-TOKEN,traceparent";
    private static final String ALLOWED_METHODS = "GET,PUT,POST,DELETE";

    @Value("${admin.password}")
    private String adminPassword;

    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("mikail")
                .password(adminPassword)
                .build();
        return new MapReactiveUserDetailsService(user);
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/login").permitAll()
                        .anyExchange().authenticated()
                )
                .httpBasic(withDefaults())
                .cors(cors -> cors.configurationSource(createCorsConfigurationSource()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(formLoginSpec -> formLoginSpec.loginPage("/login").authenticationSuccessHandler((swe, auth) -> {
                    swe.getExchange().getResponse().setStatusCode(HttpStatus.OK);
                    return swe.getExchange().getResponse().writeWith(Mono.just(new DefaultDataBufferFactory().wrap(("{\"username\": \"" + auth.getName() + "\"}").getBytes())));

                }).authenticationFailureHandler((swe, auth) -> {
                    swe.getExchange().getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return Mono.empty();
                }))

                .exceptionHandling(handlingSpec -> handlingSpec.authenticationEntryPoint((swe, e) -> Mono.fromRunnable(() -> {
                    swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                })))
        ;
        return http.build();
    }

    private CorsConfigurationSource createCorsConfigurationSource() {

        CorsConfiguration defaultConfiguration = new CorsConfiguration();

        defaultConfiguration.setAllowedOriginPatterns(Arrays.asList("*://localhost", "*://localhost:[*]"
        ));
        defaultConfiguration.setAllowedMethods(Arrays.asList(ALLOWED_METHODS.split(",")));
        defaultConfiguration.setAllowedHeaders(Arrays.asList(ALLOWED_HEADERS.split(",")));
        defaultConfiguration.setAllowCredentials(true);
        defaultConfiguration.setExposedHeaders(Arrays.asList(ALLOWED_HEADERS.split(",")));


        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        //insertion order matters
        source.registerCorsConfiguration("/**", defaultConfiguration);

        return source;
    }
}