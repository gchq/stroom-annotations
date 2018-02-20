package stroom.annotations.model;

import org.jooq.Field;
import stroom.datasource.api.v2.DataSourceField;
import stroom.query.api.v2.ExpressionTerm;
import stroom.query.audit.model.IsDataSourceField;
import stroom.query.audit.model.QueryableEntity;
import stroom.query.jooq.JooqEntity;
import stroom.query.jooq.QueryableJooqEntity;

import java.util.Objects;
import java.util.function.Supplier;

import static org.jooq.impl.DSL.field;

@JooqEntity(tableName="annotation")
public class Annotation extends QueryableJooqEntity {
    public static final String ID = "id";
    public static final String STATUS = "status";
    public static final String ASSIGN_TO = "assignTo";
    public static final String CONTENT = "content";

    public static final Field<String> ID_FIELD = field(ID, String.class);
    public static final Field<String> CONTENT_FIELD = field(CONTENT, String.class);
    public static final Field<String> ASSIGN_TO_FIELD = field(ASSIGN_TO, String.class);
    public static final Field<String> STATUS_FIELD = field(STATUS, String.class);

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
            return new DataSourceField.Builder()
                    .type(DataSourceField.DataSourceFieldType.ID)
                    .name(Annotation.ID)
                    .queryable(true)
                    .addConditions(
                            ExpressionTerm.Condition.EQUALS,
                            ExpressionTerm.Condition.IN,
                            ExpressionTerm.Condition.IN_DICTIONARY
                    ).build();
        }
    }

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
            return new DataSourceField.Builder()
                    .type(DataSourceField.DataSourceFieldType.FIELD)
                    .name(Annotation.STATUS)
                    .queryable(true)
                    .addConditions(
                            ExpressionTerm.Condition.EQUALS,
                            ExpressionTerm.Condition.IN,
                            ExpressionTerm.Condition.IN_DICTIONARY
                    ).build();
        }
    }

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
            return new DataSourceField.Builder()
                    .type(DataSourceField.DataSourceFieldType.FIELD)
                    .name(Annotation.ASSIGN_TO)
                    .queryable(true)
                    .addConditions(
                            ExpressionTerm.Condition.EQUALS,
                            ExpressionTerm.Condition.CONTAINS,
                            ExpressionTerm.Condition.IN,
                            ExpressionTerm.Condition.IN_DICTIONARY
                    ).build();
        }
    }

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
            return new DataSourceField.Builder()
                    .type(DataSourceField.DataSourceFieldType.FIELD)
                    .name(Annotation.CONTENT)
                    .queryable(true)
                    .addConditions(
                            ExpressionTerm.Condition.EQUALS,
                            ExpressionTerm.Condition.CONTAINS
                    ).build();
        }
    }

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

    public static final class Builder extends QueryableEntity.BaseBuilder<Annotation, Builder> {

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

        public Builder status(final String value) {
            return status(Status.valueOf(value));
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
