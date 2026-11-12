package com.quienlotiene.shared.infrastructure.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.quienlotiene.shared.infrastructure.web.FiltroIdCorrelacion;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Seguridad de la API (D-15, DECISIONES-BACKEND sección 11): sesión sin estado con JWT RS256 propio validado por el
 * resource server de Spring Security, roles {@code CLIENTE}, {@code TIENDA} y {@code ADMIN}, frases de contraseña
 * con Argon2id y CORS solo para el frontend.
 */
@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
@EnableConfigurationProperties({PropiedadesJwt.class, PropiedadesCors.class})
public class SeguridadConfig {

    /**
     * Rutas que no piden sesión: registro, inicio y refresco de sesión, la documentación (solo fuera de prod) y la salud
     * de la API para el healthcheck del contenedor (Caddy solo publica {@code /api/*}, así que no sale a internet).
     */
    private static final String[] RUTAS_PUBLICAS = {
        "/api/v1/auth/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**",
        "/error",
        "/actuator/health",
        "/actuator/health/**"
    };

    private static final String RUTA_REGISTRO_TIENDA = "/api/v1/tiendas/registro";
    private static final String RUTAS_ADMINISTRACION = "/api/v1/admin/**";

    // Parámetros de Argon2id recomendados por OWASP: 19 MiB de memoria, 2 iteraciones, 1 hilo, sal de 16 bytes
    private static final int BYTES_SAL = 16;
    private static final int BYTES_HASH = 32;
    private static final int HILOS = 1;
    private static final int MEMORIA_EN_KIB = 19 * 1024;
    private static final int ITERACIONES = 2;

    @Bean
    SecurityFilterChain cadenaFiltrosSeguridad(
            HttpSecurity http,
            ManejadorFallasSeguridad manejadorFallas,
            JwtAuthenticationConverter convertidorAutenticacion,
            CorsConfigurationSource origenesCors)
            throws Exception {
        return http
                // Sin sesión de servidor ni cookies de autenticación: el JWT viaja en Authorization, así que CSRF no
                // aplica. La cookie del token de refresco es SameSite=Strict (HU-003).
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(origenesCors))
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(autorizacion -> autorizacion
                        .requestMatchers(RUTAS_PUBLICAS)
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, RUTA_REGISTRO_TIENDA)
                        .permitAll()
                        .requestMatchers(RUTAS_ADMINISTRACION)
                        .hasRole("ADMIN")
                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer(servidorRecursos -> servidorRecursos
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(convertidorAutenticacion))
                        .authenticationEntryPoint(manejadorFallas)
                        .accessDeniedHandler(manejadorFallas))
                .exceptionHandling(excepciones ->
                        excepciones.authenticationEntryPoint(manejadorFallas).accessDeniedHandler(manejadorFallas))
                .build();
    }

    @Bean
    KeyPair parLlavesJwt(PropiedadesJwt propiedades) {
        return CargadorLlavesRsa.cargar(propiedades);
    }

    @Bean
    JwtEncoder codificadorJwt(KeyPair parLlavesJwt, PropiedadesJwt propiedades) {
        var llaveRsa = new RSAKey.Builder((RSAPublicKey) parLlavesJwt.getPublic())
                .privateKey((RSAPrivateKey) parLlavesJwt.getPrivate())
                .keyID(propiedades.kid())
                .build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(llaveRsa)));
    }

    @Bean
    JwtDecoder decodificadorJwt(KeyPair parLlavesJwt, PropiedadesJwt propiedades) {
        var decodificador = NimbusJwtDecoder.withPublicKey((RSAPublicKey) parLlavesJwt.getPublic())
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();
        // Revisa la firma, la fecha de expiración y que el emisor sea esta API
        decodificador.setJwtValidator(JwtValidators.createDefaultWithIssuer(propiedades.emisor()));
        return decodificador;
    }

    /** Convierte el claim {@code roles} en autoridades {@code ROLE_CLIENTE}, {@code ROLE_TIENDA}, {@code ROLE_ADMIN}. */
    @Bean
    JwtAuthenticationConverter convertidorAutenticacionJwt() {
        var convertidorRoles = new JwtGrantedAuthoritiesConverter();
        convertidorRoles.setAuthoritiesClaimName(EmisorTokenAcceso.CLAIM_ROLES);
        convertidorRoles.setAuthorityPrefix("ROLE_");
        var convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(convertidorRoles);
        return convertidor;
    }

    @Bean
    PasswordEncoder codificadorFrasesContrasena() {
        return new Argon2PasswordEncoder(BYTES_SAL, BYTES_HASH, HILOS, MEMORIA_EN_KIB, ITERACIONES);
    }

    @Bean
    CorsConfigurationSource origenesCors(PropiedadesCors propiedades) {
        var configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(propiedades.origenesPermitidos());
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(
                List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, FiltroIdCorrelacion.CABECERA));
        configuracion.setExposedHeaders(List.of(HttpHeaders.LOCATION, FiltroIdCorrelacion.CABECERA));
        // Necesario para que el navegador envíe la cookie del token de refresco
        configuracion.setAllowCredentials(true);
        var fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", configuracion);
        return fuente;
    }
}
