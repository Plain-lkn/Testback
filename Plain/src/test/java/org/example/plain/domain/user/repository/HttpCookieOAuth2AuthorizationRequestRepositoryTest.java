//package org.example.plain.domain.user.repository;
//
//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.example.plain.domain.user.service.CookieUtils;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.mock.web.MockHttpServletRequest;
//import org.springframework.mock.web.MockHttpServletResponse;
//import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//class HttpCookieOAuth2AuthorizationRequestRepositoryTest {
//
//    private HttpCookieOAuth2AuthorizationRequestRepository repository;
//    private MockHttpServletRequest request;
//    private MockHttpServletResponse response;
//
//    @BeforeEach
//    void setUp() {
//        repository = new HttpCookieOAuth2AuthorizationRequestRepository();
//        request = new MockHttpServletRequest();
//        response = new MockHttpServletResponse();
//    }
//
//    @Test
//    @DisplayName("OAuth2 인증 요청을 쿠키에 저장하고 로드할 수 있다")
//    void loadAndSaveAuthorizationRequest() {
//        // given
//        OAuth2AuthorizationRequest authRequest = OAuth2AuthorizationRequest.authorizationCode()
//                .clientId("test-client")
//                .authorizationUri("http://test-auth-uri")
//                .redirectUri("http://test-redirect-uri")
//                .build();
//
//        // when
//        repository.saveAuthorizationRequest(authRequest, request, response);
//
//        // 쿠키가 생성되었는지 확인
//        Cookie[] cookies = response.getCookies();
//        assertThat(cookies).isNotNull();
//        assertThat(cookies).hasSize(1);
//        assertThat(cookies[0].getName()).isEqualTo(HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
//
//        // 저장된 쿠키를 request에 설정
//        request.setCookies(cookies);
//
//        // then
//        OAuth2AuthorizationRequest loadedRequest = repository.loadAuthorizationRequest(request);
//        assertThat(loadedRequest).isNotNull();
//        assertThat(loadedRequest.getClientId()).isEqualTo(authRequest.getClientId());
//        assertThat(loadedRequest.getAuthorizationUri()).isEqualTo(authRequest.getAuthorizationUri());
//        assertThat(loadedRequest.getRedirectUri()).isEqualTo(authRequest.getRedirectUri());
//    }
//
////    @Test
////    @DisplayName("OAuth2 인증 요청을 삭제할 수 있다")
////    void removeAuthorizationRequest() {
////        // given
////        OAuth2AuthorizationRequest authRequest = OAuth2AuthorizationRequest.authorizationCode()
////                .clientId("test-client")
////                .authorizationUri("http://test-auth-uri")
////                .redirectUri("http://test-redirect-uri")
////                .build();
////
////        repository.saveAuthorizationRequest(authRequest, request, response);
////        request.setCookies(response.getCookies());
////
////        // when
////        repository.removeAuthorizationRequest(request, response);
////        repository.removeAuthorizationRequestCookies(request, response);
////
////        // then
////        Cookie[] cookies = response.getCookies();
////        assertThat(cookies).isNotNull();
////        for (Cookie cookie : cookies) {
////            if (cookie.getName().equals(HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)) {
////                assertThat(cookie.getMaxAge()).isEqualTo(0);
////            }
////        }
////
////        OAuth2AuthorizationRequest loadedRequest = repository.loadAuthorizationRequest(request);
////        assertThat(loadedRequest).isNull();
////    }
//
//    @Test
//    @DisplayName("리다이렉트 URI를 쿠키에 저장할 수 있다")
//    void saveRedirectUri() {
//        // given
//        String redirectUri = "http://test-redirect-uri";
//        request.setParameter(HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME, redirectUri);
//
//        OAuth2AuthorizationRequest authRequest = OAuth2AuthorizationRequest.authorizationCode()
//                .clientId("test-client")
//                .authorizationUri("http://test-auth-uri")
//                .redirectUri(redirectUri)
//                .build();
//
//        // when
//        repository.saveAuthorizationRequest(authRequest, request, response);
//
//        // then
//        Cookie[] cookies = response.getCookies();
//        assertThat(cookies).isNotNull();
//        boolean hasRedirectUriCookie = false;
//        for (Cookie cookie : cookies) {
//            if (cookie.getName().equals(HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)) {
//                hasRedirectUriCookie = true;
//                assertThat(cookie.getValue()).isEqualTo(redirectUri);
//            }
//        }
//        assertThat(hasRedirectUriCookie).isTrue();
//    }
//
//    @Test
//    @DisplayName("인증 요청이 null일 경우 쿠키를 삭제한다")
//    void removeAuthorizationRequestWhenNull() {
//        // when
//        repository.saveAuthorizationRequest(null, request, response);
//
//        // then
//        Cookie[] cookies = response.getCookies();
//        assertThat(cookies).isNotNull();
//        for (Cookie cookie : cookies) {
//            if (cookie.getName().equals(HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)) {
//                assertThat(cookie.getMaxAge()).isEqualTo(0);
//            }
//        }
//    }
//}