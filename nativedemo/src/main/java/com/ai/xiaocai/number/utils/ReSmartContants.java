package com.ai.xiaocai.number.utils;

/**
 * Created by Lucien on 2018/5/9.
 */

public interface ReSmartContants {

    String URL = "http://m.lifeai.com.cn";

    String URL_EVENT = "/index.php/Home/Api/event";//key equip

    String URL_LIST = "/index.php/Home/Api/buyer_collect";//key user_id

    String URL_GET_VOICE = "/index.php/Home/Api/voice_msg";//key equip

    //POST VOICE
    String URL_POST_VOICE = "/index.php/Home/Api/upload_voice";//key equip & media

    String URL_DEVICE_STATE = "/index.php/Home/Api/equip_status";//key equip & status(1 0)

    String URL_USER = "/index.php/Home/Api/get_user";//key equip

    String URL_MAC_AUTH = "/index.php/Home/Api/get_qrcode";//key mac & deviceid & rd

    String URL_TRANSLATE = "/index.php/Home/Api/text_trans_voice";//key from & equip & content

    String URL_ACCESS = "/index.php/Home/Api/access_times";

    String URL_WAKEUP_WORD = "/index.php/Home/Api/wake_up";  //key equip

    String URL_SOS = "/index.php/Home/Api/sos";//key equip

    String URL_ASSIS = "/index.php/Home/Api/voiceAssis";//key equip & content

    String URL_CHAT = "/index.php/Home/Api/voiceAssis";
}
