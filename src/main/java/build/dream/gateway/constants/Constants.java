package build.dream.gateway.constants;

public class Constants extends build.dream.common.constants.Constants {
    public static final String NOTIFY_JOB_CRON_EXPRESSION = "notify.job.cron.expression";
    public static final String WEI_XIN_PAY_CALLBACK_SUCCESS_RETURN_VALUE = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
    public static final String WEI_XIN_PAY_CALLBACK_FAILURE_RETURN_VALUE = "<xml><return_code><![CDATA[FAILURE]]></return_code></xml>";
    public static final String ELEME_ORDER_CALLBACK_SUCCESS_RETURN_VALUE = "{\"message\":\"ok\"}";
    public static final String ELEME_MESSAGE_TOPIC = "eleme.message.topic";

    public static final String MEI_TUAN_MESSAGE_TOPIC = "mei.tuan.message.topic";

    public static final String MEI_TUAN_CALLBACK_SUCCESS_RETURN_VALUE = "{\"data\": \"OK\"}";

    /**
     * 京东到家消息类型
     *
     * @see #DJSW_TYPE_NEW_ORDER: 创建新订单消息
     * @see #DJSW_TYPE_ORDER_ADJUST: 订单调整消息
     * @see #DJSW_TYPE_APPLY_CANCELORDER: 用户取消申请消息
     * @see #DJSW_TYPE_ORDER_WAIT_OUT_STORE: 订单等待出库消息
     * @see #DJSW_TYPE_DELIVERY_ORDER: 订单开始配送消息
     * @see #DJSW_TYPE_PICK_FINISH_ORDER: 订单拣货完成消息
     * @see #DJSW_TYPE_FINISH_ORDER: 订单妥投消息
     * @see #DJSW_TYPE_LOCK_ORDER: 订单锁定消息
     * @see #DJSW_TYPE_UNLOCK_ORDER: 订单解锁消息
     * @see #DJSW_TYPE_USER_CANCEL_ORDER: 用户取消消息
     * @see #DJSW_TYPE_PUSH_DELIVERY_STATUS: 订单运单状态消息
     * @see #DJSW_TYPE_ORDER_INFO_CHANGE: 订单信息变更消息
     * @see #DJSW_TYPE_ORDER_ADD_TIPS: 订单商家小费消息
     * @see #DJSW_TYPE_ORDER_ACCOUNTING: 订单应结消息
     * @see #DJSW_TYPE_DELIVERY_CARRIER_MODIFY: 订单转自送消息
     */
    public static final int DJSW_TYPE_NEW_ORDER = 1;
    public static final int DJSW_TYPE_ORDER_ADJUST = 2;
    public static final int DJSW_TYPE_APPLY_CANCELORDER = 3;
    public static final int DJSW_TYPE_ORDER_WAIT_OUT_STORE = 4;
    public static final int DJSW_TYPE_DELIVERY_ORDER = 5;
    public static final int DJSW_TYPE_PICK_FINISH_ORDER = 6;
    public static final int DJSW_TYPE_FINISH_ORDER = 7;
    public static final int DJSW_TYPE_LOCK_ORDER = 8;
    public static final int DJSW_TYPE_UNLOCK_ORDER = 9;
    public static final int DJSW_TYPE_USER_CANCEL_ORDER = 10;
    public static final int DJSW_TYPE_PUSH_DELIVERY_STATUS = 11;
    public static final int DJSW_TYPE_ORDER_INFO_CHANGE = 12;
    public static final int DJSW_TYPE_ORDER_ADD_TIPS = 13;
    public static final int DJSW_TYPE_ORDER_ACCOUNTING = 14;
    public static final int DJSW_TYPE_DELIVERY_CARRIER_MODIFY = 15;

    public static final String KEY_JDDJ_TOKENS = "_jddj_tokens";
    public static final String KEY_JDDJ_VENDER_INFOS = "_jddj_vender_infos";
    public static final String JDDJ_MESSAGE_TOPIC = "jddj.message.topic";
}
