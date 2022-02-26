package com.cy.compiler;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class ClassValidator {

    public static boolean isPrivate(final Element element) {
        return element.getModifiers().contains(Modifier.PRIVATE);
    }

    public static boolean isAbstract(final Element element) {
        return element.getModifiers().contains(Modifier.ABSTRACT);
    }

    public static String getClassName(final TypeElement typeElement, final String pkgName) {
        final int packageLen = pkgName.length();
        return typeElement.getQualifiedName().toString().substring(packageLen + 1).replace(".", "$");
    }
}
