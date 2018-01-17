package stroom.annotations.service;

import stroom.annotations.hibernate.Annotation;
import stroom.annotations.hibernate.AnnotationHistory;
import stroom.query.audit.security.ServiceUser;

import java.util.List;
import java.util.Optional;

public interface AnnotationsService {

    List<Annotation> search(ServiceUser user,
                            String index,
                            String q,
                            Integer seekPosition) throws Exception;

    Optional<Annotation> get(ServiceUser user,
                             String index,
                             String id) throws Exception;

    Optional<List<AnnotationHistory>> getHistory(ServiceUser user,
                                                 String index,
                                                 String id) throws Exception;

    Optional<Annotation> create(ServiceUser user,
                                String index,
                                String id) throws Exception;

    Optional<Annotation> update(ServiceUser user,
                                String index,
                                String id, Annotation annotation) throws Exception;

    Optional<Boolean> remove(ServiceUser user,
                             String index,
                             String id) throws Exception;
}
