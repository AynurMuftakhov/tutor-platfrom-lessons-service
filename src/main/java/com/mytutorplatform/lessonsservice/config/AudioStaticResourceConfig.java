package com.mytutorplatform.lessonsservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class AudioStaticResourceConfig implements WebMvcConfigurer {

    @Value("${uploads.audio.dir:./uploads/audio}")
    private String uploadsDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path path = Paths.get(uploadsDir).toAbsolutePath();
        String location = "file:" + path + "/";
        registry.addResourceHandler("/uploads/audio/**")
                .addResourceLocations(location);
    }
}
