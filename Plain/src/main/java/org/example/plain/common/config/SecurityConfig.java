package org.example.plain.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import org.example.plain.common.handler.SecurityExceptionHandler;
import org.example.plain.domain.user.filters.JwtFilter;
import org.example.plain.domain.user.filters.LoginFilter;
import org.example.plain.domain.user.handler.CustomOAuth2SuccessHandler;
import org.example.plain.domain.user.handler.CustomOAuth2FailureHandler;
import org.example.plain.domain.user.interfaces.UserService;
import org.example.plain.domain.user.repository.RefreshTokenRepository;
import org.example.plain.domain.user.repository.UserRepository;
import org.example.plain.domain.user.service.CustomOauth2UserService;
import org.example.plain.domain.user.service.CustomUserDetailsService;
import org.example.plain.domain.user.service.JWTUtil;
import org.example.plain.domain.user.service.UserServiceImpl;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity(debug = false)
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JWTUtil jwtUtil;
    private final CustomOauth2UserService oauth2UserService;
    private final SecurityExceptionHandler securityExceptionHandler;
    private final RefreshTokenRepository repository;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserService userService() {
        return new UserServiceImpl(userRepository, passwordEncoder());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(form -> form.disable())
                .userDetailsService(userDetailsService)
                .with(new Custom(), custom -> custom.getClass())
                .addFilterAt(new JwtFilter(jwtUtil), LoginFilter.class)
                .authorizeHttpRequests(authorizeRequests ->
                        {
                            // sing_in은 회원가입 페이지로 이동.
                            authorizeRequests.requestMatchers("/swagger", "/swagger-ui.html", "/swagger-ui/**", "/api-docs", "/api-docs/**", "/v3/api-docs/**").permitAll();
                            authorizeRequests.requestMatchers("/account/create","/users/login/**","/","/sign_up", "/api/email/**","/login/**").permitAll();
                            authorizeRequests.requestMatchers("/login/oauth2/code/**", "account/check-id").permitAll();  // OAuth2 콜백 URL 명시적 허용
                            authorizeRequests.requestMatchers("/api/auth/**").permitAll();
                            authorizeRequests.requestMatchers("/ws/**").permitAll();
                            authorizeRequests.anyRequest().authenticated();
                        }
                )
                .logout(logout -> {
                    logout.logoutUrl("/account/logout");
                    logout.logoutSuccessUrl("/");
                })
                .oauth2Login(oauth2 ->
                    oauth2.authorizationEndpoint(authorizationEndpoint ->
                        authorizationEndpoint.baseUri("/users/login")
                    )
                    .userInfoEndpoint(userInfoEndpoint ->
                            userInfoEndpoint.userService(oauth2UserService)
                    )
                    .defaultSuccessUrl("/")  // 성공 시 리다이렉트 URL
                    .successHandler(new CustomOAuth2SuccessHandler(jwtUtil,objectMapper))
                    .failureHandler(new CustomOAuth2FailureHandler(objectMapper))
                )
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(securityExceptionHandler)
                )
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    public class Custom extends AbstractHttpConfigurer<Custom, HttpSecurity>{
        @Override
        public void configure(HttpSecurity builder) throws Exception {
            AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);
            LoginFilter jwtAuthenticationFilter = new LoginFilter(repository, jwtUtil,authenticationManager,objectMapper);
            jwtAuthenticationFilter.setFilterProcessesUrl("/users/login");
            builder
                    .addFilterAt(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        }
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow specific origins instead of all
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",  // React development server
            "http://localhost:8080",  // Spring Boot development server
            "https://your-production-domain.com"  // Add your production domain
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache preflight requests for 1 hour
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
