package org.example.plain.user.service;

import org.example.plain.domain.user.dto.CustomOAuth2User;
import org.example.plain.domain.user.dto.UserRequestResponse;
import org.example.plain.domain.user.interfaces.UserService;
import org.example.plain.domain.user.service.CustomOauth2UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
public class CustomOauth2UserRequestResponseServiceTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CustomOauth2UserService.OauthLoadUser oauthLoadUser;
    private CustomOauth2UserService customOauth2UserService;
    private List<OAuth2User> oAuth2User;
    private OAuth2UserRequest oAuth2UserRequest;

    @BeforeEach
    public void setUp() {
        oAuth2UserRequest = Mockito.mock(OAuth2UserRequest.class);
        oAuth2User = new ArrayList<>();

        //테스트중 response가 여러개로 갈림.
    }

    @Test
    public void isGoogleUserButNotExist(){
        Mockito.when(oAuth2UserRequest.getClientRegistration()).thenReturn(Mockito.mock(ClientRegistration.class));
        Mockito.when(oAuth2UserRequest.getClientRegistration().getRegistrationId()).thenReturn("google");
        Mockito.when(userService.getUserByEmail(Mockito.anyString())).thenReturn(null);

        Map<String, Object> google = new HashMap<>();
        google.put("name", "name");
        google.put("email", "email@email.com");
        google.put("sub",1212);

        oAuth2User.add(new DefaultOAuth2User(null,google,"name"));
        Mockito.when(oauthLoadUser.loadUser(oAuth2UserRequest)).thenReturn(oAuth2User.get(0));
        customOauth2UserService = new CustomOauth2UserService(userService, oauthLoadUser);

        CustomOAuth2User result = (CustomOAuth2User) customOauth2UserService.loadUser(oAuth2UserRequest);

        assertThat(result.getUserRequestResponse().getId()).isEqualTo(null);
        assertThat(result.getUserRequestResponse().getEmail()).isEqualTo("email@email.com");
        assertThat(result.getUserRequestResponse().getUsername()).isEqualTo("name");
    }

    @Test
    public void isGoogleUserExist(){
        Mockito.when(oAuth2UserRequest.getClientRegistration()).thenReturn(Mockito.mock(ClientRegistration.class));
        Mockito.when(oAuth2UserRequest.getClientRegistration().getRegistrationId()).thenReturn("google");
        Mockito.when(userService.getUserByEmail(Mockito.anyString())).thenReturn(new UserRequestResponse("park","name2232","email@email.com","1234"));

        Map<String, Object> google = new HashMap<>();
        google.put("name", "name");
        google.put("email", "email@email.com");
        google.put("sub",1212);

        oAuth2User.add(new DefaultOAuth2User(null,google,"name"));
        Mockito.when(oauthLoadUser.loadUser(oAuth2UserRequest)).thenReturn(oAuth2User.get(0));
        customOauth2UserService = new CustomOauth2UserService(userService, oauthLoadUser);

        CustomOAuth2User result = (CustomOAuth2User) customOauth2UserService.loadUser(oAuth2UserRequest);

        assertThat(result.getUserRequestResponse().getId()).isEqualTo("park");
        assertThat(result.getUserRequestResponse().getEmail()).isEqualTo("email@email.com");
        assertThat(result.getUserRequestResponse().getUsername()).isEqualTo("name2232");
    }

    @Test
    public void isKakaoUserExist(){
        Mockito.when(oAuth2UserRequest.getClientRegistration()).thenReturn(Mockito.mock(ClientRegistration.class));
        Mockito.when(oAuth2UserRequest.getClientRegistration().getRegistrationId()).thenReturn("kakao");
        Mockito.when(userService.getUserByEmail(Mockito.anyString())).thenReturn(new UserRequestResponse("park","name2232","email@email.com","1234"));

        Map<String, Object> kakao = new HashMap<>();
        Map<String, Object> kakaoAttrebute = new HashMap<>();
        kakaoAttrebute.put("name", "name");
        kakaoAttrebute.put("email", "email@email.com");
        kakao.put("id",1212);
        kakao.put("kakao_account",kakaoAttrebute);

        oAuth2User.add(new DefaultOAuth2User(null,kakao,"id"));
        Mockito.when(oauthLoadUser.loadUser(oAuth2UserRequest)).thenReturn(oAuth2User.get(0));
        customOauth2UserService = new CustomOauth2UserService(userService, oauthLoadUser);

        CustomOAuth2User result = (CustomOAuth2User) customOauth2UserService.loadUser(oAuth2UserRequest);

        assertThat(result.getUserRequestResponse().getId()).isEqualTo("park");
        assertThat(result.getUserRequestResponse().getEmail()).isEqualTo("email@email.com");
        assertThat(result.getUserRequestResponse().getUsername()).isEqualTo("name2232");
    }

    @Test
    public void isNaverUserExist(){
        Mockito.when(oAuth2UserRequest.getClientRegistration()).thenReturn(Mockito.mock(ClientRegistration.class));
        Mockito.when(oAuth2UserRequest.getClientRegistration().getRegistrationId()).thenReturn("naver");
        Mockito.when(userService.getUserByEmail(Mockito.anyString())).thenReturn(new UserRequestResponse("park","name2232","email@email.com","1234"));

        Map<String, Object> naver = new HashMap<>();
        Map<String, Object> naverAttrebute = new HashMap<>();
        naver.put("id",12132);
        naverAttrebute.put("name", "name");
        naverAttrebute.put("email", "email@email.com");
        naverAttrebute.put("id",1212);
        naver.put("response",naverAttrebute);

        oAuth2User.add(new DefaultOAuth2User(null,naver,"id"));
        Mockito.when(oauthLoadUser.loadUser(oAuth2UserRequest)).thenReturn(oAuth2User.get(0));
        customOauth2UserService = new CustomOauth2UserService(userService, oauthLoadUser);

        CustomOAuth2User result = (CustomOAuth2User) customOauth2UserService.loadUser(oAuth2UserRequest);

        assertThat(result.getUserRequestResponse().getId()).isEqualTo("park");
        assertThat(result.getUserRequestResponse().getEmail()).isEqualTo("email@email.com");
        assertThat(result.getUserRequestResponse().getUsername()).isEqualTo("name2232");
    }
}
