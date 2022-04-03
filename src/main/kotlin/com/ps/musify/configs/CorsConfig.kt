package com.ps.musify.configs

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry

@Configuration
class CorsConfig {

    @Override
    fun addCorsMappings(corsRegistry: CorsRegistry) {
        corsRegistry.addMapping("/musify/music-artist/details/{mbid}")
            .allowedOrigins("http://localhost:8080")
            .allowedMethods("GET")
    }
}