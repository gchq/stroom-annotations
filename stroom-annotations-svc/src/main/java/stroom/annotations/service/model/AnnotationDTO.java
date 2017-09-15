package stroom.annotations.service.model;

public class AnnotationDTO {
    public static final String DEFAULT_ANNOTATION_CONTENT = "";

    private String id;

    private String content;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnnotationDTO that = (AnnotationDTO) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return content != null ? content.equals(that.content) : that.content == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AnnotationDTO{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
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

        public AnnotationDTO build() {
            return instance;
        }
    }
}
