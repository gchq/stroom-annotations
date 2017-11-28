package stroom.annotations.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.annotations.hibernate.Annotation;
import stroom.annotations.hibernate.AnnotationHistory;
import stroom.annotations.hibernate.HistoryOperation;
import stroom.annotations.resources.AnnotationsException;
import stroom.query.hibernate.QueryableEntity;

import javax.inject.Inject;
import javax.persistence.criteria.*;
import java.util.List;

public class AnnotationsServiceImpl implements AnnotationsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationsServiceImpl.class);

    private SessionFactory database;

    public static final int SEARCH_PAGE_LIMIT = 10;

    @Inject
    public AnnotationsServiceImpl(final SessionFactory database) {
        this.database = database;
    }

    @Override
    public List<Annotation> search(final String index,
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

            return session.createQuery(cq)
                    .setMaxResults(SEARCH_PAGE_LIMIT)
                    .setFirstResult((seekPosition != null) ? seekPosition : 0)
                    .getResultList();

        } catch (final Exception e) {
            LOGGER.warn("Failed to search for annotations", e);
            throw new AnnotationsException(e);
        }
    }

    @Override
    public Annotation get(final String index,
                          final String id) throws AnnotationsException {
        try (final Session session = database.openSession()) {
            return getEntity(session, index, id);
        } catch (final Exception e) {
            LOGGER.warn("Failed to get history of annotation", e);
            throw new AnnotationsException(e);
        }
    }

    @Override
    public List<AnnotationHistory> getHistory(final String index,
                                        final String id) throws AnnotationsException {
        try (final Session session = database.openSession()){
            final CriteriaBuilder cb = session.getCriteriaBuilder();
            final CriteriaQuery<AnnotationHistory> cq = cb.createQuery(AnnotationHistory.class);
            final Root<AnnotationHistory> root = cq.from(AnnotationHistory.class);

            cq.where(cb.and(
                    cb.equal(root.get(AnnotationHistory.ANNOTATION_ID), id),
                    cb.equal(root.get(QueryableEntity.DATA_SOURCE_UUID), index)
            ));

            return session.createQuery(cq).getResultList();
        } catch (final Exception e) {
            LOGGER.warn("Failed to get history of annotation", e);
            throw new AnnotationsException(e);
        }
    }

    @Override
    public Annotation create(final String index,
                             final String id) throws AnnotationsException {
        Transaction tx = null;

        try (final Session session = database.openSession()) {
            tx = session.beginTransaction();

            final Annotation annotation = new Annotation.Builder()
                    .id(id)
                    .dataSourceUuid(index)
                    .lastUpdated(System.currentTimeMillis())
                    .assignTo(Annotation.DEFAULT_ASSIGNEE)
                    .updatedBy(Annotation.DEFAULT_UPDATED_BY)
                    .content(Annotation.DEFAULT_CONTENT)
                    .status(Annotation.DEFAULT_STATUS)
                    .build();
            session.persist(annotation);

            final Annotation currentState = updateHistoryAndReturn(session, index, id, HistoryOperation.CREATE);

            tx.commit();

            return currentState;

        } catch (final Exception e) {
            if (tx!=null) tx.rollback();
            LOGGER.warn("Failed to get create annotation", e);

            throw new AnnotationsException(e);
        }
    }

    @Override
    public Annotation update(final String index,
                             final String id,
                             final Annotation annotationUpdate) throws AnnotationsException {
        Transaction tx = null;

        try (final Session session = database.openSession()) {
            tx = session.beginTransaction();

            final CriteriaBuilder cb = session.getCriteriaBuilder();
            final CriteriaUpdate<Annotation> cq = cb.createCriteriaUpdate(Annotation.class);
            final Root<Annotation> root = cq.from(Annotation.class);

            cq.set(root.get(Annotation.LAST_UPDATED), System.currentTimeMillis());
            cq.set(root.get(Annotation.ASSIGN_TO), annotationUpdate.getAssignTo());
            cq.set(root.get(Annotation.CONTENT), annotationUpdate.getContent());
            cq.set(root.get(Annotation.STATUS), annotationUpdate.getStatus());

            cq.where(
                    cb.and(
                            cb.equal(root.get(QueryableEntity.DATA_SOURCE_UUID), index),
                            cb.equal(root.get(Annotation.ID), id)
                    )
            );

            int rowsAffected = session.createQuery(cq).executeUpdate();

            if (rowsAffected == 0) {
                throw new Exception("Zero rows affected by the update");
            }

            final Annotation currentState = updateHistoryAndReturn(session, index, id, HistoryOperation.UPDATE);

            tx.commit();

            return currentState;

        } catch (final Exception e) {
            if (tx!=null) tx.rollback();
            LOGGER.warn("Failed to get update annotation", e);
            throw new AnnotationsException(e);
        }
    }

    @Override
    public void remove(final String index,
                       final String id) throws AnnotationsException {
        Transaction tx = null;

        try (final Session session = database.openSession()) {

            // Take the history snapshot before deletion happens
            takeAnnotationHistoryDelete(session, index, id);

            tx = session.beginTransaction();

            final CriteriaBuilder cb = session.getCriteriaBuilder();

            final CriteriaDelete<Annotation> cq = cb.createCriteriaDelete(Annotation.class);
            final Root<Annotation> root = cq.from(Annotation.class);

            cq.where(
                    cb.and(
                            cb.equal(root.get(QueryableEntity.DATA_SOURCE_UUID), index),
                            cb.equal(root.get(Annotation.ID), id)
                    )
            );

            int rowsAffected = session.createQuery(cq).executeUpdate();
            if (rowsAffected == 0) {
                throw new Exception("Zero rows affected by the update");
            }

            tx.commit();
        } catch (final Exception e) {
            if (tx!=null) tx.rollback();
            LOGGER.warn("Failed to get create annotation", e);
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

    private Annotation updateHistoryAndReturn(final Session session,
                                        final String index,
                                        final String id,
                                        final HistoryOperation operation) {
        final Annotation currentState = getEntity(session, index, id);

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

        LOGGER.trace(String.format("History Point Taken for Annotation %s", currentState.getId()));

        return currentState;
    }

    private void takeAnnotationHistoryDelete(final Session session,
                                             final String index,
                                             final String id) {
        final Annotation currentState = getEntity(session, index, id);

        final AnnotationHistory history = new AnnotationHistory.Builder()
                .dataSourceUuid(index)
                .annotationId(id)
                .operation(HistoryOperation.DELETE)
                .lastUpdated(System.currentTimeMillis())
                .assignTo(currentState.getAssignTo())
                .updatedBy(Annotation.DEFAULT_UPDATED_BY)
                .content(currentState.getContent())
                .status(currentState.getStatus())
                .build();
        session.persist(history);

        LOGGER.trace(String.format("History Point Taken for Annotation %s", id));
    }
}
