package stroom.annotations.service;

import stroom.annotations.hibernate.Annotation;
import stroom.annotations.hibernate.AnnotationHistory;
import stroom.util.shared.QueryApiException;

import java.util.List;

public interface AnnotationsService {

    List<Annotation> search(String index, String q, Integer seekPosition) throws QueryApiException;

    Annotation get(String index, String id) throws QueryApiException;

    List<AnnotationHistory> getHistory(String index, String id) throws QueryApiException;

    Annotation create(String index, String id) throws QueryApiException;

    Annotation update(String index, String id, Annotation annotation) throws QueryApiException;

    void remove(String index, String id) throws QueryApiException;
}
