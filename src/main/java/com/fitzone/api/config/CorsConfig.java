package com.fitzone.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration 
public class CorsConfig implements WebMvcConfigurer {
    
    private final String[] origensPermitidas;

    //a lista vem do application.properties: em produção muda a origem, não o código
    public CorsConfig(@Value ("${fitzone.cors.origens-permitidas}") String [] origensPermitidas) {
        this.origensPermitidas = origensPermitidas;
    }

    @Override 
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origensPermitidas)
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                //sem isso o JS não consegue ler o header location dos 201
                .exposedHeaders("Location");
    }
}
