package com.cy.lib_annotation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface PermissionGrant {
    int value() default 1;
}