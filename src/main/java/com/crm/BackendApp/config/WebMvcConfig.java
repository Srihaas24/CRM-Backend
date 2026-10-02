package com.crm.BackendApp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.crm.BackendApp.annotation.ApiVersion;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Dynamically prefixes controllers annotated with @ApiVersion(N) with
        // "/api/v{N}"
        for (int v = 1; v <= 5; v++) {
            final int version = v;
            configurer.addPathPrefix(
                    "/api/v" + version,
                    clazz -> {
                        ApiVersion apiVersion = AnnotatedElementUtils.findMergedAnnotation(clazz, ApiVersion.class);
                        return apiVersion != null && apiVersion.value() == version;
                    });
        }
    }
}
