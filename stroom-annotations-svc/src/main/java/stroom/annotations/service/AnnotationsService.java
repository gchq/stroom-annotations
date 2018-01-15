package stroom.annotations.service;

import stroom.annotations.hibernate.Annotation;
import stroom.annotations.hibernate.AnnotationHistory;
import stroom.query.audit.security.ServiceUser;
import stroom.util.shared.QueryApiException;

import java.util.List;
import java.util.Optional;

public interface AnnotationsService {

    List<Annotation> search(ServiceUser authenticatedServiceUser,
                            String index,
                            String q,
                            Integer seekPosition) throws QueryApiException;

    Optional<Annotation> get(ServiceUser authenticatedServiceUser,
                             String index,
                             String id) throws QueryApiException;

    Optional<List<AnnotationHistory>> getHistory(ServiceUser authenticatedServiceUser,
                                                 String index,
                                                 String id) throws QueryApiException;

    Optional<Annotation> create(ServiceUser authenticatedServiceUser,
                                String index,
                                String id) throws QueryApiException;

    Optional<Annotation> update(ServiceUser authenticatedServiceUser,
                                String index,
                                String id, Annotation annotation) throws QueryApiException;

    Optional<Boolean> remove(ServiceUser authenticatedServiceUser,
                             String index,
                             String id) throws QueryApiException;
}
