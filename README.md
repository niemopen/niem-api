
# NIEM API 2.0

This is a Spring Boot (Java) REST API and backend implementation for NIEM tool functionality.  It includes support for model management, search, transformations, migrations, and validation.

## Purpose

Provide current NIEM tool capabilities:

- as open source code for NIEMOpen
- for use with NIEM and user data models
- via an API to support other developers and avoid tool lock-in
- without requiring users to run code locally on their own systems

Support future NIEM model management:

- Maintain and update the NIEM data model
- Build artifacts necessary for publishing NIEM model packages
- Support harmonization work in the NBAC
- Support rapid prototyping for model and NDR updates

Maintain legacy support for older NIEM version

Support multiple serializations of NIEM

- Leverage CMF and CMF transformations
- Provide direct support for NIEM JSON

## Features

### Data model

The SSGT and other NIEM tools have provided support for the NIEM reference data model.  This application provides multi-model support.  This will allow published message models from the community to be included in search and subset functionality, and will give users access to the same functionality that will be used to support NIEM model management.

Most support for the data model is currently implemented.

- Stewards
- Models
- Versions
- Namespaces
- Properties
- Types
- Subproperties
- Facets

The following features are not yet implemented:

- Type unions
- Namespace local terminology
- NIEM 1.0 - 2.1 reference properties
- NIEM 2.0 - 2.1 augmentations
- Special EXT namespace support

### Search

Search for properties and types.

Property search features include:

| Feature  | Description |
|:-------- |:----------- |
token      | Search for full tokens in component names and definitions with stemming. Example: "arm" returns property names with "Arm", "Armed", and "Arming" but does not return "Alarm", "Firearm", "Harm", etc.
substring  | Search for partial text in component names and definitions. Example: "arm" returns property names with "Arm", "Armed", "Arming", "Alarm", "Firearm", "Harm", etc.
prefix     | Filter results on the given prefix(es)
type       | Filter results by substring matching on one of the given types. Example: ["text", "boolean"] matches properties with types that include nc:TextType and niem-xs:boolean
isAbstract | Return abstract or concrete properties
isElement  | Return elements or attributes

### Transformations

Leverages the CMF tool to transform supported representations of NIEM models to available output formats.  Current support:

Inputs:

- CMF
- NIEM XML Schemas

Outputs:

- CMF
- NIEM XML Schemas
- NIEM JSON Schema
- Draft OWL representation

### Migration

- Migrates a NIEM subset represent in CMF from one version to any subsequent version (multi-step support).
- Generates a migration report to track changes.

### Validation

| Feature | Description |
|:------- | ----------- |
XML       | Validate a XML file against provided XML schemas
XSD       | Validate a set of XML Schemas
CMF       | Validate a CMF XML file (v0.6) against the CMF schemas
XML catalog | Validate a XML catalog against the XML catalog schema specification
Message catalog | Validate a message or IEPD XML catalog against the XML schemas
NDR       | Validate XML schemas against NDR 3.0 - 5.0 REF and EXT Schematron rules

## Developers

### OpenAPI

- /api-docs - OpenAPI JSON file
- /api-docs.yaml - OpenAPI YAML file
- /swagger-ui.html - User interface for OpenAPI file

The `springdoc-openapi-javadoc` dependency will leverage JavaDoc method descriptions, parameter descriptions, and return descriptions in the OpenAPI files, reducing the need for as many OpenAPI-specific annotations on controllers.

**Known issues:**

- OpenAPI schema components are not picking up JavaDoc definitions for methods that are overridden, either in the parent or the child.  The definition is being repeated in the `@Schema` annotation for this reason.

### Lombok

This project uses lombok to reduce boilerplate code.  See the **Install** section of their [website](https://projectlombok.org/) to add support for your IDE.

Note: When reviewing Javadoc warnings, correct the original `src` file, not the  generated one under `build/generated/sources/delombok`.
