package com.tcp.message;



public class MessageHeader  {

    private mtiType mti;
    private String pan;
    private String prcd;
    private String stan;
    private String time;
    private String date;
    private String rrn;
    private String msgid;
    private String rpcd;
    private String tid;
    private String loc;
    private String mri;
    private String count;

    public static final String RECORD_NAME = "header";

    public enum mtiType {
        REQUEST("0600"), RESPONSE("0610");

        private String value;

        mtiType(String value){
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

    }

    public MessageHeader() {
        super();
    }

    public MessageHeader( mtiType mti, String pan, String prcd,
            String stan,  String time, String date, String rrn,
            String msgid, String rpcd, String tid,
            String loc, String mri, String count ) {
        super();
        this.mti = mti;
        this.pan =pan;
        this.prcd = prcd;
        this.stan =stan;
        this.time = time;
        this.date = date;
        this.rrn=rrn;
        this.msgid = msgid;
        this.rpcd =rpcd;
        this.tid =tid;
        this.loc=loc;
        this.mri=mri;
        this.count=count;
    }

    public mtiType getMti() {
        return mti;
    }

    public void setMti(mtiType mti) {
        this.mti = mti;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getPrcd() {
        return prcd;
    }

    public void setPrcd(String prcd) {
        this.prcd = prcd;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }

    public String getRpcd() {
        return rpcd;
    }

    public void setRpcd(String rpcd) {
        this.rpcd = rpcd;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public String getMri() {
        return mri;
    }

    public void setMri(String mri) {
        this.mri = mri;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }


}
