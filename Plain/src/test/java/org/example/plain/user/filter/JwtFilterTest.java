package org.example.plain.user.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.plain.domain.user.filters.JwtFilter;
import org.example.plain.domain.user.service.JWTUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

@TestPropertySource(properties = {"spring.jwt.expires=1800000","spring.jwt.refresh.expires=864000000"})
@TestPropertySource(properties = {"spring.jwt.secret=rjwltakfzldltkdqndnqnsdcxptmdxadksfslkafjlkfafasdsasfsaf"})
@SpringJUnitConfig(classes = {JWTUtil.class})
public class JwtFilterTest {

    @Autowired
    private JWTUtil jwtUtil;


    private JwtFilter jwtFilter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;



    @BeforeEach
    public void setUp() {
        jwtFilter = new JwtFilter(jwtUtil);
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        chain = Mockito.mock(FilterChain.class);
    }

    @Test
    public void noHaveToken() throws Exception {
        jwtFilter.doFilter(request, response, chain);
        Mockito.verify(chain,Mockito.atLeastOnce()).doFilter(request, response);
    }

    @Test
    public void haveToken() throws Exception {
        String validToken = jwtUtil.makeJwtToken("test","testName");
        Mockito.doReturn(validToken).when(request).getHeader("Authorization");
        jwtFilter.doFilter(request, response, chain);
        Mockito.verify(chain,Mockito.atLeastOnce()).doFilter(request, response);
    }

    @Test
    public void unvalidToken() throws Exception {
        String invalidToken = jwtUtil.makeRefreshToken("test", "testName");
        Mockito.doReturn(invalidToken).when(request).getHeader("Authorization");
        Mockito.doReturn(new PrintWriter(new StringWriter())).when(response).getWriter();
        jwtFilter.doFilter(request, response, chain);
        Mockito.verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Mockito.verify(response).getWriter();
    }

    @Test
    public void noBearerToken() throws Exception {
        String validToken = jwtUtil.makeJwtToken("test","testName").substring(7);
        Mockito.doReturn(validToken).when(request).getHeader("Authorization");
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            jwtFilter.doFilter(request, response, chain);
        });
    }
}
