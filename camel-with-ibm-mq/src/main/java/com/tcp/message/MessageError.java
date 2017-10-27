package com.tcp.message;

public class MessageError {

    private aspErrors errcd1;
    private indType errid1;
    private String errfd1;
    private String errds1;

    public static final String RECORD_NAME = "error";

    public enum indType {
        ERROR("E"), WARNING("W");

        private String value;

        indType(String value){
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

    }

    public enum aspErrors {

        ASCCEND_FAIL_TO_CONNECT("99"),
        ASP_TIMEOUT_ASCCEND("98"),
        MSG_LENGTH_TO_SHORT("97"),
        MSG_UNKNOW_FORMAT("96");
        private String value;

        aspErrors(String value){
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public aspErrors getErrcd1() {
        return errcd1;
    }
    public void setErrcd1(aspErrors errcd1) {
        this.errcd1 = errcd1;
    }
    public indType getErrid1() {
        return errid1;
    }
    public void setErrid1(indType errid1) {
        this.errid1 = errid1;
    }
    public String getErrfd1() {
        return errfd1;
    }
    public void setErrfd1(String errfd1) {
        this.errfd1 = errfd1;
    }
    public String getErrds1() {
        return errds1;
    }
    public void setErrds1(String errds1) {
        this.errds1 = errds1;
    }



}
