package conf.live.cfp.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Encodes, as executable rules, the hexagonal (ports & adapters) architecture described in
 * {@code .claude/skills/hexagonal-port-adapter-architecture/SKILL.md} and {@code ARCHITECTURE.md}.
 *
 * <p>Rules are written against the package <em>pattern</em> {@code conf.live.cfp.(domain)..} rather
 * than hardcoded to the {@code proposal} domain, so they automatically apply to every future domain
 * added under {@code conf.live.cfp}.
 */
@AnalyzeClasses(packages = "conf.live.cfp", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    /**
     * domain and application must stay framework-agnostic: no Spring, no JPA, no servlet types.
     * jakarta.validation is allowed on adapter DTOs (defense in depth) but must not appear in
     * domain/application, which validate their own invariants instead.
     */
    @ArchTest
    static final ArchRule domain_and_application_are_framework_agnostic = noClasses()
            .that().resideInAnyPackage("..domain..", "..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "jakarta.servlet..",
                    "jakarta.validation..");

    /**
     * domain and application must never instantiate/reference Spring stereotype annotations.
     */
    @ArchTest
    static final ArchRule application_services_have_no_spring_stereotypes = noClasses()
            .that().resideInAnyPackage("..domain..", "..application..")
            .should().beAnnotatedWith("org.springframework.stereotype.Service")
            .orShould().beAnnotatedWith("org.springframework.stereotype.Component");

    /**
     * The hexagonal onion: domain depends on nothing else, application depends only on domain,
     * adapter and config may depend on application and domain (never the reverse).
     */
    @ArchTest
    static final ArchRule layered_dependencies_point_inward = Architectures.layeredArchitecture()
            .consideringOnlyDependenciesInAnyPackage("conf.live.cfp..")
            .layer("Domain").definedBy("conf.live.cfp.(*)..domain..")
            .layer("Application").definedBy("conf.live.cfp.(*)..application..")
            .layer("Adapter").definedBy("conf.live.cfp.(*)..adapter..")
            .layer("Config").definedBy("conf.live.cfp.(*)..config..")

            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Adapter", "Config")
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Adapter", "Config")
            .whereLayer("Adapter").mayNotBeAccessedByAnyLayer()
            .whereLayer("Config").mayNotBeAccessedByAnyLayer();

    /**
     * JPA persistence entities must stay encapsulated within their own adapter package - they must
     * not leak into domain, application, or other adapters/domains.
     */
    @ArchTest
    static final ArchRule persistence_entities_stay_within_their_adapter_package = classes()
            .that().resideInAPackage("..adapter.out.persistence..")
            .and().areAnnotatedWith("jakarta.persistence.Entity")
            .should().onlyBeAccessed().byClassesThat().resideInAPackage("..adapter.out.persistence..");

    /**
     * Reverse check: classes carrying @RestController live in an in/web adapter package - catches a
     * controller accidentally placed in the wrong package.
     */
    @ArchTest
    static final ArchRule rest_controllers_reside_in_web_adapter_package = classes()
            .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
            .should().resideInAPackage("..adapter.in.web..");

    /**
     * Reverse check: classes carrying @Entity live in an out/persistence adapter package.
     */
    @ArchTest
    static final ArchRule jpa_entities_reside_in_persistence_adapter_package = classes()
            .that().areAnnotatedWith("jakarta.persistence.Entity")
            .should().resideInAPackage("..adapter.out.persistence..");

    /**
     * Reverse check: classes carrying @Configuration live in a config package.
     */
    @ArchTest
    static final ArchRule configuration_classes_reside_in_config_package = classes()
            .that().areAnnotatedWith("org.springframework.context.annotation.Configuration")
            .should().resideInAPackage("..config..");

    /**
     * No cyclic dependencies between top-level domains under conf.live.cfp. Only one domain
     * (proposal) exists today so this passes trivially, but it is written to genuinely check
     * cycles among however many domain packages exist - it will start actually protecting
     * something once a second domain is added.
     */
    @ArchTest
    static final ArchRule domains_are_free_of_cycles = SlicesRuleDefinition.slices()
            .matching("conf.live.cfp.(*)..")
            .should().beFreeOfCycles();
}
