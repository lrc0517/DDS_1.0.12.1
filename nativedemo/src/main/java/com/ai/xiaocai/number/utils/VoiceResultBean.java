package com.ai.xiaocai.number.utils;

import java.util.List;

/**
 * Created by Lucien on 2018/5/30.
 */
//{"code":0,"message":"success","data":[{"id":"84","buyer_id":"10","url":"http:\/\/p9jc4tchw.bkt.clouddn.com\/5b1604fc3b8f7.mp3","ext":"mp3","create_time":"1528169723"},{"id":"85","buyer_id":"10","url":"http:\/\/p9jc4tchw.bkt.clouddn.com\/5b1605001ed18.mp3","ext":"mp3","create_time":"1528169727"},{"id":"86","buyer_id":"10","url":"http:\/\/p9jc4tchw.bkt.clouddn.com\/5b160503ec21e.mp3","ext":"mp3","create_time":"1528169731"}]},type = 7

public class VoiceResultBean {


    /**
     * code : 0
     * message : success
     * data : [{"id":"3","buyer_id":"1","url":"http://smzg.58xmh.net/Uploads/Voice/2018-05-18/5afe8b6641210.amr","ext":"amr","create_time":"1526631269"},{"id":"4","buyer_id":"1","url":"http://smzg.58xmh.net/Uploads/Voice/2018-05-18/5afe8b7823e09.amr","ext":"amr","create_time":"1526631287"}]
     */

    private int code;
    private String message;
    private List<DataBean> data;

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

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * id : 3
         * buyer_id : 1
         * url : http://smzg.58xmh.net/Uploads/Voice/2018-05-18/5afe8b6641210.amr
         * ext : amr
         * create_time : 1526631269
         */

        private String id;
        private String buyer_id;
        private String url;
        private String ext;
        private String create_time;

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

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getExt() {
            return ext;
        }

        public void setExt(String ext) {
            this.ext = ext;
        }

        public String getCreate_time() {
            return create_time;
        }

        public void setCreate_time(String create_time) {
            this.create_time = create_time;
        }

        @Override
        public String toString() {
            return "{ id = " + id + ",buyer_id = " + buyer_id + ",url = " + url + ",ext" + ext + ",create_time = " + create_time + "}";
        }

    }

    @Override
    public String toString() {

        StringBuffer dataStr = null;
        for (DataBean bean : data) {
            dataStr.append(bean.toString());
        }
        return "{code = " + code + ",message = " + message + ",data = " + dataStr + "}";
    }
}
