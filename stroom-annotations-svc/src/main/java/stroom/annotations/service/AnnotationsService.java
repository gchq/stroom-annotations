package stroom.annotations.service;

import stroom.annotations.model.Annotation;
import stroom.annotations.model.AnnotationHistory;
import stroom.query.audit.security.ServiceUser;
import stroom.query.audit.service.QueryApiException;

import java.util.List;
import java.util.Optional;

public interface AnnotationsService {

    List<Annotation> search(ServiceUser user,
                            String index,
                            String q,
                            Integer seekPosition) throws QueryApiException;

    Optional<Annotation> get(ServiceUser user,
                             String index,
                             String id) throws QueryApiException;

    Optional<List<AnnotationHistory>> getHistory(ServiceUser user,
                                                 String index,
                                                 String id) throws QueryApiException;

    Optional<Annotation> create(ServiceUser user,
                                String index,
                                String id) throws QueryApiException;

    Optional<Annotation> update(ServiceUser user,
                                String index,
                                String id, Annotation annotation) throws QueryApiException;

    Optional<Boolean> remove(ServiceUser user,
                             String index,
                             String id) throws QueryApiException;
}
