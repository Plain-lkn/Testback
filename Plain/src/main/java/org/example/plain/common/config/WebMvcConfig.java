package org.example.plain.common.config;

import lombok.RequiredArgsConstructor;
import org.example.plain.domain.meeting.interceptor.MeetingAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final MeetingAuthInterceptor meetingAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(meetingAuthInterceptor)
                .addPathPatterns("/api/meetings/**")
                .excludePathPatterns("/api/meetings/public/**");
    }
} 