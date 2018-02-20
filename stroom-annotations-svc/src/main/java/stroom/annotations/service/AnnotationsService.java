package stroom.annotations.service;

import stroom.annotations.model.Annotation;
import stroom.annotations.model.AnnotationHistory;
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
