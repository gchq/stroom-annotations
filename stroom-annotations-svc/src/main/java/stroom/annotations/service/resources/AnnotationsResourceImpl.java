package stroom.annotations.service.resources;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.annotations.service.model.*;
import stroom.db.annotations.tables.records.AnnotationsRecord;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static stroom.db.annotations.Tables.ANNOTATIONS_;
import static stroom.db.annotations.Tables.ANNOTATIONS_HISTORY;

public class AnnotationsResourceImpl implements AnnotationsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationsResourceImpl.class);

    static final String WELCOME_TEXT = "Welcome to the annotations service";

    static final int SEARCH_PAGE_LIMIT = 10;

    @Context
    @NotNull
    private DSLContext database;

    public AnnotationsResourceImpl() {
    }

    public final Response welcome() {
        return Response.status(Response.Status.OK)
                .entity(WELCOME_TEXT)
                .build();
    }

    public final Response statusValues() {
        final Map<String, String> statusValues = Arrays.stream(Status.values())
                .collect(Collectors.toMap(Object::toString, Status::getDisplayText));

        return Response.status(Response.Status.OK)
                .entity(statusValues)
                .build();
    }

    public final Response search(final String q,
                                 final String seekId,
                                 final Long seekLastUpdated) {

        LOGGER.info(String.format("Searching the annotations for %s, pagination information (id=%s, lastUpdated=%d)",
                q, seekId, seekLastUpdated));

        final List<AnnotationDTO> dtos;
        if ((null != seekId) && (null != seekLastUpdated)) {
            dtos = database.selectFrom(ANNOTATIONS_)
                    .where(ANNOTATIONS_.ID.contains(q))
                    .or(ANNOTATIONS_.CONTENT.contains(q))
                    .or(ANNOTATIONS_.ASSIGNTO.contains(q))
                    .orderBy(ANNOTATIONS_.LASTUPDATED.desc(),
                            ANNOTATIONS_.ID.desc())
                    .seek(ULong.valueOf(seekLastUpdated), seekId)
                    .limit(SEARCH_PAGE_LIMIT)
                    .fetch() // from database
                    .stream()
                    .map(AnnotationDTOMarshaller::toDTO)
                    .collect(Collectors.toList());
        } else {
            dtos = database.selectFrom(ANNOTATIONS_)
                    .where(ANNOTATIONS_.ID.contains(q))
                    .or(ANNOTATIONS_.CONTENT.contains(q))
                    .or(ANNOTATIONS_.ASSIGNTO.contains(q))
                    .orderBy(ANNOTATIONS_.LASTUPDATED.desc(),
                            ANNOTATIONS_.ID.desc())
                    .limit(SEARCH_PAGE_LIMIT)
                    .fetch() // from database
                    .stream()
                    .map(AnnotationDTOMarshaller::toDTO)
                    .collect(Collectors.toList());
        }

        return Response.status(Response.Status.OK)
                .entity(dtos)
                .build();
    }

    public final Response get(final String id) {
        final AnnotationsRecord result = database.selectFrom(ANNOTATIONS_)
                .where(ANNOTATIONS_.ID.equal(id))
                .fetchAny();

        if (null != result) {
            final AnnotationDTO annotationDTO = AnnotationDTOMarshaller.toDTO(result);

            return Response.status(Response.Status.OK)
                    .entity(annotationDTO)
                    .build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ResponseMsgDTO.msg("No annotation found")
                            .recordsUpdated(0)
                            .build())
                    .build();
        }
    }

    public final Response getHistory(final String id) {
        final List<AnnotationHistoryDTO> results = database.selectFrom(ANNOTATIONS_HISTORY)
                .where(ANNOTATIONS_HISTORY.ANNOTATIONID.equal(id))
                .fetch()
                .stream()
                .map(AnnotationDTOMarshaller::toDTO)
                .collect(Collectors.toList());

        if (results.size() > 0) {
            return Response.status(Response.Status.OK)
                    .entity(results)
                    .build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ResponseMsgDTO.msg("No history found for annotation")
                            .recordsUpdated(0)
                            .build())
                    .build();
        }
    }

    public final Response create(final String id) {

        final int result = database.insertInto(ANNOTATIONS_)
                .set(ANNOTATIONS_.ID, id)
                .set(ANNOTATIONS_.LASTUPDATED, ULong.valueOf(System.currentTimeMillis()))
                .set(ANNOTATIONS_.ASSIGNTO, AnnotationDTO.DEFAULT_ASSIGNEE)
                .set(ANNOTATIONS_.UPDATEDBY, AnnotationDTO.DEFAULT_UPDATED_BY)
                .set(ANNOTATIONS_.CONTENT, AnnotationDTO.DEFAULT_CONTENT)
                .set(ANNOTATIONS_.STATUS, AnnotationDTO.DEFAULT_STATUS.toString())
                .execute();

        takeAnnotationHistory(database, id, HistoryOperation.CREATE);

        if (result > 0) {
            return get( id);
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ResponseMsgDTO.msg("No annotation created")
                            .recordsUpdated(0)
                            .build())
                    .build();
        }
    }

    public final Response update(final String id,
                                 final AnnotationDTO annotation) {
        final int result = database.update(ANNOTATIONS_)
                .set(ANNOTATIONS_.LASTUPDATED, ULong.valueOf(System.currentTimeMillis()))
                .set(ANNOTATIONS_.ASSIGNTO, annotation.getAssignTo())
                .set(ANNOTATIONS_.CONTENT, annotation.getContent())
                .set(ANNOTATIONS_.STATUS, annotation.getStatus().toString())
                .where(ANNOTATIONS_.ID.equal(id))
                .execute();

        takeAnnotationHistory(database, id, HistoryOperation.UPDATE);

        if (result > 0) {
            return get(id);
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ResponseMsgDTO.msg("No annotation updated")
                            .recordsUpdated(0)
                            .build())
                    .build();
        }
    }

    public final Response remove(final String id) {
        // Take the history snapshot before deletion happens
        takeAnnotationHistory(database, id, HistoryOperation.DELETE);

        final int result = database.deleteFrom(ANNOTATIONS_)
                .where(ANNOTATIONS_.ID.equal(id))
                .execute();


        if (result > 0) {
            return Response.status(Response.Status.OK)
                    .entity(ResponseMsgDTO.msg("Annotation deleted")
                            .recordsUpdated(result)
                            .build())
                    .build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ResponseMsgDTO.msg("No annotation removed")
                            .recordsUpdated(0)
                            .build())
                    .build();
        }
    }

    private void takeAnnotationHistory(final DSLContext database,
                                       final String id,
                                       final HistoryOperation operation) {
        int rowsAffected = 0;

        final AnnotationsRecord currentState = database.selectFrom(ANNOTATIONS_)
                .where(ANNOTATIONS_.ID.equal(id))
                .fetchAny();

        switch (operation) {
            case CREATE:
            case UPDATE:
                rowsAffected = database.insertInto(ANNOTATIONS_HISTORY)
                        .set(ANNOTATIONS_HISTORY.ANNOTATIONID, currentState.getId())
                        .set(ANNOTATIONS_HISTORY.OPERATION, operation.toString())
                        .set(ANNOTATIONS_HISTORY.LASTUPDATED, currentState.getLastupdated())
                        .set(ANNOTATIONS_HISTORY.ASSIGNTO, currentState.getAssignto())
                        .set(ANNOTATIONS_HISTORY.UPDATEDBY, currentState.getUpdatedby())
                        .set(ANNOTATIONS_HISTORY.CONTENT, currentState.getContent())
                        .set(ANNOTATIONS_HISTORY.STATUS, currentState.getStatus())
                        .execute();
                break;
            case DELETE:
                rowsAffected = database.insertInto(ANNOTATIONS_HISTORY)
                        .set(ANNOTATIONS_HISTORY.ANNOTATIONID, currentState.getId())
                        .set(ANNOTATIONS_HISTORY.OPERATION, operation.toString())
                        .set(ANNOTATIONS_HISTORY.LASTUPDATED, ULong.valueOf(System.currentTimeMillis()))
                        .set(ANNOTATIONS_HISTORY.ASSIGNTO, currentState.getAssignto())
                        .set(ANNOTATIONS_HISTORY.UPDATEDBY, AnnotationDTO.DEFAULT_UPDATED_BY)
                        .set(ANNOTATIONS_HISTORY.CONTENT, currentState.getContent())
                        .set(ANNOTATIONS_HISTORY.STATUS, currentState.getStatus())
                        .execute();
                break;
        }



        LOGGER.trace(String.format("History Point Taken for Annotation %s - rowsAffected: %d", id, rowsAffected));
    }
}
