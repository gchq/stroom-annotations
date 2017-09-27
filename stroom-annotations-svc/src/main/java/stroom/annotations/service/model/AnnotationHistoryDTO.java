package stroom.annotations.service.model;

public class AnnotationHistoryDTO {
    private HistoryOperation operation;
    private int historyId;
    private AnnotationDTO annotation;

    public HistoryOperation getOperation() {
        return operation;
    }

    public void setOperation(HistoryOperation operation) {
        this.operation = operation;
    }

    public int getHistoryId() {
        return historyId;
    }

    public void setHistoryId(int historyId) {
        this.historyId = historyId;
    }

    public AnnotationDTO getAnnotation() {
        return annotation;
    }

    public void setAnnotation(AnnotationDTO annotation) {
        this.annotation = annotation;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AnnotationHistoryDTO{");
        sb.append("operation=").append(operation);
        sb.append(", historyId=").append(historyId);
        sb.append(", annotation=").append(annotation);
        sb.append('}');
        return sb.toString();
    }

    public static class Builder {
        final AnnotationHistoryDTO instance;

        public Builder() {
            this(new AnnotationHistoryDTO());
        }

        public Builder(final AnnotationHistoryDTO instance) {
            this.instance = instance;
        }

        public Builder historyId(final int historyId) {
            this.instance.setHistoryId(historyId);
            return this;
        }

        public Builder annotation(final AnnotationDTO annotation) {
            this.instance.setAnnotation(annotation);
            return this;
        }

        public Builder operation(final HistoryOperation operation) {
            this.instance.setOperation(operation);
            return this;
        }

        public AnnotationHistoryDTO build() {
            return instance;
        }
    }
}
