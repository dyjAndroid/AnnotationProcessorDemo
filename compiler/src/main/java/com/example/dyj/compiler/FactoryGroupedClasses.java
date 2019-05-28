package com.example.dyj.compiler;

import com.example.dyj.compiler.exception.IdAlreadyUsedException;

import java.util.LinkedHashMap;
import java.util.Map;

public class FactoryGroupedClasses {

    private String mQualifiedSuperClassName;

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
}
