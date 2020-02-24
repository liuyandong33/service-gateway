package build.dream.gateway.constants;

public class Constants extends build.dream.common.constants.Constants {
    public static final String NOTIFY_JOB_CRON_EXPRESSION = "notify.job.cron.expression";
    public static final String WEI_XIN_PAY_CALLBACK_SUCCESS_RETURN_VALUE = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
    public static final String WEI_XIN_PAY_CALLBACK_FAILURE_RETURN_VALUE = "<xml><return_code><![CDATA[FAILURE]]></return_code></xml>";
    public static final String ELEME_ORDER_CALLBACK_SUCCESS_RETURN_VALUE = "{\"message\":\"ok\"}";
    public static final String ELEME_MESSAGE_TOPIC = "eleme.message.topic";

    public static final String MEI_TUAN_MESSAGE_TOPIC = "mei.tuan.message.topic";

    public static final String MEI_TUAN_CALLBACK_SUCCESS_RETURN_VALUE = "{\"data\": \"OK\"}";

    public static final String JDDJ_MESSAGE_TOPIC = "jddj.message.topic";


    public static final String MEI_TUAN_SIGN_KEY = "mei.tuan.sign.key";
}
