package com.ai.xiaocai.bean;

import java.util.List;

/**
 * Created by Lucien on 2018/6/30.
 */

public class AlarmBeanSingle {

    /**
     * intent : 设置单一闹钟
     * intentName : 设置单一闹钟
     * skillId : 2018032100000037
     * date : 20180701
     * time : 09:00:00
     * duiWidget : content
     * nlu : {"input":"设置明天��上九点的闹钟","version":"2018.5.30.16:26:49","skillVersion":"11","skillId":"2018032100000037","skill":"AITEK闹钟","res":"5afba513805d8500015740e4","timestamp":1530331421,"semantics":{"request":{"confidence":1,"slotcount":3,"slots":[{"name":"intent","value":"设置单一闹钟"},{"rawvalue":"早上九点","rawpinyin":"zao shang jiu dian","value":"09:00:00","name":"时间一","pos":[4,7]},{"rawvalue":"明天","rawpinyin":"ming tian","value":"20180701","name":"日期","pos":[2,3]}],"task":"创建闹铃"}},"systime":20.827880859375}
     */

    private String intent;
    private String intentName;
    private String skillId;
    private String date;
    private String time;
    private String duiWidget;
    private NluBean nlu;

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getIntentName() {
        return intentName;
    }

    public void setIntentName(String intentName) {
        this.intentName = intentName;
    }

    public String getSkillId() {
        return skillId;
    }

    public void setSkillId(String skillId) {
        this.skillId = skillId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDuiWidget() {
        return duiWidget;
    }

    public void setDuiWidget(String duiWidget) {
        this.duiWidget = duiWidget;
    }

    public NluBean getNlu() {
        return nlu;
    }

    public void setNlu(NluBean nlu) {
        this.nlu = nlu;
    }

    public static class NluBean {
        /**
         * input : 设置明天��上九点的闹钟
         * version : 2018.5.30.16:26:49
         * skillVersion : 11
         * skillId : 2018032100000037
         * skill : AITEK闹钟
         * res : 5afba513805d8500015740e4
         * timestamp : 1530331421
         * semantics : {"request":{"confidence":1,"slotcount":3,"slots":[{"name":"intent","value":"设置单一闹钟"},{"rawvalue":"早上九点","rawpinyin":"zao shang jiu dian","value":"09:00:00","name":"时间一","pos":[4,7]},{"rawvalue":"明天","rawpinyin":"ming tian","value":"20180701","name":"日期","pos":[2,3]}],"task":"创建闹铃"}}
         * systime : 20.827880859375
         */

        private String input;
        private String version;
        private String skillVersion;
        private String skillId;
        private String skill;
        private String res;
        private int timestamp;
        private SemanticsBean semantics;
        private double systime;

        public String getInput() {
            return input;
        }

        public void setInput(String input) {
            this.input = input;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getSkillVersion() {
            return skillVersion;
        }

        public void setSkillVersion(String skillVersion) {
            this.skillVersion = skillVersion;
        }

        public String getSkillId() {
            return skillId;
        }

        public void setSkillId(String skillId) {
            this.skillId = skillId;
        }

        public String getSkill() {
            return skill;
        }

        public void setSkill(String skill) {
            this.skill = skill;
        }

        public String getRes() {
            return res;
        }

        public void setRes(String res) {
            this.res = res;
        }

        public int getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(int timestamp) {
            this.timestamp = timestamp;
        }

        public SemanticsBean getSemantics() {
            return semantics;
        }

        public void setSemantics(SemanticsBean semantics) {
            this.semantics = semantics;
        }

        public double getSystime() {
            return systime;
        }

        public void setSystime(double systime) {
            this.systime = systime;
        }

        public static class SemanticsBean {
            /**
             * request : {"confidence":1,"slotcount":3,"slots":[{"name":"intent","value":"设置单一闹钟","rawvalue":"早上九点","rawpinyin":"zao shang jiu dian","pos":[4,7]},{"rawvalue":"早上九点","rawpinyin":"zao shang jiu dian","value":"09:00:00","name":"时间一","pos":[4,7]},{"rawvalue":"明天","rawpinyin":"ming tian","value":"20180701","name":"日期","pos":[2,3]}],"task":"创建闹铃"}
             */

            private RequestBean request;

            public RequestBean getRequest() {
                return request;
            }

            public void setRequest(RequestBean request) {
                this.request = request;
            }

            public static class RequestBean {
                /**
                 * confidence : 1
                 * slotcount : 3
                 * slots : [{"name":"intent","value":"设置单一闹钟"},{"rawvalue":"早上九点","rawpinyin":"zao shang jiu dian","value":"09:00:00","name":"时间一","pos":[4,7]},{"rawvalue":"明天","rawpinyin":"ming tian","value":"20180701","name":"日期","pos":[2,3]}]
                 * task : 创建闹铃
                 */

                private int confidence;
                private int slotcount;
                private String task;
                private List<SlotsBean> slots;

                public int getConfidence() {
                    return confidence;
                }

                public void setConfidence(int confidence) {
                    this.confidence = confidence;
                }

                public int getSlotcount() {
                    return slotcount;
                }

                public void setSlotcount(int slotcount) {
                    this.slotcount = slotcount;
                }

                public String getTask() {
                    return task;
                }

                public void setTask(String task) {
                    this.task = task;
                }

                public List<SlotsBean> getSlots() {
                    return slots;
                }

                public void setSlots(List<SlotsBean> slots) {
                    this.slots = slots;
                }

                public static class SlotsBean {
                    /**
                     * name : intent
                     * value : 设置单一闹钟
                     * rawvalue : 早上九点
                     * rawpinyin : zao shang jiu dian
                     * pos : [4,7]
                     */

                    private String name;
                    private String value;
                    private String rawvalue;
                    private String rawpinyin;
                    private List<Integer> pos;

                    public String getName() {
                        return name;
                    }

                    public void setName(String name) {
                        this.name = name;
                    }

                    public String getValue() {
                        return value;
                    }

                    public void setValue(String value) {
                        this.value = value;
                    }

                    public String getRawvalue() {
                        return rawvalue;
                    }

                    public void setRawvalue(String rawvalue) {
                        this.rawvalue = rawvalue;
                    }

                    public String getRawpinyin() {
                        return rawpinyin;
                    }

                    public void setRawpinyin(String rawpinyin) {
                        this.rawpinyin = rawpinyin;
                    }

                    public List<Integer> getPos() {
                        return pos;
                    }

                    public void setPos(List<Integer> pos) {
                        this.pos = pos;
                    }
                }
            }
        }
    }
}
