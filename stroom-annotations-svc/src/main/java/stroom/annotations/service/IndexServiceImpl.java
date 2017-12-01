package stroom.annotations.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.annotations.hibernate.AnnotationIndex;
import stroom.query.hibernate.DocRefEntity;
import stroom.util.shared.QueryApiException;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.util.List;

public class IndexServiceImpl implements IndexService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexServiceImpl.class);

    private SessionFactory database;

    @Inject
    public IndexServiceImpl(final SessionFactory database) {
        this.database = database;
    }

    @Override
    public List<AnnotationIndex> getAll() throws QueryApiException {
        try (final Session session = database.openSession()){
            final CriteriaBuilder cb = session.getCriteriaBuilder();
            final CriteriaQuery<AnnotationIndex> cq = cb.createQuery(AnnotationIndex.class);
            final Root<AnnotationIndex> root = cq.from(AnnotationIndex.class);

            cq.select(root);
            cq.distinct(true);

            return session.createQuery(cq).getResultList();
        } catch (final Exception e) {
            LOGGER.warn("Failed to get index list", e);
            throw new QueryApiException(e);
        }
    }

    @Override
    public AnnotationIndex get(final String uuid) throws QueryApiException {
        try (final Session session = database.openSession()){
            final CriteriaBuilder cb = session.getCriteriaBuilder();
            final CriteriaQuery<AnnotationIndex> cq = cb.createQuery(AnnotationIndex.class);
            final Root<AnnotationIndex> root = cq.from(AnnotationIndex.class);

            cq.select(root);
            cq.where(cb.equal(root.get(DocRefEntity.UUID), uuid));
            cq.distinct(true);

            return session.createQuery(cq).getSingleResult();
        } catch (final Exception e) {
            LOGGER.warn("Failed to get index list", e);
            throw new QueryApiException(e);
        }
    }

    @Override
    public AnnotationIndex create(final String uuid,
                                  final String name) throws QueryApiException {
        Transaction tx = null;

        try (final Session session = database.openSession()) {
            tx = session.beginTransaction();

            final AnnotationIndex annotation = new AnnotationIndex.Builder()
                    .uuid(uuid)
                    .name(name)
                    .build();
            session.persist(annotation);

            tx.commit();

            return annotation;
        } catch (final Exception e) {
            if (tx!=null) tx.rollback();
            LOGGER.warn("Failed to get create index", e);

            throw new QueryApiException(e);
        }
    }

    @Override
    public AnnotationIndex copyDocument(final String originalUuid,
                                        final String copyUuid) throws QueryApiException {
        Transaction tx = null;

        try (final Session session = database.openSession()) {
            tx = session.beginTransaction();

            final CriteriaBuilder cb = session.getCriteriaBuilder();
            final CriteriaQuery<AnnotationIndex> cq = cb.createQuery(AnnotationIndex.class);
            final Root<AnnotationIndex> root = cq.from(AnnotationIndex.class);

            cq.select(root);
            cq.where(cb.equal(root.get(DocRefEntity.UUID), originalUuid));
            cq.distinct(true);

            final AnnotationIndex original = session.createQuery(cq).getSingleResult();

            final AnnotationIndex annotation = new AnnotationIndex.Builder()
                    .original(original)
                    .uuid(copyUuid)
                    .build();
            session.persist(annotation);

            tx.commit();

            return annotation;
        } catch (final Exception e) {
            if (tx!=null) tx.rollback();
            LOGGER.warn("Failed to get create index", e);

            throw new QueryApiException(e);
        }
    }

    @Override
    public AnnotationIndex documentMoved(final String uuid) throws QueryApiException {

        // Nothing to worry about here
        return get(uuid);
    }

    @Override
    public AnnotationIndex documentRenamed(final String uuid,
                                           final String name) throws QueryApiException {
        Transaction tx = null;

        try (final Session session = database.openSession()) {
            tx = session.beginTransaction();

            final CriteriaBuilder cb = session.getCriteriaBuilder();
            final CriteriaUpdate<AnnotationIndex> cq = cb.createCriteriaUpdate(AnnotationIndex.class);
            final Root<AnnotationIndex> root = cq.from(AnnotationIndex.class);

            cq.set(root.get(DocRefEntity.NAME), name);

            cq.where(cb.equal(root.get(DocRefEntity.UUID), uuid));

            int rowsAffected = session.createQuery(cq).executeUpdate();

            if (rowsAffected == 0) {
                throw new Exception("Zero rows affected by the update");
            }

            tx.commit();

            return get(uuid);

        } catch (final Exception e) {
            if (tx!=null) tx.rollback();
            LOGGER.warn("Failed to get update annotation", e);
            throw new QueryApiException(e);
        }
    }

    @Override
    public void deleteDocument(final String uuid) throws QueryApiException {
        Transaction tx = null;

        try (final Session session = database.openSession()) {
            tx = session.beginTransaction();

            final CriteriaBuilder cb = session.getCriteriaBuilder();

            final CriteriaDelete<AnnotationIndex> cq = cb.createCriteriaDelete(AnnotationIndex.class);
            final Root<AnnotationIndex> root = cq.from(AnnotationIndex.class);
            cq.where(cb.equal(root.get(DocRefEntity.UUID), uuid));

            int rowsAffected = session.createQuery(cq).executeUpdate();
            if (rowsAffected == 0) {
                throw new Exception("Zero rows affected by the update");
            }

            tx.commit();
        } catch (final Exception e) {
            if (tx!=null) tx.rollback();
            LOGGER.warn("Failed to get create annotation", e);
            throw new QueryApiException(e);
        }
    }
}
