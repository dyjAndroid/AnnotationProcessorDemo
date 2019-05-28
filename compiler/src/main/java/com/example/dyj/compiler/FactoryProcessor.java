package com.example.dyj.compiler;

import com.example.dyj.library.Factory;

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
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

public class FactoryProcessor extends AbstractProcessor {

    private Types mTypeUtils;
    private Elements mElementUtils;
    private Filer mFiler;
    private Messager mMessager;
    private Map<String, FactoryAnnotatedClass> mFactoryClass = new LinkedHashMap<>();

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
            } catch (IllegalArgumentException e) {
                error(typeElement, e.getMessage());
                return true;
            }
        }
        return true;
    }

    private boolean isValidClass(FactoryAnnotatedClass fac) {

        return false;
    }

    private void error(Element element, String msg, Object... args) {
        mMessager.printMessage(Diagnostic.Kind.ERROR,
                String.format(msg, args),
                element);
    }
}
