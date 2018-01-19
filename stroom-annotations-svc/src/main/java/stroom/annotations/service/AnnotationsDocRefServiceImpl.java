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
import stroom.query.audit.security.ServiceUser;
import stroom.query.audit.service.DocRefEntity;
import stroom.query.audit.service.DocRefService;
import stroom.query.hibernate.DocRefHibernateEntity;

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
    public String getType() {
        return AnnotationsDocRefEntity.TYPE;
    }

    @Override
    public List<AnnotationsDocRefEntity> getAll(final ServiceUser user) throws Exception {
        try (final Session session = database.openSession()){
            final CriteriaBuilder cb = session.getCriteriaBuilder();
            final CriteriaQuery<AnnotationsDocRefEntity> cq = cb.createQuery(AnnotationsDocRefEntity.class);
            final Root<AnnotationsDocRefEntity> root = cq.from(AnnotationsDocRefEntity.class);

            cq.select(root);
            cq.distinct(true);

            return session.createQuery(cq).getResultList();
        } catch (final Exception e) {
            LOGGER.warn("Failed to get index list", e);
            throw e;
        }
    }

    @Override
    public Optional<AnnotationsDocRefEntity> get(final ServiceUser user, final String uuid) throws Exception {
        try (final Session session = database.openSession()){
            final CriteriaBuilder cb = session.getCriteriaBuilder();
            final CriteriaQuery<AnnotationsDocRefEntity> cq = cb.createQuery(AnnotationsDocRefEntity.class);
            final Root<AnnotationsDocRefEntity> root = cq.from(AnnotationsDocRefEntity.class);

            cq.select(root);
            cq.where(cb.equal(root.get(DocRefHibernateEntity.UUID), uuid));
            cq.distinct(true);

            return Optional.of(session.createQuery(cq).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (final Exception e) {
            LOGGER.warn("Failed to get index list", e);
            throw e;
        }
    }

    @Override
    public Optional<DocRefInfo> getInfo(final ServiceUser user, final String uuid) throws Exception {
        final Optional<AnnotationsDocRefEntity> oIndex = get(user, uuid);

        return oIndex.flatMap(index -> Optional.of(new DocRefInfo.Builder()
                .docRef(new DocRef.Builder()
                        .uuid(index.getUuid())
                        .name(index.getName())
                        .build())
                .createTime(index.getCreateTime())
                .createUser(index.getCreateUser())
                .updateTime(index.getUpdateTime())
                .updateUser(index.getUpdateUser())
                .build()));
    }

    @Override
    public Optional<AnnotationsDocRefEntity> createDocument(final ServiceUser user,
                                                            final String uuid,
                                                            final String name) throws Exception {
        Transaction tx = null;

        try (final Session session = database.openSession()) {
            tx = session.beginTransaction();

            final Long now = System.currentTimeMillis();

            final AnnotationsDocRefEntity annotation = new AnnotationsDocRefEntity.Builder()
                    .uuid(uuid)
                    .name(name)
                    .createTime(now)
                    .createUser(user.getName())
                    .updateTime(now)
                    .updateUser(user.getName())
                    .build();
            session.persist(annotation);

            tx.commit();

            return Optional.of(annotation);
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (final Exception e) {
            if (tx!=null) tx.rollback();
            LOGGER.warn("Failed to get create index", e);

            throw e;
        }
    }

    @Override
    public Optional<AnnotationsDocRefEntity> update(final ServiceUser user,
                                                    final String uuid,
                                                    final AnnotationsDocRefEntity updatedConfig) throws Exception {
        return get(user, uuid);
    }

    @Override
    public Optional<AnnotationsDocRefEntity> copyDocument(final ServiceUser user,
                                                          final String originalUuid,
                                                          final String copyUuid) throws Exception {
        Transaction tx = null;

        try (final Session session = database.openSession()) {
            tx = session.beginTransaction();

            final CriteriaBuilder cb = session.getCriteriaBuilder();
            final CriteriaQuery<AnnotationsDocRefEntity> cq = cb.createQuery(AnnotationsDocRefEntity.class);
            final Root<AnnotationsDocRefEntity> root = cq.from(AnnotationsDocRefEntity.class);

            cq.select(root);
            cq.where(cb.equal(root.get(DocRefHibernateEntity.UUID), originalUuid));
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

            throw e;
        }
    }

    @Override
    public Optional<AnnotationsDocRefEntity> moveDocument(final ServiceUser user,
                                                          final String uuid) throws Exception {

        // Nothing to worry about here
        return get(user, uuid);
    }

    @Override
    public Optional<AnnotationsDocRefEntity> renameDocument(final ServiceUser user,
                                                            final String uuid,
                                                            final String name) throws Exception {
        Transaction tx = null;

        try (final Session session = database.openSession()) {
            final Long now = System.currentTimeMillis();

            tx = session.beginTransaction();

            final CriteriaBuilder cb = session.getCriteriaBuilder();
            final CriteriaUpdate<AnnotationsDocRefEntity> cq = cb.createCriteriaUpdate(AnnotationsDocRefEntity.class);
            final Root<AnnotationsDocRefEntity> root = cq.from(AnnotationsDocRefEntity.class);

            cq.set(root.get(DocRefHibernateEntity.NAME), name);
            cq.set(root.get(DocRefHibernateEntity.UPDATE_USER), user.getName());
            cq.set(root.get(DocRefHibernateEntity.UPDATE_TIME), now);

            cq.where(cb.equal(root.get(DocRefHibernateEntity.UUID), uuid));

            int rowsAffected = session.createQuery(cq).executeUpdate();

            if (rowsAffected == 0) {
                throw new Exception("Zero rows affected by the update");
            }

            tx.commit();

            return get(user, uuid);

        } catch (NoResultException e) {
            return Optional.empty();
        } catch (final Exception e) {
            if (tx!=null) tx.rollback();
            LOGGER.warn("Failed to get update annotation", e);
            throw e;
        }
    }

    @Override
    public Optional<Boolean> deleteDocument(final ServiceUser user,
                                            final String uuid) throws Exception {
        Transaction tx = null;

        try (final Session session = database.openSession()) {
            tx = session.beginTransaction();

            final CriteriaBuilder cb = session.getCriteriaBuilder();

            final CriteriaDelete<AnnotationsDocRefEntity> cq = cb.createCriteriaDelete(AnnotationsDocRefEntity.class);
            final Root<AnnotationsDocRefEntity> root = cq.from(AnnotationsDocRefEntity.class);
            cq.where(cb.equal(root.get(DocRefHibernateEntity.UUID), uuid));

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
            throw e;
        }
    }

    @Override
    public ExportDTO exportDocument(final ServiceUser user,
                                    final String uuid) throws Exception {
        final Optional<AnnotationsDocRefEntity> optionalIndex = get(user, uuid);

        return optionalIndex.map(index -> new ExportDTO.Builder()
                    .value(DocRefEntity.NAME, index.getName())
                    .build())
                .orElse(new ExportDTO.Builder()
                        .message("could not find document")
                        .build());
    }

    @Override
    public Optional<AnnotationsDocRefEntity> importDocument(final ServiceUser user,
                                                            final String uuid,
                                                            final String name,
                                                            final Boolean confirmed,
                                                            final Map<String, String> dataMap) throws Exception {
        if (confirmed) {
            return createDocument(user, uuid, name);
        } else {
            final Optional<AnnotationsDocRefEntity> existing = get(user, uuid);

            if (null != existing) {
                return null;
            } else {
                return Optional.of(new AnnotationsDocRefEntity.Builder().uuid(uuid).name(name).build());
            }
        }
    }
}
