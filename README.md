
# NIEM API 2.0

This is a Java Spring Boot REST API and backend implementation for NIEM tool functionality.  It includes support for model management, search, transformations, migrations, and validation.

## Purpose

Provide existing NIEM tool capabilities:

- as open source code for NIEMOpen
- for use with both NIEM reference and user data models
- via an API to make functionality easily accessible to other developers and to avoid tool lock-in
- without requiring users to install and run code locally on their own systems

Support future NIEM model management:

- Maintain and update the NIEM data model
- Build artifacts necessary for publishing NIEM model packages
- Support harmonization work in the NBAC
- Support rapid prototyping for model and NDR updates

Maintain legacy support for older NIEM versions

Support multiple serializations of NIEM

- Leverage CMF and CMF transformations as a modular approach to multi-format support
- Provide direct support for NIEM JSON

## Features

### Data model

The SSGT and other NIEM tools have provided support for a single data model - the NIEM reference data model.  This application provides multi-model NIEM support.  This will allow published message models from the community to be included in searches and subsets, and will give users access to the same functionality that will be used to support NIEM model management.

Most support for the data model is currently implemented.

- [x] Stewards
- [x] Models
- [x] Versions
- [x] Namespaces
- [x] Properties
- [x] Types
- [x] Subproperties
- [x] Facets

The following model features are not yet implemented:

- [ ] Type unions
- [ ] Namespace local terminology
- [ ] NIEM 1.0 - 2.1 reference properties
- [ ] NIEM 2.0 - 2.1 augmentations
- [ ] Special EXT namespace support

The API currently only supports read access to NIEM data models.  The ability to create, update, and delete models and their contents will be added in the future:

- [x] Read model content
- [ ] Create new model content
- [ ] Update existing model content
- [ ] Delete model content

### Search

Search models stored in the application's database, which includes the NIEM reference model and may include published and contributed IEPDs.

- [x] Properties
- [x] Types
- [ ] Codes

Property search features include:

| Feature    | Description |
|:---------- |:----------- |
| NIEM version number | Search across all models based on a specific version of NIEM to find interoperable results. |
| token      | Search for full tokens in component names and definitions with stemming. <br/> Example: "arm" returns property names with "Arm", "Armed", and "Arming" but does not return "Alarm", "Firearm", "Harm", etc. |
| substring  | Search for partial text in component names and definitions. <br/> Example: "arm" returns property names with "Arm", "Armed", "Arming", "Alarm", "Firearm", "Harm", etc. |
| prefix     | Filter results on the given prefix(es) |
| type       | Filter results by substring matching on one of the given types. <br/> Example: An array with "text", "boolean" values matches properties with types that include nc:TextType and niem-xs:boolean |
| isAbstract | Return abstract or concrete (non-abstract) properties |
| isElement  | Return elements or attributes |

### Transformations

The application leverages the CMF tool to transform supported representations of NIEM models to available output formats.  Current support:

Inputs:

- [x] CMF 0.8
- [x] NIEM XML Schemas (XSD), beginning with NDR version 3.0
- [ ] SSGT wantlist (to support users migrating from the SSGT, especially for NIEM 1.0 - 2.1)

Outputs:

- [x] CMF
- [x] NIEM XML Schemas
- [x] NIEM JSON Schema
- [x] Draft OWL representation
- [ ] Lite UML representation, such as PlantUML class diagram
- [ ] CSVs
- [ ] Documentation spreadsheet
- [ ] Model stats
- [ ] Legacy NIEM XML schemas (NIEM 1.0 - 2.1)

### Migration

- [x] Migrates a NIEM subset represent in CMF from one version to any subsequent version (multi-step support).
- [x] Generates a migration report to track changes and issues.
- [ ] Migrate a model that includes a NIEM or other supported subset, plus extensions.

Note that if a component cannot be migrated, there are two possible reasons:

- The component does not have a counterpart in a later version.
- The component does have a counterpart, but the migration rule has not been added and there is no link between the two.

Migration issues will need to be resolved manually.

### Validation

- [x] **XML** - Validate a XML file against provided XML schemas.

- [x] **XSD** - Validate a set of XML Schemas.

- [x] **CMF** - Validate a CMF XML file (v0.8) against the CMF schemas.

- [x] **XML catalog** - Validate a XML catalog against the OASIS eXML catalog schema.

- [x] **IEPD or message catalog** - Validate a NIEM 3.0 MPD catalog or a NIEM 5.0 IEPD catalog XML file against their schemas.

- [x] **NDR conformance** - Validate NIEM XML schemas against NDR REF and EXT Schematron rules.

- [ ] **JSON** - Validate a JSON instance document against its provided JSON schema.

- [ ] **JSON schema** - Validate a JSON schema document against the JSON schema specification.

- [ ] **CMF QA** - Check a CMF model for general QA issues.

- [ ] **Property QA** - Check a property for NDR conformance issues.

- [ ] **Type QA** - Check a type for NDR conformance issues.

## Developers

### Build

```sh
./gradlew build
```

### OpenAPI documentation

API documentation files:

- **OpenAPI JSON** available at https://tools.niem.gov/api/v2/api-docs or `/docs/openapi.json`.
- **Swagger HTML** available at https://tools.niem.gov/api/v2/swagger-ui/index.html.

Build documentation:

```sh
./gradlew generateOpenApiDocs
```

Known issues:

- [ ] OpenAPI schema components are not picking up JavaDoc definitions for methods that are overridden, either in the parent or the child.  This is why definitions are being repeated in the `@Schema` annotations.

- [ ] Request body parameters.

OpenAPI annotation `@RequestParam` should be able to be used for request body parameters for endpoints that consume multipart form data.  These instead are being generated as query parameters in the OpenAPI documentation.

`@RequestPart` can be used to document request body parameters, but has the following drawbacks when compared to `@RequestParam`:

- Allowable values are not listed in the OpenAPI documentation for params with an enum type.  These parameters are simply marked as strings.
- Default values are not listed.
- Example values are not listed.
- Type conversion in the controllers for parameters types besides Strings or multipart files is not automatically handled.

To simplify the code, the `@RequestParam` annotation is being used despite the incorrect marking of request body parameters as query parameters.  Additional documentation has been added to each of the parameters as the simplest workaround.

### Lombok

This project uses lombok to reduce boilerplate code.  See the **Install** section of their [website](https://projectlombok.org/) to add support for your IDE.

Note: When reviewing Javadoc warnings, correct the original `src` file, not the  generated one under `build/generated/sources/delombok`.

### Environment variables

You can optionally create file `.env` to define postgresql url, username and password values.

The variables declared in this file will be imported into `application.yaml` if available via the `spring.config.import` property.

There or other ways to include these variables, such as via system or user environment variables and via CI/CD settings.

### Testing

A separate database schema (`test`) is used for testing purposes.

### CMF Tool

- Build the jar for the CMF tool and place it under `libs/cmftool`
- Update `app.cmftool` properties in `application.yaml`.

If the version of CMF has changed during an upgrade to the CMF Tool:

- Update CMF schemas for the CMF validation endpoint under `src/main/resources/validation/cmf`.
- Update the path to the CMF schemas in `NiemValidationService` method `validateCmf()`.
- Update `app.cmf` properties in `application.yaml`.
- Update CMF files used in `src/test/resources`.

### Dependencies and plugins

| Library | License | Description |
|:------- |:------- |:----------- |
| [com.github.ben-manes.versions](https://plugins.gradle.org/plugin/com.github.ben-manes.versions) | Apache 2.0 | Gradle plugin that provides tasks for discovering dependency updates. |
| [io.freefair.lombok](https://plugins.gradle.org/plugin/io.freefair.lombok) | MIT | Automatic lombok and delombok configuration |
| [io.spring.dependency-management](https://plugins.gradle.org/plugin/io.spring.dependency-management) | Apache 2.0 | A Gradle plugin that provides Maven-like dependency management functionality |
| [org.springdoc.openapi-gradle-plugin](https://plugins.gradle.org/plugin/org.springdoc.openapi-gradle-plugin) | Apache 2.0 | This plugin generates json OpenAPI description during build time |
| [org.springframework.boot](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot) | Apache 2.0 | Spring Boot makes it easy to create stand-alone, production-grade Spring based Applications that you can "just run". It takes an opinionated view of the Spring platform and third-party libraries so you can get started with minimum configuration. |
| [org.mitre.niem.cmf.cmftool](https://github.com/niemopen/cmftool) | Apache 2.0 | CMFTool is a command-line tool for the developers of NIEM-conforming data exchange specifications using the NIEM Common Model Format (CMF). |
| [com.fasterxml.jackson.core:jackson-core](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core) | Apache 2.0 | Core Jackson processing abstractions (aka Streaming API), implementation for JSON |
| [com.fasterxml.jackson.core:jackson-databind](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind) | Apache 2.0 | General data-binding functionality for Jackson: works on core streaming API |
| [com.fasterxml.jackson.dataformat:jackson-dataformat-xml](https://mvnrepository.com/artifact/com.fasterxml.jackson.dataformat/jackson-dataformat-xml) | Apache 2.0 | Data format extension for Jackson to offer alternative support for serializing POJOs as XML and deserializing XML as pojos. |
| [com.fasterxml.jackson.dataformat:jackson-dataformat-csv](https://mvnrepository.com/artifact/com.fasterxml.jackson.dataformat/jackson-dataformat-csv) | Apache 2.0 | Support for reading and writing CSV-encoded data via Jackson abstractions. |
| [com.fasterxml.jackson.dataformat:jackson-dataformats-text](https://mvnrepository.com/artifact/com.fasterxml.jackson.dataformat/jackson-dataformats-text) | Apache 2.0 | |
| [com.fasterxml.jackson.datatype:jackson-datatype-hibernate6](https://mvnrepository.com/artifact/com.fasterxml.jackson.datatype/jackson-datatype-hibernate6) | Apache 2.0 | Add-on module for Jackson (https://github.com/FasterXML/jackson) to support Hibernate (https://hibernate.org/) version 6.x with Jakarta data types. |
| [com.fasterxml.jackson:jackson-bom](https://mvnrepository.com/artifact/com.fasterxml.jackson/jackson-bom) | Apache 2.0 | Bill of Materials pom for getting full, complete set of compatible versions of Jackson components maintained by FasterXML.com |
| [com.github.therapi:therapi-runtime-javadoc](https://mvnrepository.com/artifact/com.github.therapi/therapi-runtime-javadoc-scribe) | Apache 2.0 | Annotation processor that bakes Javadoc comments into your code so they can be accessed at runtime. |
| [commons-io:commons-io](https://mvnrepository.com/artifact/commons-io/commons-io) | Apache 2.0 | The Apache Commons IO library contains utility classes, stream implementations, file filters, file comparators, endian transformation classes, and much more. |
| [net.lingala.zip4j:zip4j](https://mvnrepository.com/artifact/net.lingala.zip4j/zip4j) | Apache 2.0 | Zip4j - A Java library for zip files and streams |
| [net.sf.saxon:Saxon-HE](https://mvnrepository.com/artifact/net.sf.saxon/Saxon-HE) | MPL 2.0 | The XSLT and XQuery Processor |
| [org.apache.commons:commons-csv:1.10.0](https://mvnrepository.com/artifact/org.apache.commons/commons-csv) | Apache 2.0 | The Apache Commons CSV library provides a simple interface for reading and writing CSV files of various types. |
| [org.hibernate.orm:hibernate-envers](https://mvnrepository.com/artifact/org.hibernate.orm/hibernate-envers) | LGPL 2.1 | |
| [org.hibernate.search:hibernate-search-mapper-orm-orm6](https://mvnrepository.com/artifact/org.hibernate.search/hibernate-search-mapper-orm-orm6) | LGPL 2.1 | Hibernate Search integration to Hibernate ORM - ORM6 version |
| [org.hibernate.search:hibernate-search-backend-lucene](https://mvnrepository.com/artifact/org.hibernate.search/hibernate-search-backend-lucene) | LGPL 2.1 | Hibernate Search Backend relying on embedded instances of Lucene |
| [org.json](https://mvnrepository.com/artifact/org.json/json) | Public | ...The files in this package implement JSON encoders/decoders in Java. It also includes the capability to convert between JSON and XML, HTTP headers, Cookies, and CDL.... |
| [org.postgresql:postgresql](https://mvnrepository.com/artifact/org.postgresql/postgresql) | BSD 2-clause | PostgreSQL JDBC Driver Postgresql |
| [org.springdoc:springdoc-openapi-starter-webmvc-ui](https://mvnrepository.com/artifact/org.springdoc/springdoc-openapi-starter-webmvc-ui) | Apache 2.0 | SpringDoc OpenAPI Starter WebMVC UI |
| [org.springframework.boot:spring-boot-starter-actuator](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-actuator) | Apache 2.0 | Starter for using Spring Boot's Actuator which provides production ready features to help you monitor and manage your application |
| [org.springframework.boot:spring-boot-starter-data-jpa](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-data-jpa) | Apache 2.0 | Starter for using Spring Data JPA with Hibernate |
| [org.springframework.boot:spring-boot-starter-web](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-web) | Apache 2.0 | Starter for building web, including RESTful, applications using Spring MVC. Uses Tomcat as the default embedded container |
| [org.springframework.boot:spring-boot-devtool](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-devtools) | Apache 2.0 | Spring Boot Developer Tools |
| [org.springframework.boot:spring-boot-configuration-processor](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-configuration-processor) | Apache 2.0 | https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-configuration-processor |
| [com.github.therapi:therapi-runtime-javadoc-scribe](https://mvnrepository.com/artifact/com.github.therapi/therapi-runtime-javadoc-scribe) | Apache 2.0 | Annotation processor that bakes Javadoc comments into your code so they can be accessed at runtime. |
| [org.springframework.boot:spring-boot-starter-test](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-test) | Apache 2.0 | Starter for testing Spring Boot applications with libraries including JUnit Jupiter, Hamcrest and Mockito |
