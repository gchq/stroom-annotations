package stroom.annotations.hibernate;

import stroom.datasource.api.v2.DataSourceField;
import stroom.query.api.v2.ExpressionTerm;
import stroom.query.hibernate.IsDataSourceField;
import stroom.query.hibernate.QueryableEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

@Entity(name="annotation")
public class Annotation extends QueryableEntity {
    public static final String ID = "id";
    public static final String STATUS = "status";
    public static final String ASSIGN_TO = "assignTo";
    public static final String CONTENT = "content";

    public static final int MIN_ID_LENGTH = 3;
    public static final Status DEFAULT_STATUS = Status.QUEUED;
    public static final String DEFAULT_CONTENT = "";
    public static final String DEFAULT_ASSIGNEE = "";

    private String id;

    private String assignTo;

    private Status status;

    private String content;

    public static class IdField implements Supplier<DataSourceField> {

        @Override
        public DataSourceField get() {
            return new DataSourceField(
                    DataSourceField.DataSourceFieldType.ID,
                    Annotation.ID,
                    true,
                    Arrays.asList(
                            ExpressionTerm.Condition.EQUALS,
                            ExpressionTerm.Condition.IN,
                            ExpressionTerm.Condition.IN_DICTIONARY
                    )
            );
        }
    }

    @Id
    @Column(name=ID)
    @IsDataSourceField(fieldSupplier = IdField.class)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static class StatusField implements Supplier<DataSourceField> {

        @Override
        public DataSourceField get() {
            return new DataSourceField(
                    DataSourceField.DataSourceFieldType.FIELD,
                    Annotation.STATUS,
                    true,
                    Arrays.asList(
                            ExpressionTerm.Condition.EQUALS,
                            ExpressionTerm.Condition.IN,
                            ExpressionTerm.Condition.IN_DICTIONARY
                    )
            );
        }
    }

    @Enumerated(EnumType.STRING)
    @Column(name=STATUS)
    @IsDataSourceField(fieldSupplier = StatusField.class)
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public static class AssignToField implements Supplier<DataSourceField> {

        @Override
        public DataSourceField get() {
            return new DataSourceField(
                    DataSourceField.DataSourceFieldType.FIELD,
                    Annotation.ASSIGN_TO,
                    true,
                    Arrays.asList(
                            ExpressionTerm.Condition.EQUALS,
                            ExpressionTerm.Condition.CONTAINS,
                            ExpressionTerm.Condition.IN,
                            ExpressionTerm.Condition.IN_DICTIONARY
                    )
            );
        }
    }

    @Column(name=ASSIGN_TO)
    @IsDataSourceField(fieldSupplier = AssignToField.class)
    public String getAssignTo() {
        return assignTo;
    }

    public void setAssignTo(String assignTo) {
        this.assignTo = assignTo;
    }

    public static class ContentField implements Supplier<DataSourceField> {

        @Override
        public DataSourceField get() {
            return new DataSourceField(
                    DataSourceField.DataSourceFieldType.FIELD,
                    Annotation.CONTENT,
                    true,
                    Arrays.asList(
                            ExpressionTerm.Condition.EQUALS,
                            ExpressionTerm.Condition.CONTAINS
                    )
            );
        }
    }

    @Column(name=CONTENT)
    @IsDataSourceField(fieldSupplier = ContentField.class)
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
        sb.append(", id='").append(assignTo).append('\'');
        sb.append(", assignTo='").append(assignTo).append('\'');
        sb.append(", status=").append(status);
        sb.append(", content='").append(content).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Annotation that = (Annotation) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(assignTo, that.assignTo) &&
                status == that.status &&
                Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, assignTo, status, content);
    }

    public static final class Builder extends QueryableEntity.Builder<Annotation, Builder> {

        public Builder() {
            super(new Annotation());
        }

        public Builder id(final String value) {
            this.instance.setId(value);
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
