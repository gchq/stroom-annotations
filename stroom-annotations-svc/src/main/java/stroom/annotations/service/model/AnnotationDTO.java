package stroom.annotations.service.model;

public class AnnotationDTO {
    public static final String DEFAULT_CONTENT = "";
    public static final Status DEFAULT_STATUS = Status.CREATED;

    private String id;

    private String content;

    private Status status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "AnnotationDTO{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", status=" + status +
                '}';
    }

    public static class Builder {
        final AnnotationDTO instance;

        public Builder() {
            this(new AnnotationDTO());
        }

        public Builder(final AnnotationDTO instance) {
            this.instance = instance;
        }

        public Builder id(final String id) {
            this.instance.setId(id);
            return this;
        }

        public Builder content(final String content) {
            this.instance.setContent(content);
            return this;
        }

        public Builder status(final Status status) {
            this.instance.setStatus(status);
            return this;
        }

        public AnnotationDTO build() {
            return instance;
        }
    }
}
