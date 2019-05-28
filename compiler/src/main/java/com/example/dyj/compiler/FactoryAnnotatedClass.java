package com.example.dyj.compiler;

import com.example.dyj.library.Factory;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;

public class FactoryAnnotatedClass {

    private TypeElement mTypeElement;

    private String mQualifiedSuperClassName;

    private String mSimpleTypeName;

    private String mId;

    public FactoryAnnotatedClass(TypeElement typeElement) throws IllegalArgumentException {
        mTypeElement = typeElement;
        Factory factory = typeElement.getAnnotation(Factory.class);
        mId = factory.id();
        if ("".equals(mId)){
            throw new IllegalArgumentException(String.format("id() in @%s for class %s is null or empty! that's not allowed",
                    Factory.class.getSimpleName(), typeElement.getQualifiedName().toString()));
        }

        try {
            Class clazz = factory.type();
            mQualifiedSuperClassName = clazz.getCanonicalName();
            mSimpleTypeName = clazz.getSimpleName();
        } catch (MirroredTypeException met) {
            DeclaredType declaredType = (DeclaredType) met.getTypeMirror();
            TypeElement classTypeElement = (TypeElement) declaredType.asElement();
            mQualifiedSuperClassName =  classTypeElement.getQualifiedName().toString();
            mSimpleTypeName = classTypeElement.getSimpleName().toString();
        }
    }

    public TypeElement getTypeElement() {
        return mTypeElement;
    }

    public String getQualifiedSuperClassName() {
        return mQualifiedSuperClassName;
    }

    public String getSimpleTypeName() {
        return mSimpleTypeName;
    }

    public String getId() {
        return mId;
    }
}
