package stroom.annotations.service.model;

import stroom.db.annotations.tables.records.AnnotationsRecord;

public final class AnnotationDTOMarshaller {
    private AnnotationDTOMarshaller() {

    }

    public static AnnotationDTO toDTO(final AnnotationsRecord record) {
        return new AnnotationDTO.Builder()
                .id(record.getId())
                .content(record.getContent())
                .status(Status.valueOf(record.getStatus()))
                .build();
    }
}
