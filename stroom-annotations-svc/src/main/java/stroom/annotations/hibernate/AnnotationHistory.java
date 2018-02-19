package stroom.annotations.hibernate;


import stroom.query.audit.model.QueryableEntity;
import stroom.query.hibernate.QueryableHibernateEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

@Entity(name="annotation_history")
public class AnnotationHistory extends QueryableHibernateEntity {
    public static final String ANNOTATION_ID = "annotationId";
    public static final String OPERATION = "operation";

    private int id;

    private HistoryOperation operation;

    private String annotationId;

    private String assignTo;

    private Status status;

    private String content;

    @Id
    @Column(name=Annotation.ID)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Enumerated(EnumType.STRING)
    @Column(name=OPERATION)
    public HistoryOperation getOperation() {
        return operation;
    }

    public void setOperation(HistoryOperation operation) {
        this.operation = operation;
    }

    @Column(name=ANNOTATION_ID)
    public String getAnnotationId() {
        return annotationId;
    }

    public void setAnnotationId(String annotationId) {
        this.annotationId = annotationId;
    }

    @Enumerated(EnumType.STRING)
    @Column(name=Annotation.STATUS)
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Column(name=Annotation.ASSIGN_TO)
    public String getAssignTo() {
        return assignTo;
    }

    public void setAssignTo(String assignTo) {
        this.assignTo = assignTo;
    }

    @Column(name=Annotation.CONTENT)
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Annotation{");
        sb.append(super.toString()).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append(", operation='").append(operation).append('\'');
        sb.append(", annotationId='").append(annotationId).append('\'');
        sb.append(", assignTo='").append(assignTo).append('\'');
        sb.append(", status=").append(status);
        sb.append(", content='").append(content).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static final class Builder extends QueryableEntity.BaseBuilder<AnnotationHistory, Builder> {

        public Builder() {
            super(new AnnotationHistory());
        }

        public Builder id(final int value) {
            this.instance.setId(value);
            return self();
        }

        public Builder operation(final HistoryOperation value) {
            this.instance.setOperation(value);
            return self();
        }

        public Builder annotationId(final String value) {
            this.instance.setAnnotationId(value);
            return self();
        }

        public Builder assignTo(final String value) {
            this.instance.setAssignTo(value);
            return self();
        }

        public Builder status(final Status value) {
            this.instance.setStatus(value);
            return self();
        }

        public Builder content(final String value) {
            this.instance.setContent(value);
            return self();
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
