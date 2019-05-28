package com.example.dyj.compiler;

import com.example.dyj.compiler.exception.IdAlreadyUsedException;
import com.example.dyj.library.Factory;

import org.omg.CORBA.PUBLIC_MEMBER;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
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

        //遍历所有被@Factory注解的元素
        for (Element element :
                roundEnv.getElementsAnnotatedWith(Factory.class)) {
            //判断该元素是否为类
            if (element.getKind() != ElementKind.CLASS) {
                error(element, "Only classes can be annotated with @%s", Factory.class.getSimpleName());
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
                    mFactoryClass.put(qualifiedSuperClassName,groupedClasses);
                }
                groupedClasses.add(fac);
            } catch (IllegalArgumentException e) {
                error(typeElement, e.getMessage());
                return true;
            } catch (IdAlreadyUsedException e) {
                error(element,
                        "Conflict: The class %s is annotated with @%s with id ='%s' but %s already uses the same id",
                        typeElement.getQualifiedName().toString(), Factory.class.getSimpleName());
                return true;
            }
        }
        return true;
    }

    private boolean isValidClass(FactoryAnnotatedClass fac) {
        TypeElement classElement = fac.getTypeElement();
        if (!classElement.getModifiers().contains(Modifier.PUBLIC)) {
            error(classElement, "The class %s is not public.",
                    classElement.getQualifiedName().toString());
            return false;
        }

        if (!classElement.getModifiers().contains(Modifier.ABSTRACT)) {
            error(classElement, "The class %s is abstract. You can't annotate abstract classes with @%",
                    classElement.getQualifiedName().toString(), Factory.class.getSimpleName());
            return false;
        }

        TypeElement superClassElement = mElementUtils.getTypeElement(fac.getQualifiedSuperClassName());
        if (superClassElement.getKind() == ElementKind.INTERFACE) {
            if (!classElement.getInterfaces().contains(superClassElement.asType())){
                error(classElement, "The class %s annotated with @%s must implement the interface %s",
                        classElement.getQualifiedName().toString(), Factory.class.getSimpleName(),
                        fac.getQualifiedSuperClassName());
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

        //没有找到构造函数
        error(classElement, "The class %s must provide an public empty default constructor",
                classElement.getQualifiedName().toString());
        return false;
    }

    private void error(Element element, String msg, Object... args) {
        mMessager.printMessage(Diagnostic.Kind.ERROR,
                String.format(msg, args),
                element);
    }
}
