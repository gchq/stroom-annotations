package stroom.annotations.service.resources;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.annotations.service.hibernate.Annotation;
import stroom.annotations.service.hibernate.AnnotationHistory;
import stroom.annotations.service.model.*;
import stroom.query.hibernate.QueryableEntity;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnnotationsResourceImpl implements AnnotationsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationsResourceImpl.class);

    static final String WELCOME_TEXT = "Welcome to the annotations service";

    static final int SEARCH_PAGE_LIMIT = 10;

    private SessionFactory database;

    @Inject
    public AnnotationsResourceImpl(final SessionFactory database) {
        this.database = database;
    }

    public final Response welcome() throws AnnotationsException {
        return Response.ok(WELCOME_TEXT)
                .build();
    }

    public final Response statusValues() throws AnnotationsException {
        final Map<String, String> statusValues = Arrays.stream(Status.values())
                .collect(Collectors.toMap(Object::toString, Status::getDisplayText));

        return Response.ok(statusValues)
                .build();
    }

    public final Response search(final String index,
                                 final String q,
                                 final Integer seekPosition) throws AnnotationsException {
        try (final Session session = database.openSession()){
            LOGGER.info(String.format("Searching the annotations for %s, pagination information (position=%d)",
                    q, seekPosition));

            final CriteriaBuilder cb = session.getCriteriaBuilder();

            final CriteriaQuery<Annotation> cq = cb.createQuery(Annotation.class);
            final Root<Annotation> root = cq.from(Annotation.class);

            final String likeTerm = String.format("%%%s%%", q);

            cq.where(
                cb.and(
                    cb.equal(root.get(QueryableEntity.DATA_SOURCE_UUID), index),
                    cb.or(
                        cb.like(root.get(Annotation.ID), likeTerm),
                        cb.like(root.get(Annotation.CONTENT), likeTerm),
                        cb.like(root.get(Annotation.ASSIGN_TO), likeTerm)
                    )
                )
            );

            cq.orderBy(cb.desc(root.get(Annotation.ID)));

            final List<AnnotationDTO> dtos = session.createQuery(cq)
                    .setMaxResults(SEARCH_PAGE_LIMIT)
                    .setFirstResult((seekPosition != null) ? seekPosition : 0)
                    .getResultList()
                    .stream()
                    .map(AnnotationDTOMarshaller::toDTO)
                    .collect(Collectors.toList());

            return Response.ok(dtos)
                    .build();

        } catch (final Exception e) {
            LOGGER.warn("Failed to search for annotations", e);
            throw new AnnotationsException(e);
        }
    }

    //@Override
    public Response getIndexes() throws AnnotationsException {
        try (final Session session = database.openSession()){
            final CriteriaBuilder cb = session.getCriteriaBuilder();
            final CriteriaQuery<String> cq = cb.createQuery(String.class);
            final Root<Annotation> root = cq.from(Annotation.class);

            cq.select(root.get(QueryableEntity.DATA_SOURCE_UUID));
            cq.distinct(true);

            final List<String> indexUuids = session.createQuery(cq).getResultList();

            return Response.ok(indexUuids).build();

        } catch (final Exception e) {
            LOGGER.warn("Failed to get index list", e);
            throw new AnnotationsException(e);
        }
    }

    public final Response get(final String index,
                              final String id) throws AnnotationsException {
        try (final Session session = database.openSession()){
            return get(session, index, id);
        } catch (final Exception e) {
            LOGGER.warn("Failed to get annotation", e);
            throw new AnnotationsException(e);
        }
    }

    private Annotation getEntity(final Session session, final String index, final String id) {
        final CriteriaBuilder cb = session.getCriteriaBuilder();

        final CriteriaQuery<Annotation> cq = cb.createQuery(Annotation.class);
        final Root<Annotation> root = cq.from(Annotation.class);
        cq.where(cb.and(
                cb.equal(root.get(Annotation.ID), id),
                cb.equal(root.get(QueryableEntity.DATA_SOURCE_UUID), index)
        ));

        return session.createQuery(cq).getSingleResult();
    }

    private Response get(final Session session, final String index, final String id) {
        final Annotation result = getEntity(session, index, id);

        final AnnotationDTO annotationDTO = AnnotationDTOMarshaller.toDTO(result);

        return Response.ok(annotationDTO)
                .build();
    }

    public final Response getHistory(final String index,
                                     final String id) throws AnnotationsException {
        try (final Session session = database.openSession()){
            final CriteriaBuilder cb = session.getCriteriaBuilder();
            final CriteriaQuery<AnnotationHistory> cq = cb.createQuery(AnnotationHistory.class);
            final Root<AnnotationHistory> root = cq.from(AnnotationHistory.class);

            cq.where(cb.and(
                cb.equal(root.get(AnnotationHistory.ANNOTATION_ID), id),
                cb.equal(root.get(QueryableEntity.DATA_SOURCE_UUID), index)
            ));

            final List<AnnotationHistoryDTO> results = session.createQuery(cq)
                    .getResultList()
                    .stream()
                    .map(AnnotationDTOMarshaller::toDTO)
                    .collect(Collectors.toList());

            if (results.size() > 0) {
                return Response.ok(results)
                        .build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ResponseMsgDTO.msg("No history found for annotation")
                                .recordsUpdated(0)
                                .build())
                        .build();
            }
        } catch (final Exception e) {
            LOGGER.warn("Failed to get history of annotation", e);
            throw new AnnotationsException(e);
        }
    }

    public final Response create(final String index,
                                 final String id) throws AnnotationsException {

        Transaction tx = null;

        try (final Session session = database.openSession()) {
            tx = session.beginTransaction();

            final Annotation annotation = new Annotation.Builder()
                    .id(id)
                    .dataSourceUuid(index)
                    .lastUpdated(System.currentTimeMillis())
                    .assignTo(AnnotationDTO.DEFAULT_ASSIGNEE)
                    .updatedBy(AnnotationDTO.DEFAULT_UPDATED_BY)
                    .content(AnnotationDTO.DEFAULT_CONTENT)
                    .status(AnnotationDTO.DEFAULT_STATUS)
                    .build();
            session.persist(annotation);

            takeAnnotationHistory(session, annotation, HistoryOperation.CREATE);

            tx.commit();

            final AnnotationDTO annotationDTO = AnnotationDTOMarshaller.toDTO(annotation);

            return Response.ok(annotationDTO)
                    .build();
        } catch (final Exception e) {
            if (tx!=null) tx.rollback();
            LOGGER.warn("Failed to get create annotation", e);

            throw new AnnotationsException(e);
        }
    }

    public final Response update(final String index,
                                 final String id,
                                 final AnnotationDTO annotationDTO) throws AnnotationsException {
        Transaction tx = null;

        try (final Session session = database.openSession()) {
            final Annotation annotation = getEntity(session, index, id);

            tx = session.beginTransaction();

            session.evict(annotation);

            annotation.setLastUpdated(System.currentTimeMillis());
            annotation.setAssignTo(annotationDTO.getAssignTo());
            annotation.setContent(annotationDTO.getContent());
            annotation.setStatus(annotationDTO.getStatus());

            session.merge(annotation);

            takeAnnotationHistory(session, annotation, HistoryOperation.UPDATE);

            tx.commit();

            return get(session, index, id);

        } catch (final Exception e) {
            if (tx!=null) tx.rollback();
            LOGGER.warn("Failed to get update annotation", e);
            throw new AnnotationsException(e);
        }
    }

    public final Response remove(final String index,
                                 final String id) throws AnnotationsException {
        Transaction tx = null;

        try (final Session session = database.openSession()) {

            // Take the history snapshot before deletion happens
            takeAnnotationHistoryDelete(session, index, id, HistoryOperation.DELETE);

            final Annotation annotation = getEntity(session, index, id);

            tx = session.beginTransaction();

            session.delete(annotation);

            tx.commit();

            return Response
                    .ok(ResponseMsgDTO.msg("Annotation deleted")
                            .recordsUpdated(1)
                            .build())
                    .build();
        } catch (final Exception e) {
            if (tx!=null) tx.rollback();
            LOGGER.warn("Failed to get create annotation", e);
            throw new AnnotationsException(e);
        }
    }

    private void takeAnnotationHistory(final Session session,
                                       final Annotation currentState,
                                       final HistoryOperation operation) {
        switch (operation) {
            case CREATE:
            case UPDATE: {
                final AnnotationHistory history = new AnnotationHistory.Builder()
                        .dataSourceUuid(currentState.getDataSourceUuid())
                        .annotationId(currentState.getId())
                        .operation(operation)
                        .lastUpdated(currentState.getLastUpdated())
                        .assignTo(currentState.getAssignTo())
                        .updatedBy(currentState.getUpdatedBy())
                        .content(currentState.getContent())
                        .status(currentState.getStatus())
                        .build();
                session.persist(history);
                break;
            }
        }

        LOGGER.trace(String.format("History Point Taken for Annotation %s", currentState.getId()));
    }

    private void takeAnnotationHistoryDelete(final Session session,
                                       final String index,
                                       final String id,
                                       final HistoryOperation operation) {
        final Annotation currentState = getEntity(session, index, id);

                final AnnotationHistory history = new AnnotationHistory.Builder()
                        .dataSourceUuid(index)
                        .annotationId(id)
                        .operation(HistoryOperation.DELETE)
                        .lastUpdated(System.currentTimeMillis())
                        .assignTo(currentState.getAssignTo())
                        .updatedBy(AnnotationDTO.DEFAULT_UPDATED_BY)
                        .content(currentState.getContent())
                        .status(currentState.getStatus())
                        .build();
                session.persist(history);

        LOGGER.trace(String.format("History Point Taken for Annotation %s", id));
    }
}
