package gov.niem.tools.api.transform;

import gov.niem.tools.api.core.exceptions.BadRequestException;
import gov.niem.tools.api.core.utils.CmfUtils;
import gov.niem.tools.api.core.utils.FileUtils;
import gov.niem.tools.api.core.utils.ZipUtils;

import org.mitre.niem.cmf.Component;
import org.mitre.niem.cmf.Datatype;
import org.mitre.niem.cmf.Model;
import org.mitre.niem.cmf.Namespace;
import org.mitre.niem.cmf.Property;
import org.mitre.niem.cmf.SchemaDocument;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.mitre.niem.json.ModelToJSON;
import org.mitre.niem.rdf.ModelToOWL;
import org.mitre.niem.xsd.ModelFromXSD;
import org.mitre.niem.xsd.ModelToN5XSD;
import org.mitre.niem.xsd.ModelToSrcXSD;
import org.mitre.niem.xsd.ModelToXSD;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Operations to support model transformations.
 */
@Log4j2
@Service
public class TransformService {

  /**
   * Convert the input file from the given 'from' format to the given 'to'
   * format.  Saves the output to a temporary directory.
   *
   * @param from - A supported NIEM model format to be transformed
   * @param to - A supported NIEM model format to be transformed into
   * @param multipartInputFile - A file or files to be transformed
   */
  public byte[] transform(TransformFrom from, TransformTo to, MultipartFile multipartInputFile)
      throws Exception {

    log.info(String.format("Transform %s from %s to %s",
        FileUtils.getFilename(multipartInputFile), from, to));

    // Get the input filename base and extension
    String inputFilenameBase = FileUtils.getFilenameBase(multipartInputFile);
    String inputExtension = FileUtils.getFileExtension(multipartInputFile);

    // Verify user input
    checkInput(from, to, inputExtension);

    // Convert input to CMF
    Model cmf = this.loadInput(from, multipartInputFile, inputExtension);

    // Transformation optional step:
    // TODO: Support CMF to simple CMF transforms

    // Convert CMF to the user-selected format
    // TODO: Model should store model name
    byte[] bytes = this.generateOutput(cmf, to, inputFilenameBase);
    return bytes;

  }

  /**
   * Checks to make sure user input is valid.
   *
   * @param from - A supported NIEM model format to be transformed
   * @param to - A supported NIEM model format to be transformed into
   * @param inputExtension - The file extension of the model to be transformed
   */
  public static void checkInput(TransformFrom from, TransformTo to, String inputExtension)
      throws BadRequestException {
    // Check that the input file extension is valid for the given from parameter
    checkInputFileExtension(from, inputExtension);
  }

  /**
   * Check if the input file has a valid extension for a transformation.
   * Throw a BadRequestException if the check fails.
   *
   * @param from - A supported NIEM model format to be transformed
   * @param inputExtension - The file extension of the model to be transformed
   */
  public static void checkInputFileExtension(TransformFrom from, String inputExtension)
      throws BadRequestException {
    switch (from) {
      case xsd:
        if (inputExtension.equals("xsd") || inputExtension.equals("zip")) {
          return;
        }
        break;
      case cmf:
        if (inputExtension.equals("cmf") || inputExtension.equals("cmf.xml")) {
          return;
        }
        break;
      default:
        break;
    }

    String msg = String.format("A file with extension .%s is not a valid input for transforming a model from %s", inputExtension, from.toString());

    throw new BadRequestException(msg);
  }

  /**
   * Convert the input file from the given `from` format to the given
   * `to` format.
   */
  public Model loadInput(TransformFrom from, MultipartFile multipartInputFile,
      String inputExtension) throws Exception {

    Path tempInputFolder = FileUtils.createTempDir("transform-load-input");

    // Save the input multipart file to a new temporary file
    Path inputFile = FileUtils.saveFile(multipartInputFile, tempInputFolder);
    log.debug("User input saved to " + inputFile.toAbsolutePath().toString());

    Model cmf;

    switch (from) {
      case xsd:
        // Read one or more XML schemas and load into a new CMF model.
        List<Path> files = new ArrayList<Path>();

        // Extract to a new temporary folder if input is a zip file
        if (inputExtension.equals("zip")) {
          tempInputFolder = ZipUtils.unzip(inputFile);
        }

        switch (inputExtension) {
          case "xsd":
            // Read a single XSD file into the CMF model
            files.add(inputFile);
            break;
          case "zip":
            // Read a folder with multiple XML schemas into the CMF model
            files = FileUtils.getFilePathsFromDirWithExtension(tempInputFolder, "xsd");
            files.removeIf(file -> file.getFileName().toString().equals("localTerminology.xsd"));

            // Zip folder may also contain xml-catalog.xml files
            List<Path> catalogs = FileUtils.getFilePathsFromDirWithFilename(tempInputFolder,
                "xml-catalog");
            files.addAll(catalogs);
            break;
          default:
            // Handle unexpected input format for a transformation from XSD
            String message = String.format("%s is not supported as an input format for a NIEM transformation from XSD", inputExtension);
            throw new BadRequestException(message);
        }

        ModelFromXSD modelFromXsd = new ModelFromXSD();
        cmf = modelFromXsd.createModel(files.stream()
          .map(path -> path.toString())
          .toArray(String[]::new)
        );

        break;

      case cmf:
        cmf = CmfUtils.loadCmf(multipartInputFile);
        break;

      default:
        // Handle unexpected input cases
        String message = String.format("%s is not supported as a NIEM transformation input", from);
        throw new BadRequestException(message);
    }

    FileUtils.deleteTempDir(tempInputFolder);

    return cmf;

  }

  /**
   * Second pass of transformation. Convert the input file from the given
   * `from` format to the given `to` format.
   */
  public byte[] generateOutput(Model model, TransformTo to, String filenameBase) throws Exception {

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    String results = null;

    switch (to) {
      case cmf:
        results = CmfUtils.generateString(model);
        results = fixCmfOutput(results);
        break;

      case owl:
        ModelToOWL m2o = new ModelToOWL(model);
        m2o.writeRDF(printWriter);
        results = stringWriter.toString();
        results = fixOwlOutput(results);
        break;

      case xsd:
        // Return a zip file
        printWriter.close();
        stringWriter.close();
        return generateXsdOutput(model, filenameBase);

      case json_schema:
        ModelToJSON modelToJson = new ModelToJSON(model);
        modelToJson.writeJSON(printWriter);
        results = stringWriter.toString();
        break;

      default:
        break;
    }

    printWriter.flush();
    printWriter.close();

    stringWriter.flush();
    stringWriter.close();

    if (results == null) {
      throw new Exception("Transform results are null");
    }

    return results.getBytes();

  }

  /**
   * Second pass of transformation for XSD output. Generate XML Schema files
   * from a CMF model and zip the results.
   */
  public byte[] generateXsdOutput(Model model, String filenameBase) throws Exception {

    // Create a temp directory for the results. Dir name includes a unique ID num.
    Path tempDir = FileUtils.createTempDir("transform-output");

    // Create a new subdirectory with a clean name (the model input file name)
    Path xsdDir = FileUtils.createDir(FileUtils.path(tempDir.toString() + "/" + filenameBase));

    // Set up a NIEM 3.0 - 5.2 model writer or a NIEM 6.0+ model writer
    String niem6UriBase = "https://docs.oasis-open.org/niemopen/ns/model";

    Boolean isNiem6 = model.getNamespaceList().stream()
        .filter(namespace -> namespace.getNamespaceURI().contains(niem6UriBase))
        .findAny()
        .isPresent();

    ModelToXSD modelToXsd = isNiem6 ? new ModelToSrcXSD(model) : new ModelToN5XSD(model);

    // Transform the CMF file to XSDs and write to the new directory above
    modelToXsd.writeXSD(xsdDir.toFile());

    // Fix the transform output
    fixXsdOutput(model, xsdDir);

    // Zip the XSD directory to a new zip file under the temp directory
    String zipFilePathString = String.format("%s/%s.zip", tempDir.toString(), filenameBase);
    File zipFile = FileUtils.file(zipFilePathString);
    ZipUtils.zip(xsdDir.toFile(), zipFilePathString);

    // Convert the zip file to bytes and delete the temp directory
    byte[] bytes = Files.readAllBytes(zipFile.toPath());
    FileUtils.deleteTempDir(tempDir);

    return bytes;

  }

  /**
   * Fix errors in the XSD output transform.
   *
   * @todo Get fix for CMF tool XSD output
   */
  private void fixXsdOutput(Model model, Path xsdDir) throws Exception {

    // Get paths to all of the XSDs
    List<Path> xsdPaths = FileUtils.getFilePathsFromDirWithExtension(xsdDir, "xsd");

    List<Property> properties = model.getComponentList()
        .stream()
        .filter(component -> component.asProperty() != null)
        .map(component -> component.asProperty())
        .collect(Collectors.toList());

    for (Path xsdPath : xsdPaths) {
      String xsd = FileUtils.getFileText(xsdPath);

      if (!xsd.contains("ct:conformanceTargets")) {
        // Skip files not generated by the CMF tool
        continue;
      }

      // Remove the errant closing tag on the xs:schema element (attributes will follow)
      xsd = xsd.replaceFirst("<xs:schema>", "<xs:schema");

      // Remove the errant closing tag on the xs:import elements
      xsd = xsd.replace("<xs:import>", "<xs:import");

      // Add the uri to xs:import elements
      xsd = fixXsdImports(model, xsd);

      // Remove the errant closing tag on element refs in a sequence
      xsd = xsd.replace("<xs:element>", "<xs:element ");

      // Add names to elements with substitution groups
      xsd = fixXsdElements(model, xsd, properties);

      // Add the conformance targets namespace prefix declaration if needed
      String conformanceTargetsUri = "http://release.niem.gov/niem/conformanceTargets/3.0/";
      xsd = fixXsdAddPrefix(xsd, "ct", conformanceTargetsUri);

      // Qualify the xml:lang attribute if needed
      xsd = xsd.replace(" lang=", " xml:lang=");

      // Add the xml:lang attribute if missing
      if (!xsd.contains("xml:lang")) {
        String xmlLang = " ".repeat(11) + "xml:lang=\"en-US\"";
        xsd = xsd.replace("<xs:schema", "<xs:schema\n" + xmlLang);
      }

      // Change the conformance target of reference subset schemas
      xsd = xsd.replace("/#ReferenceSchemaDocument", "/#ExtensionSchemaDocument");

      FileUtils.saveFile(xsdPath, xsd.getBytes());
    }

  }

  private String fixXsdImports(Model model, String xsd) {

    String oldImportText = "   <xs:import\n" + " ".repeat(14) + "schemaLocation=\"(.*)\"/>";
    Pattern pattern = Pattern.compile(oldImportText);
    Matcher matcher = pattern.matcher(xsd);

    Map<String, SchemaDocument> schemaDocuments = model.schemadoc();

    while (matcher.find()) {
      // Get the filename from the import
      String relativePath = matcher.group(1);
      int index = relativePath.lastIndexOf("/");
      String filename = relativePath.substring(index + 1);

      // Get the uri from the model based on the file name
      String uri = null;
      for (SchemaDocument schemaDocument : schemaDocuments.values()) {
        if (schemaDocument.filePath().contains(filename)) {
          // Update the XSD
          uri = schemaDocument.targetNS();
          String newImportText = String.format("   <xs:import namespace=\"%s\" schemaLocation=\"%s\"/>", uri, relativePath);
          xsd = xsd.replaceFirst(oldImportText, newImportText);
          matcher = pattern.matcher(xsd);
          break;
        }
      }
    }

    return xsd;

  }

  /**
   * Fix element declarations without names.
   */
  private String fixXsdElements(Model model, String xsd, List<Property> properties) {

    String[] patternFields = {
      "<xs:element",
      "(?:nillable=\".*\")?",
      "(?:substitutionGroup=\"(.*)\")?",
      "type=\"(.*)\">",
      "<xs:annotation>",
      "<xs:documentation>(.*)</xs:documentation"
    };

    Pattern pattern = Pattern.compile(String.join("\\s*", patternFields));
    Matcher matcher = pattern.matcher(xsd);

    Set<Namespace> namespaces = new HashSet<>();

    xsd = matcher.replaceAll(m -> fixXsdElement(model, properties, m, namespaces));

    for (Namespace namespace : namespaces) {
      // Fix any missing namespace prefix declarations
      xsd = fixXsdAddPrefix(xsd, namespace.getNamespacePrefix(), namespace.getNamespaceURI());
    }

    return xsd;
  }

  private String fixXsdElement(Model model, List<Property> properties,
      MatchResult matchResult, Set<Namespace> namespaces) {

    String substitutionGroupQname = matchResult.group(1);
    String typeQname = matchResult.group(2);
    String definition = matchResult.group(3);

    String oldText = matchResult.group();

    Property property = getProperty(properties, substitutionGroupQname, typeQname, definition);

    if (property != null) {
      String newText = oldText.replace("<xs:element",
          String.format("<xs:element name=\"%s\"", property.getName()));
      addDependencyNamespaces(property, namespaces);
      return newText;
    }

    return oldText;
  }

  private void addDependencyNamespaces(Property property, Set<Namespace> namespaces) {
    addDependencyNamespace(property.getClassType(), namespaces);
    addDependencyNamespace(property.getDatatype(), namespaces);
    addDependencyNamespace(property.getSubPropertyOf(), namespaces);
  }

  private void addDependencyNamespace(Component component, Set<Namespace> namespaces) {
    if (component != null) {
      namespaces.add(component.getNamespace());
    }
  }


  private Property getProperty(List<Property> properties, String substitutionGroupQname,
      String typeQname, String definition) {
    return properties.stream()
        .filter(property -> {
          // Check if definition matches (both same value or both null)
          if (!equalOrNull(property.getDefinition(), definition)) {
            return false;
          }

          // Check if substitution group matches (both same qname or both null)
          String actualSubstitutionGroupQname = property.getSubPropertyOf() == null
              ? null
              : property.getSubPropertyOf().getQName();

          if (!equalOrNull(actualSubstitutionGroupQname, substitutionGroupQname)) {
            return false;
          }

          String actualTypeQname = property.getClassType() == null
              ? null
              : property.getClassType().getQName();

          boolean hasDatatype = false;

          if (actualTypeQname == null && property.getDatatype() != null) {
            actualTypeQname = property.getDatatype().getQName();
            hasDatatype = true;
          }

          if (equalOrNull(actualTypeQname, typeQname)) {
            return true;
          }

          // Check in case the CMF tool converted a proxy xs type to a simple xs type
          if (hasDatatype == true) {
            Datatype datatype = property.getDatatype();
            String typeName = typeQname.substring(typeQname.indexOf(":") + 1);
            if (datatype.getName().equals(typeName)
                && datatype.getQName().contains("xs") && typeQname.contains("xs")) {
              return true;
            }
          }

          return false;
        })
        .findFirst()
        .orElse(null);

  }

  /**
   * Returns true if both strings are equal or if both strings are null.
   */
  private boolean equalOrNull(String value1, String value2) {
    if (value1 == null) {
      return value2 == null;
    }
    return value1.equals(value2);
  }

  /**
   * Adds the namespace prefix declaration to the XSD string if it doesn't
   * already exist.
   */
  private String fixXsdAddPrefix(String xsd, String prefix, String uri) {
    String xmlnsPrefix = "xmlns:" + prefix;
    if (!xsd.contains(xmlnsPrefix)) {
      String schemaTag = "<xs:schema\n";
      String prefixSpaces = " ".repeat(11);
      String declaration = String.format("%s=\"%s\"\n", xmlnsPrefix, uri);

      xsd = xsd.replace(schemaTag, String.format("%s%s%s", schemaTag, prefixSpaces, declaration));
    }
    return xsd;
  }

  /**
   * Fix errors and irregular formatting in the CMF output transform.
   *
   * @todo Get fix for CMF tool CMF output
   */
  private String fixCmfOutput(String cmfString) {

    // 1. Replace errant closing tag in CMF output after default xmlns declaration
    final String badText = "xmlns=\"https://docs.oasis-open.org/niemopen/ns/specification/cmf/0.8/\">\n";

    final String goodText = "xmlns=\"https://docs.oasis-open.org/niemopen/ns/specification/cmf/0.8/\"\n";

    cmfString = cmfString.replace(badText, goodText);

    // 2. Replace extra spaces before xmlns prefix declarations
    cmfString = cmfString.replaceAll("       xmlns", "  xmlns");

    // 3. Replace extra space after model opening element
    cmfString = cmfString.replace("<Model \n", "<Model\n");

    return cmfString;

  }

  /**
   * Fix errors and irregular formatting in the OWL output transform.
   *
   * @todo Get fix for CMF tool RDF output
   */
  private String fixOwlOutput(String owlString) {

    // 1. Standardize irregular lines in OWL export
    owlString = owlString.replaceAll("\r\n", "\n").replaceAll("\n\n\n", "\n\n");

    // 2. Replace double ## in URIs with single #
    owlString = owlString.replaceAll("##", "#");

    return owlString;

  }

  /**
   * Get the output filename with extension based on the kind of transformation
   * and the original filename.
   */
  public String getOutputFilename(TransformTo to, String filenameBase) throws Exception {
    switch (to) {
      case cmf:
        return filenameBase + ".cmf.xml";
      case owl:
        return filenameBase + ".owl.ttl";
      case xsd:
        return filenameBase + ".zip";
      case json_schema:
        return filenameBase + ".schema.json";
      default:
        throw new Exception("Unknown transformation format");
    }
  }

  /**
   * Get the content media type for the response based on the kind of transformation.
   */
  public MediaType getOutputMediaType(TransformTo to) throws Exception {
    switch (to) {
      case cmf:
        return MediaType.APPLICATION_XML;
      case json_schema:
        return MediaType.APPLICATION_JSON;
      case owl:
        return MediaType.TEXT_PLAIN;
      case xsd:
        return MediaType.valueOf("application/zip");
      default:
        throw new Exception("Unknown transformation format");
    }
  }

}
