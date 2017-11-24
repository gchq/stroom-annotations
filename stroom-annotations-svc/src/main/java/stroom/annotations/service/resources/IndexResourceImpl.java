package stroom.annotations.service.resources;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.annotations.service.hibernate.Annotation;
import stroom.query.hibernate.QueryableEntity;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.core.Response;
import java.util.List;

public class IndexResourceImpl implements IndexResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexResourceImpl.class);

    private SessionFactory database;

    @Inject
    public IndexResourceImpl(final SessionFactory database) {
        this.database = database;
    }

    @Override
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
}
