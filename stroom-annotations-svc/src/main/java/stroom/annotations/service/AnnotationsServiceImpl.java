package stroom.annotations.service;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.annotations.model.Annotation;
import stroom.annotations.model.AnnotationHistory;
import stroom.annotations.model.HistoryOperation;
import stroom.query.audit.security.ServiceUser;
import stroom.query.audit.service.QueryApiException;
import stroom.query.jooq.DocRefJooqEntity;
import stroom.query.jooq.JooqEntity;
import stroom.query.jooq.QueryableJooqEntity;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.jooq.impl.DSL.or;

public class AnnotationsServiceImpl implements AnnotationsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationsServiceImpl.class);

    private final DSLContext database;

    private final Table<Record> annotationTable;
    private final Table<Record> historyTable;

    public static final int SEARCH_PAGE_LIMIT = 10;

    @Inject
    public AnnotationsServiceImpl(final DSLContext jooqConfig) {
        this.database = jooqConfig;
        this.annotationTable = Optional.ofNullable(Annotation.class.getAnnotation(JooqEntity.class))
                .map(JooqEntity::tableName)
                .map(DSL::table)
                .orElseThrow(() -> new IllegalArgumentException("The Annotation Class must be annotated with JooqEntity"));
        this.historyTable = Optional.ofNullable(AnnotationHistory.class.getAnnotation(JooqEntity.class))
                .map(JooqEntity::tableName)
                .map(DSL::table)
                .orElseThrow(() -> new IllegalArgumentException("The Annotation Class must be annotated with JooqEntity"));
    }

    @Override
    public List<Annotation> search(final ServiceUser user,
                                   final String index,
                                   final String q,
                                   final Integer seekPosition) {
        return database.transactionResult(configuration -> {
            LOGGER.info(String.format("Searching the annotations for %s, pagination information (position=%d)",
                    q, seekPosition));

            final String likeTerm = String.format("%%%s%%", q);

            return DSL.using(configuration)
                    .select()
                    .from(annotationTable)
                    .where(QueryableJooqEntity.DATA_SOURCE_UUID_FIELD.equal(index))
                    .and(or(
                            Annotation.ID_FIELD.like(likeTerm),
                            Annotation.CONTENT_FIELD.like(likeTerm),
                            Annotation.ASSIGN_TO_FIELD.like(likeTerm)
                    ))
                    .limit(SEARCH_PAGE_LIMIT)
                    .offset((seekPosition != null) ? seekPosition : 0)
                    .fetch()
                    .into(Annotation.class);
        });
    }

    @Override
    public Optional<Annotation> get(final ServiceUser user,
                                    final String index,
                                    final String id) {
        return database.transactionResult(configuration -> {
            final Annotation result = DSL.using(configuration)
                    .select()
                    .from(annotationTable)
                    .where(QueryableJooqEntity.DATA_SOURCE_UUID_FIELD.equal(index))
                    .and(Annotation.ID_FIELD.equal(id))
                    .fetchOneInto(Annotation.class);
            return Optional.ofNullable(result);
        });
    }

    @Override
    public Optional<List<AnnotationHistory>> getHistory(final ServiceUser user,
                                                        final String index,
                                                        final String id) {
        return database.transactionResult(configuration ->
                Optional.of(DSL.using(configuration).select()
                        .from(historyTable)
                        .where(QueryableJooqEntity.DATA_SOURCE_UUID_FIELD.equal(index))
                        .and(AnnotationHistory.ANNOTATION_ID_FIELD.equal(id))
                        .fetch()
                        .into(AnnotationHistory.class)
                ).filter(a -> a.size() > 0));
    }

    @Override
    public Optional<Annotation> create(final ServiceUser user,
                                       final String index,
                                       final String id) {
        return database.transactionResult(configuration -> {
            final ULong now = ULong.valueOf(System.currentTimeMillis());

            DSL.using(configuration)
                    .insertInto(annotationTable)
                    .columns(
                            QueryableJooqEntity.DATA_SOURCE_UUID_FIELD,
                            DocRefJooqEntity.CREATE_USER_FIELD,
                            DocRefJooqEntity.CREATE_TIME_FIELD,
                            DocRefJooqEntity.UPDATE_USER_FIELD,
                            DocRefJooqEntity.UPDATE_TIME_FIELD,
                            Annotation.ID_FIELD,
                            Annotation.CONTENT_FIELD,
                            Annotation.ASSIGN_TO_FIELD,
                            Annotation.STATUS_FIELD)
                    .values(index,
                            user.getName(),
                            now,
                            user.getName(),
                            now,
                            id,
                            Annotation.DEFAULT_CONTENT,
                            Annotation.DEFAULT_ASSIGNEE,
                            Annotation.DEFAULT_STATUS.toString())
                    .execute();

            final Annotation currentState = updateHistoryAndReturn(configuration, index, id, HistoryOperation.CREATE);

            return Optional.ofNullable(currentState);
        });
    }

    @Override
    public Optional<Annotation> update(final ServiceUser user,
                                       final String index,
                                       final String id,
                                       final Annotation annotationUpdate) {
        return database.transactionResult(configuration -> {
            final ULong now = ULong.valueOf(System.currentTimeMillis());

            int rowsAffected = DSL.using(configuration)
                    .update(annotationTable)
                    .set(Annotation.ASSIGN_TO_FIELD, annotationUpdate.getAssignTo())
                    .set(Annotation.STATUS_FIELD, Optional.ofNullable(annotationUpdate.getStatus()).map(Object::toString).orElse(null))
                    .set(Annotation.CONTENT_FIELD, annotationUpdate.getContent())
                    .set(DocRefJooqEntity.UPDATE_USER_FIELD, user.getName())
                    .set(DocRefJooqEntity.UPDATE_TIME_FIELD, now)
                    .where(QueryableJooqEntity.DATA_SOURCE_UUID_FIELD.equal(index))
                    .and(Annotation.ID_FIELD.equal(id))
                    .execute();

            if (rowsAffected == 0) {
                throw new Exception("Zero rows affected by the update");
            }

            final Annotation currentState = updateHistoryAndReturn(configuration, index, id, HistoryOperation.UPDATE);

            return Optional.of(currentState);
        });
    }

    @Override
    public Optional<Boolean> remove(final ServiceUser user,
                                    final String index,
                                    final String id) {
        return database.transactionResult(configuration -> {
            takeAnnotationHistoryDelete(user, configuration, index, id);

            int rowsAffected = DSL.using(configuration).deleteFrom(annotationTable)
                    .where(QueryableJooqEntity.DATA_SOURCE_UUID_FIELD.equal(index))
                    .and(Annotation.ID_FIELD.equal(id))
                    .execute();

            if (rowsAffected == 0) {
                throw new Exception("Zero rows affected by the update");
            }

            return Optional.of(Boolean.TRUE);
        });
    }

    private Annotation updateHistoryAndReturn(final Configuration jooqConfiguration,
                                        final String index,
                                        final String id,
                                        final HistoryOperation operation) {
        final Annotation currentState = DSL.using(jooqConfiguration)
                .select()
                .from(annotationTable)
                .where(QueryableJooqEntity.DATA_SOURCE_UUID_FIELD.equal(index))
                .and(Annotation.ID_FIELD.equal(id))
                .fetchOneInto(Annotation.class);

        Objects.requireNonNull(currentState);

        DSL.using(jooqConfiguration)
                .insertInto(historyTable)
                .columns(
                        QueryableJooqEntity.DATA_SOURCE_UUID_FIELD,
                        DocRefJooqEntity.CREATE_USER_FIELD,
                        DocRefJooqEntity.CREATE_TIME_FIELD,
                        DocRefJooqEntity.UPDATE_USER_FIELD,
                        DocRefJooqEntity.UPDATE_TIME_FIELD,
                        AnnotationHistory.OPERATION_FIELD,
                        AnnotationHistory.ANNOTATION_ID_FIELD,
                        Annotation.CONTENT_FIELD,
                        Annotation.ASSIGN_TO_FIELD,
                        Annotation.STATUS_FIELD)
                .values(currentState.getDataSourceUuid(),
                        currentState.getCreateUser(),
                        ULong.valueOf(currentState.getCreateTime()),
                        currentState.getUpdateUser(),
                        ULong.valueOf(currentState.getUpdateTime()),
                        operation.toString(),
                        currentState.getId(),
                        currentState.getContent(),
                        currentState.getAssignTo(),
                        currentState.getStatus().toString())
                .execute();

        LOGGER.trace(String.format("History Point Taken for Annotation %s", currentState.getId()));

        return currentState;
    }

    private void takeAnnotationHistoryDelete(final ServiceUser user,
                                             final Configuration jooqConfiguration,
                                             final String index,
                                             final String id) {
        final Annotation currentState = DSL.using(jooqConfiguration)
                .select()
                .from(annotationTable)
                .where(QueryableJooqEntity.DATA_SOURCE_UUID_FIELD.equal(index))
                .and(Annotation.ID_FIELD.equal(id))
                .fetchOneInto(Annotation.class);

        DSL.using(jooqConfiguration)
                .insertInto(historyTable)
                .columns(
                        QueryableJooqEntity.DATA_SOURCE_UUID_FIELD,
                        DocRefJooqEntity.CREATE_USER_FIELD,
                        DocRefJooqEntity.CREATE_TIME_FIELD,
                        DocRefJooqEntity.UPDATE_USER_FIELD,
                        DocRefJooqEntity.UPDATE_TIME_FIELD,
                        AnnotationHistory.OPERATION_FIELD,
                        AnnotationHistory.ANNOTATION_ID_FIELD,
                        Annotation.CONTENT_FIELD,
                        Annotation.ASSIGN_TO_FIELD,
                        Annotation.STATUS_FIELD)
                .values(currentState.getDataSourceUuid(),
                        currentState.getCreateUser(),
                        ULong.valueOf(currentState.getCreateTime()),
                        currentState.getUpdateUser(),
                        ULong.valueOf(currentState.getUpdateTime()),
                        HistoryOperation.DELETE.toString(),
                        currentState.getId(),
                        currentState.getContent(),
                        currentState.getAssignTo(),
                        currentState.getStatus().toString())
                .execute();

        LOGGER.trace(String.format("History Point Taken for Annotation %s", id));
    }
}
