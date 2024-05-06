package io.swagger.codegen.v3.generators.dotnet;

import io.swagger.codegen.v3.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class CsharpDotnetCoreClientCodegen extends AbstractCSharpCodegen {
    public static final String CLIENT_PACKAGE = "clientPackage";
    public static final String USE_CSPROJ_FILE = "useCsProjFile";
    public static final String DefaultargetFramwork = "net8.0";
    protected String clientPackage = "IO.Swagger.Client";
    protected String systemTextJsonVersion = "8.0.3";
    protected String apiDocPath = "docs";
    protected String modelDocPath = "docs";

    protected Map<String, String> versions = new HashMap<>();
    protected String exceptionTypeName;
    protected String apiClientBaseTypeName;

    public CsharpDotnetCoreClientCodegen() {
        super();

        versions.put("net8.0","8.0.3");
        versions.put("net7.0","7.0.4");
        versions.put("net6.0","6.0.9");
        versions.put("net5.0","5.0.2");

        importMapping.clear();

        modelTemplateFiles.put("model.mustache", ".cs");
        apiTemplateFiles.put("api.mustache", ".cs");

        setApiPackage(packageName + ".Api");
        setModelPackage(packageName + ".Model");
        setClientPackage(packageName + ".Client");
        setSourceFolder("src" + File.separator + "main" + File.separator + "CsharpDotNet2");

        modelDocTemplateFiles.put("model_doc.mustache", ".md");
        apiDocTemplateFiles.put("api_doc.mustache", ".md");

        cliOptions.clear();
        cliOptions.add(new CliOption(CodegenConstants.PACKAGE_NAME,
            "C# package name (convention: Camel.Case).")
            .defaultValue(packageName));
        cliOptions.add(new CliOption(CodegenConstants.DOTNET_FRAMEWORK,
            CodegenConstants.DOTNET_FRAMEWORK_DESC)
            .defaultValue("net8.0"));
        cliOptions.add(new CliOption(CodegenConstants.PACKAGE_VERSION,
            "C# package version.")
            .defaultValue(packageVersion));
    }

    @Override
    public void processOpts() {
        super.processOpts();

        sourceFolder = "";

        if (additionalProperties.containsKey(CLIENT_PACKAGE)) {
            setClientPackage((String) additionalProperties.get(CLIENT_PACKAGE));
        } else {
            additionalProperties.put(CLIENT_PACKAGE, getClientPackage());
        }

        String generatorVersion = this.getClass().getPackage().getSpecificationVersion();
        if(generatorVersion == null) {
            generatorVersion = "1.0";
        }
        additionalProperties.put("generateorVersion", generatorVersion);

        final String clientPackage = getClientPackage();

        if(!additionalProperties.containsKey("apiDocPath")) {
            additionalProperties.put("apiDocPath", apiDocPath);
        }
        if(!additionalProperties.containsKey("modelDocPath")) {
            additionalProperties.put("modelDocPath", modelDocPath);
        }

        String exceptionTypeName = clientPackage.replace(".", "") + "ApiException";
        additionalProperties.put("exceptionTypeName", exceptionTypeName);
        String apiClientBaseTypeName = clientPackage.replace(".", "") + "ApiClientBase";
        additionalProperties.put("apiClientBaseTypeName", apiClientBaseTypeName);

        supportingFiles.add(new SupportingFile("ApiException.mustache", "", exceptionTypeName + ".cs"));
        supportingFiles.add(new SupportingFile("ApiClient.mustache", "", "ApiClient.cs"));
        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));

        if (additionalProperties.containsKey(USE_CSPROJ_FILE) && Boolean.parseBoolean(additionalProperties.get(USE_CSPROJ_FILE).toString())) {
            supportingFiles.add(new SupportingFile("csproj.mustache", "", clientPackage + ".csproj"));
        }
        if(!additionalProperties.containsKey(CodegenConstants.DOTNET_FRAMEWORK)) {
            additionalProperties.put(CodegenConstants.DOTNET_FRAMEWORK, DefaultargetFramwork);
        }
        String version =  additionalProperties.get(CodegenConstants.DOTNET_FRAMEWORK).toString();
        boolean contains = versions.containsKey(version);
        if(contains) {
            setSystemTextJsonVersion(versions.get(version));
        }
    }

    @Override
    public String apiPackage() {
        return packageName + ".Clients";
    }

    @Override
    public String modelPackage() {
        return packageName + ".Models";
    }

    public void setSystemTextJsonVersion(String systemTextJsonVersion){
        this.systemTextJsonVersion = systemTextJsonVersion;
    }

    public String getSystemTextJsonVersion(){
        return this.systemTextJsonVersion;
    }

    public String getClientPackage() {
        return clientPackage;
    }

    public void setClientPackage(String clientPackage) {
        this.clientPackage = clientPackage;
    }

    @Override
    protected void processOperation(CodegenOperation operation) {
        CodegenParameter cancellationTokenParameter = new CodegenParameter();
        cancellationTokenParameter.dataType = "CancellationToken";
        cancellationTokenParameter.paramName = "ct";
        cancellationTokenParameter.secondaryParam = true;
        operation.getVendorExtensions().put("x-has-more", false);

        if(operation.allParams.size() != 0) {
            CodegenParameter lastParameter = operation.allParams.get(operation.allParams.size() - 1);
            lastParameter.getVendorExtensions().put("x-has-more", true);
        }

        operation.allParams.add(cancellationTokenParameter);

        super.processOperation(operation);
    }

    @Override
    public io.swagger.codegen.v3.CodegenType getTag() {
        return io.swagger.codegen.v3.CodegenType.CLIENT;
    }

    @Override
    public String getName() {
        return "csharp-dotnet-core";
    }

    @Override
    public String getHelp() {
        return "Generates a C# dotnet Core client library.";
    }

    @Override
    public String apiFileFolder() {
        return outputFolder  + File.separator + "Clients";
    }

    @Override
    public String modelFileFolder() {
        return outputFolder  + File.separator + "Models";
    }

    @Override
    public String apiDocFileFolder() {
        return handleAbsolutePathIfPresentFromProperties("apiDocPath");
    }

    @Override
    public String modelDocFileFolder() {
        return handleAbsolutePathIfPresentFromProperties("modelDocPath");
    }

    private String handleAbsolutePathIfPresentFromProperties(String propertyWithPathName){
        String pathFromProperty = additionalProperties.get(propertyWithPathName).toString();
        return handleAbsolutePathIfPresent(pathFromProperty);
    }

    private String handleAbsolutePathIfPresent(String value){
        String pathFromProperties = value;
        Path path = Paths.get(pathFromProperties);

        if (path.isAbsolute()) {
            return pathFromProperties.replace('/', File.separatorChar);
        }

        return (outputFolder + "/" +pathFromProperties).replace('/', File.separatorChar);
    }
}