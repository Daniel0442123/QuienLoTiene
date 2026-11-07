package com.quienlotiene.arquitectura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;

/**
 * Reglas de capas de Clean Architecture dentro de cada módulo (DECISIONES-BACKEND 4.2, RNF-023).
 * Las reglas permiten paquetes vacíos para que pasen mientras los módulos no tengan código.
 */
@Tag("NF-MAN-023")
@DisplayName("NF-MAN-023 Reglas de arquitectura por capas")
@AnalyzeClasses(packages = "com.quienlotiene", importOptions = ImportOption.DoNotIncludeTests.class)
class ReglasDeArquitecturaTest {

    @ArchTest
    static final ArchRule elDominioNoDependeDeFrameworks = noClasses()
            .that()
            .resideInAPackage("com.quienlotiene..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "org.hibernate..",
                    "com.fasterxml.jackson..",
                    "tools.jackson..",
                    "lombok..")
            .because("el dominio solo usa Java y shared.domain (regla 1)")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule elDominioNoDependeDeOtrasCapas = noClasses()
            .that()
            .resideInAPackage("com.quienlotiene..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("com.quienlotiene..application..", "com.quienlotiene..infrastructure..")
            .because("las dependencias apuntan hacia el dominio")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule laAplicacionNoDependeDeInfraestructura = noClasses()
            .that()
            .resideInAPackage("com.quienlotiene..application..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("com.quienlotiene..infrastructure..")
            .because("los casos de uso hablan con puertos, no con adaptadores")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule losControladoresNoUsanPersistencia = noClasses()
            .that()
            .resideInAPackage("com.quienlotiene..infrastructure.web..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("com.quienlotiene..infrastructure.persistence..")
            .because("los controladores solo llaman casos de uso (regla 3)")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule sinInyeccionEnAtributos = NO_CLASSES_SHOULD_USE_FIELD_INJECTION
            .because("se inyecta por constructor (sección 16)")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule sinSystemOut = NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS.because("se usa SLF4J (sección 14)");
}
