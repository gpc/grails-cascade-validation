# Grails Cascade Validation Plugin

[![Maven Central](https://img.shields.io/maven-central/v/io.github.gpc/cascade-validation.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.gpc/cascade-validation)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![CI](https://github.com/gpc/grails-cascade-validation/actions/workflows/ci.yml/badge.svg)](https://github.com/gpc/grails-cascade-validation/actions/workflows/ci.yml)

This plugin establishes a `cascaded` constraint property for validatable objects — domain classes and
classes implementing `grails.validation.Validateable`. When `cascaded: true` is set on a nested object,
the nested object's `validate()` method is invoked and its errors are reported as part of the parent
object's validation.

## Installation

```groovy
dependencies {
    implementation 'io.github.gpc:cascade-validation:7.0.1'
}
```

## Compatibility

| Plugin version | Grails version | Java version |
|----------------|----------------|--------------|
| 8.0.x          | Grails 8       | 21+          |
| 7.0.x          | Grails 7       | 17+          |
| 4.0.x          | Grails 5 / 6   | 11+          |

## Documentation

The reference documentation, including the upgrade notes for the `cascade` &rarr; `cascaded` rename, is
published at **https://gpc.github.io/grails-cascade-validation/**.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).
