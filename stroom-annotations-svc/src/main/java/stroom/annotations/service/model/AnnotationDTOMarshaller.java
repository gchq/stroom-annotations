package stroom.annotations.service.model;

import stroom.annotations.service.hibernate.Annotation;
import stroom.annotations.service.hibernate.AnnotationHistory;

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

    public static AnnotationHistoryDTO toDTO(final AnnotationHistory record) {
        return new AnnotationHistoryDTO.Builder()
                .historyId(record.getId())
                .operation(record.getOperation())
                .annotation(new AnnotationDTO.Builder()
                        .id(record.getAnnotationId())
                        .status(record.getStatus())
                        .assignTo(record.getAssignTo())
                        .content(record.getContent())
                        .updatedBy(record.getUpdatedBy())
                        .lastUpdated(record.getLastUpdated().longValue())
                        .build())
                .build();
    }
}
