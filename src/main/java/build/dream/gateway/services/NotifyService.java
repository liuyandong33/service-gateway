package build.dream.gateway.services;

import build.dream.common.saas.domains.AsyncNotify;
import build.dream.common.utils.*;
import build.dream.gateway.constants.Constants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class NotifyService {
    /**
     * 处理支付宝回调
     *
     * @param callbackParameters
     * @param uuidKey
     * @return
     */
    public String handleAlipayCallback(Map<String, String> callbackParameters, String uuidKey) {
        try {
            String uuid = callbackParameters.get(uuidKey);
            String asyncNotifyJson = CommonRedisUtils.get(uuid);
            AsyncNotify asyncNotify = JacksonUtils.readValue(asyncNotifyJson, AsyncNotify.class);
            ValidateUtils.notNull(asyncNotify, "异步通知不存在！");

            // 开始验签
            Map<String, String> sortedParameters = new TreeMap<String, String>(callbackParameters);
            String sign = sortedParameters.remove("sign");
            String charset = sortedParameters.get("charset");
            List<String> sortedParameterPair = new ArrayList<String>();
            for (Map.Entry<String, String> entry : sortedParameters.entrySet()) {
                sortedParameterPair.add(entry.getKey() + "=" + entry.getValue());
            }
            boolean isOk = AlipayUtils.verifySign(StringUtils.join(sortedParameterPair, "&"), asyncNotify.getAlipaySignType(), sign, charset, asyncNotify.getAlipayPublicKey());
            ValidateUtils.isTrue(isOk, "签名验证未通过！");

            KafkaUtils.send(asyncNotify.getTopic(), JacksonUtils.writeValueAsString(callbackParameters));
            CommonRedisUtils.del(uuid);
            return Constants.SUCCESS;
        } catch (Exception e) {
            LogUtils.error("支付宝回调处理失败", this.getClass().getName(), "alipayCallback", e, callbackParameters);
            return Constants.FAILURE;
        }
    }

    /**
     * 处理微信支付回调
     *
     * @param callbackParameters
     * @param uuidKey
     * @return
     */
    public String handleWeiXinPayCallback(Map<String, String> callbackParameters, String uuidKey) {
        try {
            String outTradeNo = callbackParameters.get(uuidKey);
            String asyncNotifyJson = CommonRedisUtils.get(outTradeNo);
            AsyncNotify asyncNotify = JacksonUtils.readValue(asyncNotifyJson, AsyncNotify.class);
            ValidateUtils.notNull(asyncNotify, "异步通知不存在！");
            KafkaUtils.send(asyncNotify.getTopic(), JacksonUtils.writeValueAsString(callbackParameters));
            CommonRedisUtils.del(outTradeNo);
            return Constants.WEI_XIN_PAY_CALLBACK_SUCCESS_RETURN_VALUE;
        } catch (Exception e) {
            LogUtils.error("微信支付回调处理失败", this.getClass().getName(), "handleWeiXinPayCallback", e, callbackParameters);
            return Constants.WEI_XIN_PAY_CALLBACK_FAILURE_RETURN_VALUE;
        }
    }

    private String obtainApiV3Key(String appId) {
        if ("wx63f5194332cc0f1b".equals(appId)) {
            return "qingdaozhihuifangxiangruanjian12";
        }
        return null;
    }

    /**
     * 处理微信退款回调
     *
     * @param callbackParameters
     * @param uuidKey
     * @return
     */
    public String handleXinRefundCallback(Map<String, String> callbackParameters, String uuidKey) {
        try {
            String appId = callbackParameters.get("appid");
            String reqInfo = callbackParameters.get("req_info");
            String apiV3Key = obtainApiV3Key(appId);
            byte[] bytes = AESUtils.decrypt(Base64.decodeBase64(reqInfo), DigestUtils.md5Hex(apiV3Key).getBytes(Constants.CHARSET_UTF_8), AESUtils.ALGORITHM_AES_ECB_PKCS7PADDING, AESUtils.PROVIDER_NAME_BC);
            String plaintext = new String(bytes, Constants.CHARSET_UTF_8);
            Map<String, String> plaintextMap = XmlUtils.xmlStringToMap(plaintext);
            String outRefundNo = plaintextMap.get(uuidKey);

            String asyncNotifyJson = CommonRedisUtils.get(outRefundNo);
            AsyncNotify asyncNotify = JacksonUtils.readValue(asyncNotifyJson, AsyncNotify.class);
            KafkaUtils.send(asyncNotify.getTopic(), JacksonUtils.writeValueAsString(callbackParameters));

            CommonRedisUtils.del(outRefundNo);
            return Constants.WEI_XIN_PAY_CALLBACK_SUCCESS_RETURN_VALUE;
        } catch (Exception e) {
            LogUtils.error("微信支付回调处理失败", this.getClass().getName(), "handleWeiXinPayCallback", e, callbackParameters);
            return Constants.WEI_XIN_PAY_CALLBACK_FAILURE_RETURN_VALUE;
        }
    }
}
