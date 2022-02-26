package com.cy.compiler;

import com.cy.lib_annotation.annotation.PermissionDenied;
import com.cy.lib_annotation.annotation.PermissionGrant;
import com.cy.lib_annotation.annotation.PermissionRational;
import com.google.auto.service.AutoService;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;


@AutoService(Processor.class)
public class RuntimePermissionAbstractProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Messager messager;

    private final HashMap<String, MethodInfo> methodInfoMap = new HashMap<>();
    private Filer filer;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        methodInfoMap.clear();
        messager.printMessage(Diagnostic.Kind.NOTE, "process star ...");
        if (!handleAnnotationInfo(roundEnv, PermissionGrant.class)) {
            return false;
        }
        if (!handleAnnotationInfo(roundEnv, PermissionDenied.class)) {
            return false;
        }
        if (!handleAnnotationInfo(roundEnv, PermissionRational.class)) {
            return false;
        }
        for (final String className : methodInfoMap.keySet()) {
            final MethodInfo info = methodInfoMap.get(className);
            try {
                final JavaFileObject sourceFile = filer.createSourceFile(info.getPackageName() + "." + info.getFileName());
                final Writer writer = sourceFile.openWriter();
                writer.write(info.generateJavaCode());
                writer.flush();
                writer.close();
            } catch (final IOException e) {
                e.printStackTrace();
                messager.printMessage(Diagnostic.Kind.NOTE, "write file failed:" + e.getMessage());
            }
        }
        messager.printMessage(Diagnostic.Kind.NOTE, "process end ...");
        return false;
    }

    private boolean handleAnnotationInfo(final RoundEnvironment roundEnv, final Class<? extends Annotation> annotation) {
        final Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
        for (final Element element : elements) {
            if (!checkMethodValidator(element, annotation)) {
                return false;
            }
            final ExecutableElement methodElement = (ExecutableElement) element;
            final TypeElement enclosingElement = (TypeElement) methodElement.getEnclosingElement();
            final String className = enclosingElement.getQualifiedName().toString();

            MethodInfo methodInfo = methodInfoMap.get(className);
            if (methodInfo == null) {
                methodInfo = new MethodInfo(elementUtils, enclosingElement);
                methodInfoMap.put(className, methodInfo);
            }

            final Annotation annotationClazz = methodElement.getAnnotation(annotation);
            final String methodName = methodElement.getSimpleName().toString();
            final List<? extends VariableElement> parameters = methodElement.getParameters();
            if (parameters == null || parameters.isEmpty()) {
                final String msg = "%s注解下的%s方法必须要有个string[]参数";
                throw new IllegalArgumentException(String.format(msg, annotationClazz.getClass(), methodName));
            }

            if (annotationClazz instanceof PermissionGrant) {
                final int requestCode = ((PermissionGrant) annotationClazz).value();
                methodInfo.grantMethodMap.put(requestCode, methodName);
            } else if (annotationClazz instanceof PermissionDenied) {
                final int requestCode = ((PermissionDenied) annotationClazz).value();
                methodInfo.deniedMethodMap.put(requestCode, methodName);
            } else if (annotationClazz instanceof PermissionRational) {
                final int requestCode = ((PermissionRational) annotationClazz).value();
                methodInfo.rationalMethodMap.put(requestCode, methodName);
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean checkMethodValidator(final Element element, final Class<? extends Annotation> annotation) {
        if (element.getKind() != ElementKind.METHOD) {
            messager.printMessage(Diagnostic.Kind.NOTE, element.getSimpleName() + "不是方法");
            return false;
        }
        if (ClassValidator.isPrivate(element) || ClassValidator.isAbstract(element)) {
            messager.printMessage(Diagnostic.Kind.NOTE, element.getSimpleName() + "方法不能是private或abstract");
            return false;
        }
        return true;
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        final HashSet<String> res = new HashSet<>();
        res.add(PermissionGrant.class.getCanonicalName());
        res.add(PermissionDenied.class.getCanonicalName());
        res.add(PermissionRational.class.getCanonicalName());
        return res;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
