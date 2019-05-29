package com.example.dyj.compiler;

import com.example.dyj.compiler.exception.IdAlreadyUsedException;
import com.example.dyj.library.Factory;
import com.google.auto.service.AutoService;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class FactoryProcessor extends AbstractProcessor {

    private Types mTypeUtils;
    private Elements mElementUtils;
    private Filer mFiler;
    private Messager mMessager;
    private Map<String, FactoryGroupedClasses> mFactoryClass = new LinkedHashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mTypeUtils = processingEnv.getTypeUtils();
        mElementUtils = processingEnv.getElementUtils();
        mFiler = processingEnv.getFiler();
        mMessager = processingEnv.getMessager();
        System.out.println("FactoryProcessor init...........");
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(Factory.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        System.out.println("FactoryProcessor process start");
        for (Element element :
                roundEnv.getElementsAnnotatedWith(Factory.class)) {

            if (element.getKind() != ElementKind.CLASS) {
                error(element, "Only classes can be annotated with @%s", Factory.class.getSimpleName());
                System.out.println("Only classes can be annotated with");
                return true;
            }

            TypeElement typeElement = (TypeElement) element;
            try {
                FactoryAnnotatedClass fac = new FactoryAnnotatedClass(typeElement);
                if (!isValidClass(fac)) {
                    return true;
                }
                String qualifiedSuperClassName = fac.getQualifiedSuperClassName();
                FactoryGroupedClasses groupedClasses = mFactoryClass.get(qualifiedSuperClassName);
                if (groupedClasses == null) {
                    groupedClasses = new FactoryGroupedClasses(qualifiedSuperClassName);
                    mFactoryClass.put(qualifiedSuperClassName, groupedClasses);
                }
                groupedClasses.add(fac);
            } catch (IllegalArgumentException e) {
                error(typeElement, e.getMessage());
                System.out.println("IllegalArgumentException");
                return true;
            } catch (IdAlreadyUsedException e) {
                System.out.println("IdAlreadyUsedException");
                error(element,
                        "Conflict: The class %s is annotated with @%s with id ='%s' but %s already uses the same id",
                        typeElement.getQualifiedName().toString(), Factory.class.getSimpleName());
                return true;
            }
        }


        try {
            for (FactoryGroupedClasses fgc : mFactoryClass.values()) {
                fgc.generateCode(mElementUtils, mFiler);
            }
            mFactoryClass.clear();
        } catch (IOException e) {
            error(null, e.getMessage());
        }
        System.out.println("FactoryProcessor process end");
        return true;
    }

    private boolean isValidClass(FactoryAnnotatedClass fac) {
        TypeElement classElement = fac.getTypeElement();
        if (!classElement.getModifiers().contains(Modifier.PUBLIC)) {
            error(classElement, "The class %s is not public.",
                    classElement.getQualifiedName().toString());
            System.out.println("The class is not public:" + classElement.getQualifiedName().toString());
            return false;
        }

        if (classElement.getModifiers().contains(Modifier.ABSTRACT)) {
            error(classElement, "The class %s is abstract. You can't annotate abstract classes with @%",
                    classElement.getQualifiedName().toString(), Factory.class.getSimpleName());
            System.out.println("The class is not abstract:" + classElement.getQualifiedName().toString());
            return false;
        }

        TypeElement superClassElement = mElementUtils.getTypeElement(fac.getQualifiedSuperClassName());
        if (superClassElement.getKind() == ElementKind.INTERFACE) {
            if (!classElement.getInterfaces().contains(superClassElement.asType())) {
                error(classElement, "The class %s annotated with @%s must implement the interface %s",
                        classElement.getQualifiedName().toString(), Factory.class.getSimpleName(),
                        fac.getQualifiedSuperClassName());
                System.out.println("The class must implement the interface");
                return false;
            }
        }

        for (Element element :
                classElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement executableElement = (ExecutableElement) element;
                if (executableElement.getParameters().size() == 0 && executableElement.getModifiers().contains(Modifier.PUBLIC)) {
                    return true;
                }
            }
        }


        error(classElement, "The class %s must provide an public empty default constructor",
                classElement.getQualifiedName().toString());
        return false;
    }

    private void error(Element element, String msg, Object... args) {
//        mMessager.printMessage(Diagnostic.Kind.ERROR,
//                String.format(msg, args),
//                element);
    }
}
