package com.example.dyj.compiler;

import com.example.dyj.compiler.exception.IdAlreadyUsedException;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.io.Writer;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

public class FactoryGroupedClasses {

    private String mQualifiedSuperClassName;

    private static final String SUFFIX = "Factory";
    private Map<String,FactoryAnnotatedClass> mMap = new LinkedHashMap<>();

    public FactoryGroupedClasses(String qualifiedSuperClassName) {
        mQualifiedSuperClassName = qualifiedSuperClassName;
    }

    public void add(FactoryAnnotatedClass factoryAnnotatedClass) throws IdAlreadyUsedException {
        FactoryAnnotatedClass fac = mMap.get(factoryAnnotatedClass.getId());
        if (fac != null) {
            throw new IdAlreadyUsedException(fac.getQualifiedSuperClassName() + "already exists");
        }
        mMap.put(factoryAnnotatedClass.getId(),factoryAnnotatedClass);
    }


    public void generateCode(Elements elementUtils, Filer filer) throws IOException {
        System.out.println("generateCode start");
        TypeElement superClassElement = elementUtils.getTypeElement(mQualifiedSuperClassName);
        String factoryClassName = superClassElement.getSimpleName() + SUFFIX;
        JavaFileObject jfo = filer.createSourceFile(mQualifiedSuperClassName + SUFFIX);
        Writer writer = jfo.openWriter();
        JavaWriter jw = new JavaWriter(writer);
        PackageElement pkg = elementUtils.getPackageOf(superClassElement);
        if (!pkg.isUnnamed()) {
            jw.emitPackage(pkg.getQualifiedName().toString());
            jw.emitEmptyLine();
        } else {
            jw.emitPackage("");
        }

        jw.beginType(factoryClassName, "class", EnumSet.of(Modifier.PUBLIC));
        jw.emitEmptyLine();
        jw.beginMethod(mQualifiedSuperClassName, "create", EnumSet.of(Modifier.PUBLIC), "String", "id");

        jw.beginControlFlow("if (id == null)");
        jw.emitStatement("throw new IllegalArgumentException(\"id is null!\")");
        jw.endControlFlow();

        for (FactoryAnnotatedClass item : mMap.values()) {
            jw.beginControlFlow("if (\"%s\".equals(id))", item.getId());
            jw.emitStatement("return new %s()", item.getTypeElement().getQualifiedName().toString());
            jw.endControlFlow();
            jw.emitEmptyLine();
        }

        jw.emitStatement("throw new IllegalArgumentException(\"Unknown id = \" + id)");
        jw.endMethod();
        jw.endType();
        jw.close();

        System.out.println("generateCode end");
    }
}
