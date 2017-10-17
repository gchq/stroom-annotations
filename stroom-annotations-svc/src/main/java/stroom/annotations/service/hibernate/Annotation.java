package stroom.annotations.service.hibernate;

import stroom.annotations.service.model.Status;

import javax.persistence.*;

@Entity(name="annotations")
public class Annotation {
    public static final String ID = "id";
    public static final String STATUS = "status";
    public static final String ASSIGN_TO = "assignTo";
    public static final String LAST_UPDATED = "lastUpdated";
    public static final String CONTENT = "content";
    public static final String UPDATED_BY = "updatedBy";

    private String id;

    private String assignTo;

    private Long lastUpdated;

    private String updatedBy;

    private Status status;

    private String content;

    @Id
    @Column(name=ID)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Enumerated(EnumType.STRING)
    @Column(name=STATUS)
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Column(name=ASSIGN_TO)
    public String getAssignTo() {
        return assignTo;
    }

    public void setAssignTo(String assignTo) {
        this.assignTo = assignTo;
    }

    @Column(name=LAST_UPDATED)
    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Column(name=CONTENT)
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Column(name=UPDATED_BY)
    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Annotation{");
        sb.append("id='").append(id).append('\'');
        sb.append(", assignTo='").append(assignTo).append('\'');
        sb.append(", lastUpdated=").append(lastUpdated);
        sb.append(", updatedBy='").append(updatedBy).append('\'');
        sb.append(", status=").append(status);
        sb.append(", content='").append(content).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
