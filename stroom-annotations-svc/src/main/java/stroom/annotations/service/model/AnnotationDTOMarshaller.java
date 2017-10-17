package stroom.annotations.service.model;

import stroom.annotations.service.hibernate.Annotation;
import stroom.db.annotations.tables.records.AnnotationsHistoryRecord;
import stroom.db.annotations.tables.records.AnnotationsRecord;

public final class AnnotationDTOMarshaller {
    private AnnotationDTOMarshaller() {

    }

    public static AnnotationDTO toDTO(final Annotation record) {
        return new AnnotationDTO.Builder()
                .id(record.getId())
                .status(record.getStatus())
                .assignTo(record.getAssignTo())
                .content(record.getContent())
                .updatedBy(record.getUpdatedBy())
                .lastUpdated(record.getLastUpdated())
                .build();
    }


    public static AnnotationDTO toDTO(final AnnotationsRecord record) {
        return new AnnotationDTO.Builder()
                .id(record.getId())
                .status(Status.valueOf(record.getStatus()))
                .assignTo(record.getAssignto())
                .content(record.getContent())
                .updatedBy(record.getUpdatedby())
                .lastUpdated(record.getLastupdated().longValue())
                .build();
    }

    public static AnnotationHistoryDTO toDTO(final AnnotationsHistoryRecord record) {
        return new AnnotationHistoryDTO.Builder()
                .historyId(record.getId())
                .operation(HistoryOperation.valueOf(record.getOperation()))
                .annotation(new AnnotationDTO.Builder()
                        .id(record.getAnnotationid())
                        .status(Status.valueOf(record.getStatus()))
                        .assignTo(record.getAssignto())
                        .content(record.getContent())
                        .updatedBy(record.getUpdatedby())
                        .lastUpdated(record.getLastupdated().longValue())
                        .build())
                .build();
    }
}
