package stroom.annotations.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.annotations.hibernate.AnnotationsDocRefEntity;
import stroom.query.api.v2.DocRef;
import stroom.query.api.v2.DocRefInfo;
import stroom.query.audit.ExportDTO;
import stroom.query.audit.service.DocRefService;
import stroom.query.hibernate.DocRefEntity;
import stroom.util.shared.QueryApiException;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AnnotationsDocRefServiceImpl implements DocRefService<AnnotationsDocRefEntity> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationsDocRefServiceImpl.class);

    private SessionFactory database;

    @Inject
    public AnnotationsDocRefServiceImpl(final SessionFactory database) {
        this.database = database;
    }

    @Override
    public List<AnnotationsDocRefEntity> getAll() throws QueryApiException {
        try (final Session session = database.openSession()){
            final CriteriaBuilder cb = session.getCriteriaBuilder();
            final CriteriaQuery<AnnotationsDocRefEntity> cq = cb.createQuery(AnnotationsDocRefEntity.class);
            final Root<AnnotationsDocRefEntity> root = cq.from(AnnotationsDocRefEntity.class);

            cq.select(root);
            cq.distinct(true);

            return session.createQuery(cq).getResultList();
        } catch (final Exception e) {
            LOGGER.warn("Failed to get index list", e);
            throw new QueryApiException(e);
        }
    }

    @Override
    public Optional<AnnotationsDocRefEntity> get(final String uuid) throws QueryApiException {
        try (final Session session = database.openSession()){
            final CriteriaBuilder cb = session.getCriteriaBuilder();
            final CriteriaQuery<AnnotationsDocRefEntity> cq = cb.createQuery(AnnotationsDocRefEntity.class);
            final Root<AnnotationsDocRefEntity> root = cq.from(AnnotationsDocRefEntity.class);

            cq.select(root);
            cq.where(cb.equal(root.get(DocRefEntity.UUID), uuid));
            cq.distinct(true);

            return Optional.of(session.createQuery(cq).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (final Exception e) {
            LOGGER.warn("Failed to get index list", e);
            throw new QueryApiException(e);
        }
    }

    @Override
    public Optional<DocRefInfo> getInfo(final String uuid) throws QueryApiException {
        final Optional<AnnotationsDocRefEntity> oIndex = get(uuid);

        return oIndex.flatMap(index -> Optional.of(new DocRefInfo.Builder()
                .docRef(new DocRef.Builder()
                        .uuid(index.getUuid())
                        .name(index.getName())
                        .build())
                .build()));
    }

    @Override
    public Optional<AnnotationsDocRefEntity> createDocument(final String uuid,
                                                            final String name) throws QueryApiException {
        Transaction tx = null;

        try (final Session session = database.openSession()) {
            tx = session.beginTransaction();

            final AnnotationsDocRefEntity annotation = new AnnotationsDocRefEntity.Builder()
                    .uuid(uuid)
                    .name(name)
                    .build();
            session.persist(annotation);

            tx.commit();

            return Optional.of(annotation);
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (final Exception e) {
            if (tx!=null) tx.rollback();
            LOGGER.warn("Failed to get create index", e);

            throw new QueryApiException(e);
        }
    }

    @Override
    public Optional<AnnotationsDocRefEntity> update(final String uuid,
                                                    final AnnotationsDocRefEntity updatedConfig) throws QueryApiException {
        return get(uuid);
    }

    @Override
    public Optional<AnnotationsDocRefEntity> copyDocument(final String originalUuid,
                                                          final String copyUuid) throws QueryApiException {
        Transaction tx = null;

        try (final Session session = database.openSession()) {
            tx = session.beginTransaction();

            final CriteriaBuilder cb = session.getCriteriaBuilder();
            final CriteriaQuery<AnnotationsDocRefEntity> cq = cb.createQuery(AnnotationsDocRefEntity.class);
            final Root<AnnotationsDocRefEntity> root = cq.from(AnnotationsDocRefEntity.class);

            cq.select(root);
            cq.where(cb.equal(root.get(DocRefEntity.UUID), originalUuid));
            cq.distinct(true);

            final AnnotationsDocRefEntity original = session.createQuery(cq).getSingleResult();

            final AnnotationsDocRefEntity annotation = new AnnotationsDocRefEntity.Builder()
                    .original(original)
                    .uuid(copyUuid)
                    .build();
            session.persist(annotation);

            tx.commit();

            return Optional.of(annotation);
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (final Exception e) {
            if (tx!=null) tx.rollback();
            LOGGER.warn("Failed to get create index", e);

            throw new QueryApiException(e);
        }
    }

    @Override
    public Optional<AnnotationsDocRefEntity> documentMoved(final String uuid) throws QueryApiException {

        // Nothing to worry about here
        return get(uuid);
    }

    @Override
    public Optional<AnnotationsDocRefEntity> documentRenamed(final String uuid,
                                                             final String name) throws QueryApiException {
        Transaction tx = null;

        try (final Session session = database.openSession()) {
            tx = session.beginTransaction();

            final CriteriaBuilder cb = session.getCriteriaBuilder();
            final CriteriaUpdate<AnnotationsDocRefEntity> cq = cb.createCriteriaUpdate(AnnotationsDocRefEntity.class);
            final Root<AnnotationsDocRefEntity> root = cq.from(AnnotationsDocRefEntity.class);

            cq.set(root.get(DocRefEntity.NAME), name);

            cq.where(cb.equal(root.get(DocRefEntity.UUID), uuid));

            int rowsAffected = session.createQuery(cq).executeUpdate();

            if (rowsAffected == 0) {
                throw new Exception("Zero rows affected by the update");
            }

            tx.commit();

            return get(uuid);

        } catch (NoResultException e) {
            return Optional.empty();
        } catch (final Exception e) {
            if (tx!=null) tx.rollback();
            LOGGER.warn("Failed to get update annotation", e);
            throw new QueryApiException(e);
        }
    }

    @Override
    public Optional<Boolean> deleteDocument(final String uuid) throws QueryApiException {
        Transaction tx = null;

        try (final Session session = database.openSession()) {
            tx = session.beginTransaction();

            final CriteriaBuilder cb = session.getCriteriaBuilder();

            final CriteriaDelete<AnnotationsDocRefEntity> cq = cb.createCriteriaDelete(AnnotationsDocRefEntity.class);
            final Root<AnnotationsDocRefEntity> root = cq.from(AnnotationsDocRefEntity.class);
            cq.where(cb.equal(root.get(DocRefEntity.UUID), uuid));

            int rowsAffected = session.createQuery(cq).executeUpdate();
            if (rowsAffected == 0) {
                throw new Exception("Zero rows affected by the update");
            }

            tx.commit();

            return Optional.of(Boolean.TRUE);
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (final Exception e) {
            if (tx!=null) tx.rollback();
            LOGGER.warn("Failed to get create annotation", e);
            throw new QueryApiException(e);
        }
    }

    @Override
    public ExportDTO exportDocument(final String uuid) throws QueryApiException {
        final Optional<AnnotationsDocRefEntity> index = get(uuid);

        return new ExportDTO.Builder()
                .message(index.isPresent() ? "is present" : "could not find document")
                .build();
    }

    @Override
    public Optional<AnnotationsDocRefEntity> importDocument(final String uuid,
                                                            final String name,
                                                            final Boolean confirmed,
                                                            final Map<String, String> dataMap) throws QueryApiException {
        if (confirmed) {
            return createDocument(uuid, name);
        } else {
            final Optional<AnnotationsDocRefEntity> existing = get(uuid);

            if (null != existing) {
                return null;
            } else {
                return Optional.of(new AnnotationsDocRefEntity.Builder().uuid(uuid).name(name).build());
            }
        }
    }
}
