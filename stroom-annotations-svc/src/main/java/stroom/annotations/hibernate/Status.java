package stroom.annotations.hibernate;

public enum Status {
    QUEUED("Queued"),
    CLOSED_DATA_ERROR("Closed - Data Error"),
    CLOSED_DUPLICATE("Closed - Duplicate"),
    CLOSED_INCIDENT_RAISED("Closed - Incident Raised"),
    CLOSED_LEGITIMATE_BEHAVIOUR("Closed - Legitmate Behaviour"),
    CLOSED_PASSED_TO_CONTENT_DEVELOPMENT("Closed - Passed to Content Development"),
    CLOSED_TRENDED("Closed - Trended"),
    OPEN_CHANGE_COMMENT("Open - Change Comment"),
    OPEN_ESCALATED("Open - Escalated"),
    OPEN_GUIDANCE_REQUIRED("Open - Guidance Required"),
    OPEN_MONITORING_EVENT("Open - Monitoring Event"),
    OPEN_PASSED_TO_AUDIT("Open - Passed to Audit"),
    OPEN_PASSED_TO_ITAD("Open - Passed to ITAD");

    private final String displayText;

    private Status(final String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() {
        return displayText;
    }
}
