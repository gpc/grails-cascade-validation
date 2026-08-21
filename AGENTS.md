# AGENTS.md - grails-cascade-validation

## Project Overview

The **Grails Cascade Validation Plugin** establishes a `cascaded` constraint property for validateable
objects — domain classes and classes implementing `grails.validation.Validateable`. When
`cascaded: true` is set on a nested object, the nested object's `validate()` method is invoked and its
field errors are re-reported as part of the parent object's validation.

- **Language:** Groovy 5.0.8 on Java 21
- **Framework:** Grails 8.x
- **Build System:** Gradle 8.14.4 (with wrapper)
- **Published artifact:** `io.github.gpc:cascade-validation`
- **Current Version:** 8.0.0-SNAPSHOT
- **License:** Apache 2.0

## Skill Files (Best Practices)

Detailed best practices are documented as skills in `.agents/skills/` (`.claude` is a symlink to `.agents`):

| Skill                                                                                  | Purpose                                                 |
|----------------------------------------------------------------------------------------|---------------------------------------------------------|
| [`repository-structure`](.agents/skills/repository-structure/SKILL.md)                 | Canonical directory layout and architectural rules      |
| [`gradle-best-practices`](.agents/skills/gradle-best-practices/SKILL.md)               | Gradle best practices, convention plugins, and idioms   |
| [`plugin-project`](.agents/skills/plugin-project/SKILL.md)                             | Plugin project scope: source code + unit tests only     |
| [`example-apps`](.agents/skills/example-apps/SKILL.md)                                 | Example app patterns: integration & functional tests    |
| [`enhance-plugin-with-template`](.agents/skills/enhance-plugin-with-template/SKILL.md) | Migrate an existing plugin onto this template structure  |

**Read these skill files before making structural changes to the repository.**

## Critical Rules

1. **NEVER add code to the root `build.gradle` to configure subprojects.** No `subprojects {}`, `allprojects {}`, or
   `configure()` blocks. All shared configuration goes through convention plugins in `build-logic/`.
2. **The plugin project contains ONLY plugin code and unit tests.** No integration tests, no functional tests, no
   example controllers or views.
3. **The example app under `examples/` hosts all integration and functional tests.** It depends on the plugin via
   `implementation project(':cascade-validation')` and tests it as a real consumer would.
4. **Use Gradle convention plugins to deduplicate.** If two or more subprojects share build logic, extract it into a
   convention plugin in `build-logic/`.
5. **Always use lazy Gradle APIs** to avoid eager initialization (`tasks.register()`, `tasks.named()`, `configureEach`,
   `provider {}`).
6. **Do not edit files owned by the template.** `build-logic/`, `.agents/`, `gradle/`, `CONTRIBUTING.md`,
   `docs/src/docs/index.tmpl`, `.github/workflows/` and `.github/scripts/` are synced from
   grails-plugins/grails-plugin-template and any local edit will be reverted by the next sync PR. Put
   project-specific guidance in this file instead.

## Repository Structure

```
grails-cascade-validation/
├── .agents/skills/                    # Agent skill files (.claude is a symlink to .agents)
├── plugin/                            # Core Grails plugin (artifact: cascade-validation)
│   ├── grails-app/                    #   Plugin conf and Application entry point
│   └── src/main/groovy/               #   Plugin source code
├── examples/cascade-validation-example/# Example Grails app (integration & unit tests as a consumer)
├── docs/                              # Asciidoctor documentation
├── build-logic/                       # Gradle convention plugins (composite build)
├── code-coverage/                     # Aggregated JaCoCo report
├── .github/workflows/                 # CI, release, and release-notes workflows
├── build.gradle                       # Root build file (docs + root-publish ONLY)
├── settings.gradle                    # Multi-project settings
├── gradle.properties                  # Version properties
└── project.yml                        # Project metadata (POM, docs, version index)
```

## Build and Test Commands

```bash
# Full build (compile + test)
./gradlew build

# Run only unit tests (plugin module)
./gradlew :cascade-validation:test

# Run integration tests (example app)
./gradlew :cascade-validation-example:integrationTest

# Skip tests
./gradlew build -PskipTests

# Run the example app
./gradlew :cascade-validation-example:bootRun

# Generate documentation
./gradlew docs

# Clean build
./gradlew clean build

# Run code style checks only
./gradlew codeStyle

# Skip code style checks
./gradlew build -PskipCodeStyle

# Verify the repository still matches the template's expectations
groovy .github/scripts/verify-repository.groovy
```

## SDK Requirements

Use SDKMAN to install the correct tool versions (see `.sdkmanrc`):

- Java: `21.0.12-librca`
- Gradle: `8.14.4`
- Groovy: `5.0.8`

Run `sdk env install` to set up the environment.

## Architecture

1. **`CascadeValidationGrailsPlugin`** is the plugin descriptor. Its `doWithApplicationContext()` calls
   `CascadedConstraintRegistration.register(applicationContext)`.
2. **`CascadedConstraintRegistration`** walks the application context and adds `CascadedConstraint` to every
   `ConstraintRegistry` it can reach — the `DefaultConstraintEvaluator`'s registry, the `DefaultValidatorRegistry`,
   and any directly registered `ConstraintRegistry` bean.
3. **`CascadedConstraint`** is the constraint itself. It accepts either a `Boolean` or a `Closure<Boolean>` taking one
   (the property value) or two (property value, target) arguments, and on validation failure copies the child's field
   errors onto the parent with a prefixed field name.

### Core Classes

| Class                              | Location                                                     | Purpose                                    |
|------------------------------------|--------------------------------------------------------------|--------------------------------------------|
| `CascadeValidationGrailsPlugin`    | `plugin/src/main/groovy/grails/cascade/validation/`          | Plugin descriptor                          |
| `CascadedConstraint`               | `plugin/src/main/groovy/grails/cascade/validation/internal/` | The `cascaded` constraint implementation   |
| `CascadedConstraintRegistration`   | `plugin/src/main/groovy/grails/cascade/validation/internal/` | Registers the constraint on GORM registries|

## Configuration

| Property                       | Default | Purpose                                                                                 |
|--------------------------------|---------|-----------------------------------------------------------------------------------------|
| `constraints.cascaded.legacy`  | `false` | Use the pre-Grails-7 `field.0.childProperty` error field naming instead of `field[0].…`  |

## Testing

### Unit Tests (`plugin/src/test/`)

Unit tests use the **Spock Framework** on the JUnit Platform, and exercise `CascadedConstraint` and
`CascadedConstraintRegistration` directly against the `support/Validateable*` fixtures.

### Integration / Unit Tests (`examples/cascade-validation-example/`)

The example app has real GORM domain classes (`Person`, `PhoneNumber`, `TelephoneType`) and a data service, and tests
the plugin the way a consuming application would. Any test that needs a running Grails application or a datastore
belongs here, not in `plugin/`.

Note that unit tests of cascaded constraints must call
`CascadedConstraintRegistration.register(applicationContext)` in `setup()`, because the registration otherwise only
happens when a Grails application context starts. This is documented for users in `docs/src/docs/usage.adoc`.

## CI/CD

- **CI** (`.github/workflows/ci.yml`): verifies the repository structure, builds and tests on push/PR, and publishes
  snapshots to Maven Central Snapshots plus docs to GitHub Pages on push to release branches.
- **Release** (`.github/workflows/release.yml`): triggered by a published GitHub release — stage artifacts, release to
  Maven Central, publish docs to GitHub Pages, bump version.
- **Release Notes** (`.github/workflows/release-notes.yml`): auto-drafts release notes with release-drafter.

## Code Conventions

- Groovy source files follow standard Grails conventions (`grails-app/` for artefacts, `src/main/groovy/` for
  everything else).
- CodeNarc (`build-logic/config/codenarc/codenarc.groovy`) is enforced with zero tolerance on the plugin project:
  single-quote non-interpolated strings, a space after `if`/`switch`, a blank line after a class's opening brace, no
  consecutive blank lines, and a trailing newline.
- **Use `def` for local variables** where the type is inferred from the right-hand side. Explicit types are for cases
  where the type cannot be inferred or `@CompileStatic` needs it. This applies to production code and tests.
- When writing Gradle, always use the latest best practices to avoid eager initialization.

## Grails 8 Notes

- Grails 8's Gradle plugin injects the Grails BOM as a real dependency into every declarable configuration.
  Gradle's JaCoCo plugin contributes its agent and ant jars through `Configuration.defaultDependencies`, which
  only apply while a configuration has no declared dependencies — so the injected BOM silently suppresses them
  and `jacocoAgent` resolves to nothing. `examples/cascade-validation-example/build.gradle` therefore declares
  `jacocoAgent`/`jacocoAnt` explicitly. The plugin project is unaffected because `config.grails-plugin` turns
  the BOM injection off.
- The example app no longer carries a `grails.mime.types` block. Grails 8 supplies MIME defaults from the
  framework, and a local block would *replace* rather than extend them. Use `grails.mime.mergeDefaults: true`
  if custom MIME types are ever needed here.
- The build still runs on Gradle 8.14.4. Gradle 9 is blocked on deprecations inside third-party plugins
  (`org.gradle.api.plugins.Convention` in the Grails Gradle plugin, `StartParameter.isConfigurationCacheRequested`
  in the Asciidoctor plugin), not on anything in this repository or in `build-logic/`.
