package com.cy.compiler;

import java.util.HashMap;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public class MethodInfo {
    private final String className;
    private final String packageName;
    public HashMap<Integer, String> grantMethodMap = new HashMap<>();
    public HashMap<Integer, String> deniedMethodMap = new HashMap<>();
    public HashMap<Integer, String> rationalMethodMap = new HashMap<>();

    private static final String PROXY_NAME = "PermissionProxy";
    private final String fileName;

    public MethodInfo(final Elements elementUtils, final TypeElement typeElement) {
        final PackageElement packageElement = elementUtils.getPackageOf(typeElement);
        packageName = packageElement.getQualifiedName().toString();
        className = ClassValidator.getClassName(typeElement, packageName);
        fileName = className + "$$" + PROXY_NAME;
    }

    public String generateJavaCode() {
        final StringBuilder builder = new StringBuilder();
        builder.append("// generate code,do not modify\n");
        builder.append("package ").append(packageName).append(";\n\n");
        builder.append("import com.cy.permission.helper.*;");
        builder.append("\n");

        builder.append("public class ").append(fileName).append(" implements "
                + PROXY_NAME + "<" + className + ">");
        builder.append("{\n");

        generateMethod(builder);

        builder.append("}\n");

        return builder.toString();
    }

    private void generateMethod(final StringBuilder builder) {
        generateGrantMethod(builder);
        generateDeniedMethod(builder);
        generateRationalMethod(builder);
    }

    private void generateGrantMethod(final StringBuilder builder) {
        builder.append("@Override\n");
        builder.append("public void grant(int requestCode," + className + " source, String[] permissions){\n");
        builder.append("switch(requestCode){\n");
        for (final int requestCode : grantMethodMap.keySet()) {
            builder.append("case " + requestCode + ":\n");
            builder.append("source." + grantMethodMap.get(requestCode) + "(permissions);\n");
            builder.append("break;\n");
        }
        builder.append("}\n");
        builder.append("}\n");
    }

    private void generateDeniedMethod(final StringBuilder builder) {
        builder.append("@Override\n");
        builder.append("public void denied(int requestCode," + className + " source, String[] permissions){\n");
        builder.append("switch(requestCode){\n");
        for (final int requestCode : deniedMethodMap.keySet()) {
            builder.append("case " + requestCode + ":\n");
            builder.append("source." + deniedMethodMap.get(requestCode) + "(permissions);\n");
            builder.append("break;\n");
        }
        builder.append("}\n");
        builder.append("}\n");
    }

    private void generateRationalMethod(final StringBuilder builder) {
        builder.append("@Override\n");
        builder.append("public boolean rational(int requestCode," + className +
                " source, String[] permissions,PermissionRationCallback callback){\n");
        builder.append("switch(requestCode){\n");
        for (final int requestCode : rationalMethodMap.keySet()) {
            builder.append("case " + requestCode + ":\n");
            builder.append("source." + rationalMethodMap.get(requestCode) + "(permissions);\n");
            builder.append("return true;\n");
        }
        builder.append("}\n");
        builder.append("return false;\n");
        builder.append("}");
    }

    public String getFileName() {
        return fileName;
    }

    public String getClassName() {
        return className;
    }

    public String getPackageName() {
        return packageName;
    }
}
