package stroom.annotations.service.resources;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.annotations.service.hibernate.Annotation;
import stroom.datasource.api.v2.DataSource;
import stroom.datasource.api.v2.DataSourceField;
import stroom.query.api.v2.*;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

public class QueryResourceImpl implements QueryResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryResourceImpl.class);

    private final SessionFactory database;

    static final int QUERY_LIMIT = 10;

    public QueryResourceImpl(SessionFactory database) {
        this.database = database;
    }

    @Override
    public Response getDataSource(final DocRef docRef) {
        final List<DataSourceField> fields = new ArrayList<>();

        fields.add(
                new DataSourceField(
                        DataSourceField.DataSourceFieldType.ID,
                        Annotation.ID,
                        true,
                        Collections.singletonList(
                                ExpressionTerm.Condition.EQUALS
                        )
                )
        );
        fields.add(
                new DataSourceField(
                        DataSourceField.DataSourceFieldType.FIELD,
                        Annotation.STATUS,
                        true,
                        Collections.singletonList(
                                ExpressionTerm.Condition.EQUALS
                        )
                )
        );
        fields.add(
                new DataSourceField(
                        DataSourceField.DataSourceFieldType.FIELD,
                        Annotation.CONTENT,
                        true,
                        Arrays.asList(
                                ExpressionTerm.Condition.EQUALS,
                                ExpressionTerm.Condition.CONTAINS
                        )
                )
        );
        fields.add(
                new DataSourceField(
                        DataSourceField.DataSourceFieldType.FIELD,
                        Annotation.UPDATED_BY,
                        true,
                        Arrays.asList(
                                ExpressionTerm.Condition.EQUALS,
                                ExpressionTerm.Condition.CONTAINS
                        )
                )
        );
        fields.add(
                new DataSourceField(
                        DataSourceField.DataSourceFieldType.FIELD,
                        Annotation.ASSIGN_TO,
                        true,
                        Arrays.asList(
                                ExpressionTerm.Condition.EQUALS,
                                ExpressionTerm.Condition.CONTAINS
                        )
                )
        );
        fields.add(
                new DataSourceField(
                        DataSourceField.DataSourceFieldType.DATE_FIELD,
                        Annotation.LAST_UPDATED,
                        true,
                        Arrays.asList(
                                ExpressionTerm.Condition.BETWEEN,
                                ExpressionTerm.Condition.EQUALS,
                                ExpressionTerm.Condition.GREATER_THAN,
                                ExpressionTerm.Condition.GREATER_THAN_OR_EQUAL_TO,
                                ExpressionTerm.Condition.LESS_THAN,
                                ExpressionTerm.Condition.LESS_THAN_OR_EQUAL_TO
                        )
                )
        );

        return Response
                .accepted(new DataSource(fields))
                .build();
    }

    @Override
    public Response search(final SearchRequest request) {

        final Session session = database.getCurrentSession();
        final CriteriaBuilder criteriaBuilder = database.getCurrentSession().getCriteriaBuilder();

        final CriteriaQuery<Annotation> query = criteriaBuilder.createQuery(Annotation.class);
        final Root<Annotation> root = query.from(Annotation.class);

        final List<Predicate> conditions = new ArrayList<>();
        final ExpressionOperator ex = request.getQuery().getExpression();
        for (final ExpressionItem exItem : ex.getChildren()) {
            if (exItem instanceof ExpressionTerm) {
                final ExpressionTerm exTerm = (ExpressionTerm) exItem;
                conditions.add(criteriaBuilder.like(root.get(exTerm.getField()), "%" + exTerm.getValue() + "%"));
            }
        }

        query.where(criteriaBuilder.or(conditions.toArray(new Predicate[]{})));

        List<Annotation> annotations = session.createQuery(query).getResultList();
        annotations.forEach(a -> LOGGER.info("Annotation Found " + a));

        final List<Result> dtos = annotations.stream()
                .map(QueryResourceImpl::annotationToResult)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return Response
                .accepted(new SearchResponse(null, dtos, null, false))
                .build();
    }

    private static Result annotationToResult(final Annotation annotation) {

        return null;
    }

    @Override
    public Response destroy(final QueryKey queryKey) {
        return Response
                .accepted(Boolean.TRUE)
                .build();
    }
}
