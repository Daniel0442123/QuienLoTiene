package com.quienlotiene.tiendas.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quienlotiene.ContenedoresDePrueba;
import com.quienlotiene.tiendas.fixtures.ArchivosDePrueba;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.junit.jupiter.Testcontainers;

/** HU-002 de punta a punta: multipart real, cuenta TIENDA en identidad, tienda y documentos en PostgreSQL y disco. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ContenedoresDePrueba.class)
@Testcontainers(disabledWithoutDocker = true)
@Tag("HU-002")
@DisplayName("HU-002 Registrar tienda por la API")
class RegistroTiendaIT {

    @TempDir
    static Path directorioArchivos;

    @DynamicPropertySource
    static void almacenTemporal(DynamicPropertyRegistry registro) {
        registro.add("quienlotiene.archivos.directorio", () -> directorioArchivos.toString());
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcClient jdbcClient;

    private static MockMultipartFile datos(String correo) {
        var json = """
                {"nombreDueno": "Carlos Muñoz", "correo": "%s", "telefono": "3001234567",
                 "fraseContrasena": "mi tienda vende de todo un poco", "aceptaPoliticaDatos": true,
                 "versionPoliticaDatos": "1.0", "nombreTienda": "Droguería La 5", "categoriaId": "%s",
                 "ubicacion": {"latitud": 2.4448, "longitud": -76.6147}}
                """.formatted(correo, UUID.randomUUID());
        return new MockMultipartFile("datos", "", MediaType.APPLICATION_JSON_VALUE, json.getBytes());
    }

    private ResultActions registrar(String correo, MockMultipartFile... archivos) throws Exception {
        var peticion = multipart("/api/v1/tiendas/registro").file(datos(correo));
        for (var archivo : archivos) {
            peticion.file(archivo);
        }
        return mockMvc.perform(peticion);
    }

    private static MockMultipartFile foto() {
        return new MockMultipartFile("fotoLocal", "fachada.jpg", "image/jpeg", ArchivosDePrueba.unJpeg());
    }

    private static MockMultipartFile rut() {
        return new MockMultipartFile("rut", "rut.pdf", "application/pdf", ArchivosDePrueba.unPdf());
    }

    @Test
    @Tag("CP-TIE-002-01")
    @DisplayName("CP-TIE-002-01 La tienda queda Pendiente, con sus documentos y una cuenta TIENDA que inicia sesión")
    void registraLaTiendaPendiente() throws Exception {
        // Dado
        var correo = "tienda-" + UUID.randomUUID() + "@correo.com";

        // Cuando
        registrar(correo, foto(), rut())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/tiendas/me"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.nombre").value("Droguería La 5"));

        // Entonces: tienda pendiente y cerrada, con dos documentos en el disco
        var tienda = jdbcClient.sql("""
                        select t.id, t.estado, t.abierta from tiendas.tienda t
                        join identidad.usuario u on u.id = t.dueno_id
                        where u.correo = :correo and u.rol = 'TIENDA'
                        """).param("correo", correo).query().singleRow();
        assertThat(tienda.get("estado")).isEqualTo("PENDIENTE");
        assertThat(tienda.get("abierta")).isEqualTo(false);
        var rutas = jdbcClient
                .sql("select ruta from tiendas.documento_verificacion where tienda_id = :tiendaId order by tipo")
                .param("tiendaId", tienda.get("id"))
                .query(String.class)
                .list();
        assertThat(rutas).hasSize(2).allMatch(ruta -> Files.exists(directorioArchivos.resolve(ruta)));
        // La cuenta del dueño puede iniciar sesión para ver el estado de su tienda
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo": "%s", "fraseContrasena": "mi tienda vende de todo un poco"}
                                """.formatted(correo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("TIENDA"));
    }

    @Test
    @Tag("CP-TIE-002-02")
    @DisplayName("CP-TIE-002-02 Sin el documento de existencia responde 400 TIE-002 y no crea la cuenta")
    void rechazaElRegistroSinDocumento() throws Exception {
        var correo = "tienda-" + UUID.randomUUID() + "@correo.com";

        registrar(correo, foto())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("TIE-002"))
                .andExpect(jsonPath("$.detail").value(containsString("RUT")));
        var cuentas = jdbcClient
                .sql("select count(*) from identidad.usuario where correo = :correo")
                .param("correo", correo)
                .query(Long.class)
                .single();
        assertThat(cuentas).isZero();
    }

    @Test
    @DisplayName("Un ejecutable con extensión .pdf se rechaza con TIE-003")
    void rechazaUnArchivoDisfrazado() throws Exception {
        var rutFalso =
                new MockMultipartFile("rut", "rut.pdf", "application/pdf", ArchivosDePrueba.unEjecutableDisfrazado());

        registrar("tienda-" + UUID.randomUUID() + "@correo.com", foto(), rutFalso)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("TIE-003"));
    }

    @Test
    @DisplayName("Un correo ya registrado responde 409 y no deja archivos huérfanos")
    void rechazaElCorreoRepetidoSinDejarArchivos() throws Exception {
        // Dado
        var correo = "tienda-" + UUID.randomUUID() + "@correo.com";
        registrar(correo, foto(), rut()).andExpect(status().isCreated());
        long archivosAntes;
        try (var archivos = Files.walk(directorioArchivos)) {
            archivosAntes = archivos.filter(Files::isRegularFile).count();
        }

        // Cuando / Entonces
        registrar(correo, foto(), rut())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("IDE-001"));
        try (var archivos = Files.walk(directorioArchivos)) {
            assertThat(archivos.filter(Files::isRegularFile).count()).isEqualTo(archivosAntes);
        }
    }
}
