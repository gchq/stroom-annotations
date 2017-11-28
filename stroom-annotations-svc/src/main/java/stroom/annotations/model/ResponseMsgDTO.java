package stroom.annotations.model;

public class ResponseMsgDTO {
    private String msg;
    private int recordsUpdated;

    public ResponseMsgDTO() {
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getRecordsUpdated() {
        return recordsUpdated;
    }

    public void setRecordsUpdated(int recordsUpdated) {
        this.recordsUpdated = recordsUpdated;
    }

    public static Builder msg(final String msg) {
        return new Builder().msg(msg);
    }

    public static Builder recordsUpdated(final int recordsUpdated) {
        return new Builder().recordsUpdated(recordsUpdated);
    }

    public static class Builder {
        private final ResponseMsgDTO instance;

        public Builder() {
            this.instance = new ResponseMsgDTO();
        }

        public Builder msg(final String msg) {
            this.instance.msg = msg;
            return this;
        }

        public Builder recordsUpdated(final int recordsUpdated) {
            this.instance.recordsUpdated = recordsUpdated;
            return this;
        }

        public ResponseMsgDTO build() {
            return instance;
        }
    }
}
