package com.ai.xiaocai.number.utils;

/**
 * Created by Lucien on 2018/5/29.
 */
//{"code":0,"message":"success","data":{"buyer_id":"1","event":"pause","type":"1","url":"http:\/\/smzg.58xmh.net\/Public\/Home\/audio\/bbaer.mp3"}}
public class EventResultBean {


    /**
     * code : 0
     * message : success
     * data : {"id":"447","buyer_id":"10","event":"play","type":"1","url":"http://smzg.58xmh.net/Public/Home/audio/aaa.mp3","is_get":"1","title":"","singer":""}
     */

    private int code;
    private String message;
    private DataBean data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * id : 447
         * buyer_id : 10
         * event : play
         * type : 1
         * url : http://smzg.58xmh.net/Public/Home/audio/aaa.mp3
         * is_get : 1
         * title :
         * singer :
         */

        private String id;
        private String buyer_id;
        private String event;
        private String type;
        private String url;
        private String is_get;
        private String title;
        private String singer;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getBuyer_id() {
            return buyer_id;
        }

        public void setBuyer_id(String buyer_id) {
            this.buyer_id = buyer_id;
        }

        public String getEvent() {
            return event;
        }

        public void setEvent(String event) {
            this.event = event;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getIs_get() {
            return is_get;
        }

        public void setIs_get(String is_get) {
            this.is_get = is_get;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSinger() {
            return singer;
        }

        public void setSinger(String singer) {
            this.singer = singer;
        }
    }
}
