package stroom.annotations.model;

import stroom.annotations.hibernate.Annotation;
import stroom.annotations.hibernate.Status;

import java.util.Objects;

public class AnnotationDTO {
    public static final int MIN_ID_LENGTH = 3;
    public static final Status DEFAULT_STATUS = Status.QUEUED;
    public static final String DEFAULT_CONTENT = "";
    public static final String DEFAULT_UPDATED_BY = "stroom-annotations-user";
    public static final String DEFAULT_ASSIGNEE = "";

    private String id;

    private String assignTo;

    private Long lastUpdated;

    private String updatedBy;

    private Status status;

    private String content;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getAssignTo() {
        return assignTo;
    }

    public void setAssignTo(String assignTo) {
        this.assignTo = assignTo;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public String toString() {
        return "AnnotationDTO{" +
                "id='" + id + '\'' +
                ", status=" + status +
                ", content=" + content +
                ", assignTo=" + assignTo +
                ", lastUpdated=" + lastUpdated +
                ", updatedBy=" + updatedBy +
                '}';
    }

    /**
     * Only takes into account the fields settable by the user.
     * @param o The other annotation to compare.
     * @return If the annotations have the same values in the user settable fields.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnnotationDTO that = (AnnotationDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(assignTo, that.assignTo) &&
                status == that.status &&
                Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, assignTo, status, content);
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

        public Builder status(final Status status) {
            this.instance.setStatus(status);
            return this;
        }

        public Builder assignTo(final String assignee) {
            this.instance.setAssignTo(assignee);
            return this;
        }

        public Builder content(final String content) {
            this.instance.setContent(content);
            return this;
        }

        public Builder updatedBy(final String updatedBy) {
            this.instance.setUpdatedBy(updatedBy);
            return this;
        }

        public Builder lastUpdated(final Long lastUpdated) {
            this.instance.setLastUpdated(lastUpdated);
            return this;
        }

        public Builder entity(final Annotation entity) {
            return this
                    .id(entity.getId())
                    .status(entity.getStatus())
                    .assignTo(entity.getAssignTo())
                    .content(entity.getContent())
                    .updatedBy(entity.getUpdatedBy())
                    .lastUpdated(entity.getLastUpdated());
        }

        public AnnotationDTO build() {
            return instance;
        }
    }
}
