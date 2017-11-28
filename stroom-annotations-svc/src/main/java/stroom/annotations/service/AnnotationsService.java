package stroom.annotations.service;

import stroom.annotations.hibernate.Annotation;
import stroom.annotations.hibernate.AnnotationHistory;
import stroom.annotations.resources.AnnotationsException;

import java.util.List;

public interface AnnotationsService {

    List<Annotation> search(String index, String q, Integer seekPosition) throws AnnotationsException;

    Annotation get(String index, String id) throws AnnotationsException;

    List<AnnotationHistory> getHistory(String index, String id) throws AnnotationsException;

    Annotation create(String index, String id) throws AnnotationsException;

    Annotation update(String index, String id, Annotation annotation) throws AnnotationsException;

    void remove(String index, String id) throws AnnotationsException;
}
