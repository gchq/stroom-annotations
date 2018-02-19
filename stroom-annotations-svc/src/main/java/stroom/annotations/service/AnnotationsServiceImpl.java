package stroom.annotations.service;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.annotations.hibernate.Annotation;
import stroom.annotations.hibernate.AnnotationHistory;
import stroom.annotations.hibernate.HistoryOperation;
import stroom.annotations.hibernate.Status;
import stroom.query.audit.model.QueryableEntity;
import stroom.query.audit.security.ServiceUser;
import stroom.query.jooq.DocRefJooqEntity;
import stroom.query.jooq.JooqEntity;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.or;

public class AnnotationsServiceImpl implements AnnotationsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationsServiceImpl.class);

    private final DSLContext database;

    private final Table<Record> annotationTable;
    private final Table<Record> historyTable;

    public static final int SEARCH_PAGE_LIMIT = 10;

    @Inject
    public AnnotationsServiceImpl(final Configuration jooqConfig) {
        this.database = DSL.using(jooqConfig);
        this.annotationTable = Optional.ofNullable(Annotation.class.getAnnotation(JooqEntity.class))
                .map(JooqEntity::tableName)
                .map(DSL::table)
                .orElseThrow(() -> new IllegalArgumentException("The Annotation Class must be annotated with JooqEntity"));
        this.historyTable = Optional.ofNullable(AnnotationHistory.class.getAnnotation(JooqEntity.class))
                .map(JooqEntity::tableName)
                .map(DSL::table)
                .orElseThrow(() -> new IllegalArgumentException("The Annotation Class must be annotated with JooqEntity"));
    }

    private Annotation convertRecord(final Record record) {
        return new Annotation.Builder()
                .id(record.getValue(field(Annotation.ID, String.class)))
                .dataSourceUuid(record.getValue(field(QueryableEntity.DATA_SOURCE_UUID, String.class)))
                .assignTo(record.getValue(field(Annotation.ASSIGN_TO, String.class)))
                .content(record.getValue(field(Annotation.CONTENT, String.class)))
                .status(record.getValue(field(Annotation.STATUS, String.class)))
                .createUser(record.getValue(DocRefJooqEntity.CREATE_USER_FIELD))
                .createTime(record.getValue(DocRefJooqEntity.CREATE_TIME_FIELD).longValue())
                .updateUser(record.getValue(DocRefJooqEntity.UPDATE_USER_FIELD))
                .updateTime(record.getValue(DocRefJooqEntity.UPDATE_TIME_FIELD).longValue())
                .build();
    }

    @Override
    public List<Annotation> search(final ServiceUser user,
                                   final String index,
                                   final String q,
                                   final Integer seekPosition) throws Exception {
        return database.transactionResult(configuration -> {
            LOGGER.info(String.format("Searching the annotations for %s, pagination information (position=%d)",
                    q, seekPosition));

            final String likeTerm = String.format("%%%s%%", q);

            return DSL.using(configuration)
                    .select()
                    .from(annotationTable)
                    .where(field(QueryableEntity.DATA_SOURCE_UUID, String.class).equal(index))
                    .and(or(
                            field(Annotation.ID, String.class).like(likeTerm),
                            field(Annotation.CONTENT, String.class).like(likeTerm),
                            field(Annotation.ASSIGN_TO, String.class).like(likeTerm)
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
                                    final String id) throws Exception {
        return database.transactionResult(configuration -> {
            final Annotation result = DSL.using(configuration)
                    .select()
                    .from(annotationTable)
                    .where(field(QueryableEntity.DATA_SOURCE_UUID, String.class).equal(index))
                    .and(field(Annotation.ID, String.class).equal(id))
                    .fetchOneInto(Annotation.class);
            return Optional.ofNullable(result);
        });
    }

    @Override
    public Optional<List<AnnotationHistory>> getHistory(final ServiceUser user,
                                                        final String index,
                                                        final String id) throws Exception {
        return database.transactionResult(configuration ->
                Optional.of(DSL.using(configuration).select()
                        .from(historyTable)
                        .where(field(QueryableEntity.DATA_SOURCE_UUID, String.class).equal(index))
                        .and(field(AnnotationHistory.ANNOTATION_ID, String.class).equal(id))
                        .fetch()
                        .into(AnnotationHistory.class)
                ).filter(a -> a.size() > 0));
    }

    @Override
    public Optional<Annotation> create(final ServiceUser user,
                                       final String index,
                                       final String id) throws Exception {
        return database.transactionResult(configuration -> {
            final ULong now = ULong.valueOf(System.currentTimeMillis());

            DSL.using(configuration)
                    .insertInto(annotationTable)
                    .columns(
                            field(QueryableEntity.DATA_SOURCE_UUID, String.class),
                            DocRefJooqEntity.CREATE_USER_FIELD,
                            DocRefJooqEntity.CREATE_TIME_FIELD,
                            DocRefJooqEntity.UPDATE_USER_FIELD,
                            DocRefJooqEntity.UPDATE_TIME_FIELD,
                            field(Annotation.ID, String.class),
                            field(Annotation.CONTENT, String.class),
                            field(Annotation.ASSIGN_TO, String.class),
                            field(Annotation.STATUS, String.class))
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
                                       final Annotation annotationUpdate) throws Exception {
        return database.transactionResult(configuration -> {
            final ULong now = ULong.valueOf(System.currentTimeMillis());

            int rowsAffected = DSL.using(configuration)
                    .update(annotationTable)
                    .set(field(Annotation.ASSIGN_TO, String.class), annotationUpdate.getAssignTo())
                    .set(field(Annotation.STATUS, String.class), Optional.ofNullable(annotationUpdate.getStatus()).map(Object::toString).orElse(null))
                    .set(field(Annotation.CONTENT, String.class), annotationUpdate.getContent())
                    .set(DocRefJooqEntity.UPDATE_USER_FIELD, user.getName())
                    .set(DocRefJooqEntity.UPDATE_TIME_FIELD, now)
                    .where(field(QueryableEntity.DATA_SOURCE_UUID, String.class).equal(index))
                    .and(field(Annotation.ID, String.class).equal(id))
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
                                    final String id) throws Exception {
        return database.transactionResult(configuration -> {
            takeAnnotationHistoryDelete(user, configuration, index, id);

            int rowsAffected = DSL.using(configuration).deleteFrom(annotationTable)
                    .where(field(QueryableEntity.DATA_SOURCE_UUID, String.class).equal(index))
                    .and(field(Annotation.ID, String.class).equal(id))
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
                .where(field(QueryableEntity.DATA_SOURCE_UUID, String.class).equal(index))
                .and(field(Annotation.ID, String.class).equal(id))
                .fetchOneInto(Annotation.class);

        Objects.requireNonNull(currentState);

        DSL.using(jooqConfiguration)
                .insertInto(historyTable)
                .columns(
                        field(QueryableEntity.DATA_SOURCE_UUID, String.class),
                        DocRefJooqEntity.CREATE_USER_FIELD,
                        DocRefJooqEntity.CREATE_TIME_FIELD,
                        DocRefJooqEntity.UPDATE_USER_FIELD,
                        DocRefJooqEntity.UPDATE_TIME_FIELD,
                        field(AnnotationHistory.OPERATION, String.class),
                        field(AnnotationHistory.ANNOTATION_ID, String.class),
                        field(Annotation.CONTENT, String.class),
                        field(Annotation.ASSIGN_TO, String.class),
                        field(Annotation.STATUS, String.class))
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
                .where(field(QueryableEntity.DATA_SOURCE_UUID, String.class).equal(index))
                .and(field(Annotation.ID, String.class).equal(id))
                .fetchOneInto(Annotation.class);

        DSL.using(jooqConfiguration)
                .insertInto(historyTable)
                .columns(
                        field(QueryableEntity.DATA_SOURCE_UUID, String.class),
                        DocRefJooqEntity.CREATE_USER_FIELD,
                        DocRefJooqEntity.CREATE_TIME_FIELD,
                        DocRefJooqEntity.UPDATE_USER_FIELD,
                        DocRefJooqEntity.UPDATE_TIME_FIELD,
                        field(AnnotationHistory.OPERATION, String.class),
                        field(AnnotationHistory.ANNOTATION_ID, String.class),
                        field(Annotation.CONTENT, String.class),
                        field(Annotation.ASSIGN_TO, String.class),
                        field(Annotation.STATUS, String.class))
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
