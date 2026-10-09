package io.github.filsynyster.blockforge.database.core;

import io.github.filsynyster.blockforge.database.core.repository.DataRepository;
import io.github.filsynyster.blockforge.database.providers.ormlite.configuration.Context;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class ClassScanner {

    private final String rootPackage;
    private final String entityPackage;

    public ClassScanner(Context config) {
        this.rootPackage = config.getContextConfig().getRootPackage();
        if (config.getEntityConfig().isClassesEmpty()) {
            this.entityPackage = rootPackage;
        } else {
            this.entityPackage = config.getEntityConfig().getEntityClasses()[0].getValue();
        }
    }

    public List<Class<?>> findClassesWithAnnotation(Class<? extends Annotation> annotation) {

        List<Class<?>> ret = new ArrayList<>();
        try (ScanResult scanResult = new ClassGraph()
                .enableAnnotationInfo()
                .acceptPackages(entityPackage)
                .scan()
        ) {

            ClassInfoList classes = scanResult.getClassesWithAnnotation(annotation);

            for (ClassInfo classInfo : classes) {
                ret.add(classInfo.loadClass());
            }
        }

        return ret;
    }

    /**
     * Localiza todas as subinterfaces de {@link DataRepository} disponíveis
     * no pacote raiz configurado para a varredura do classpath.
     * <p>
     * A busca é realizada por meio do ClassGraph, identificando interfaces
     * que possuem {@code DataRepository} como interface de origem. As
     * interfaces encontradas são carregadas e retornadas como objetos
     * {@link Class}.
     *
     * @return uma lista contendo as classes que representam subinterfaces de
     *         {@link DataRepository}; caso nenhuma seja encontrada, retorna
     *         uma lista vazia.
     */
    public List<Class<?>> findDataRepositoryInterfaces() {

        Class<?> clazz = DataRepository.class;

        List<Class<?>> ret = new ArrayList<>();

        try (ScanResult result = new ClassGraph().acceptPackages(rootPackage).enableClassInfo().scan()) {

            ClassInfoList interfaces = result.getAllInterfaces();

            for (ClassInfo info : interfaces) {

                if (info.implementsInterface(clazz.getName())) {
                    ret.add(info.loadClass());
                }
            }
        }

        return ret;
    }

}
