package build.dream.gateway.services;

import build.dream.common.beans.MqConfig;
import build.dream.common.beans.NewLandOrgInfo;
import build.dream.common.domains.saas.AsyncNotify;
import build.dream.common.utils.*;
import build.dream.gateway.constants.Constants;
import com.aliyun.openservices.ons.api.Message;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.util.*;

@Service
public class NotifyService {
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
            ValidateUtils.notBlank(asyncNotifyJson, "异步通知不存在！");
            AsyncNotify asyncNotify = JacksonUtils.readValue(asyncNotifyJson, AsyncNotify.class);
            send(asyncNotify.getMqConfig(), JacksonUtils.writeValueAsString(callbackParameters));
            CommonRedisUtils.del(outTradeNo);
            return Constants.WEI_XIN_PAY_CALLBACK_SUCCESS_RETURN_VALUE;
        } catch (Exception e) {
            LogUtils.error("微信支付回调处理失败", this.getClass().getName(), "handleWeiXinPayCallback", e, callbackParameters);
            return Constants.WEI_XIN_PAY_CALLBACK_FAILURE_RETURN_VALUE;
        }
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
            String apiV3Key = WeiXinPayUtils.obtainApiV3Key(appId);
            byte[] bytes = AESUtils.decrypt(Base64.decodeBase64(reqInfo), DigestUtils.md5Hex(apiV3Key).getBytes(Constants.CHARSET_UTF_8), AESUtils.ALGORITHM_AES_ECB_PKCS7PADDING, AESUtils.PROVIDER_NAME_BC);
            String plaintext = new String(bytes, Constants.CHARSET_UTF_8);
            Map<String, String> plaintextMap = XmlUtils.xmlStringToMap(plaintext);
            String outRefundNo = plaintextMap.get(uuidKey);

            String asyncNotifyJson = CommonRedisUtils.get(outRefundNo);
            ValidateUtils.notBlank(asyncNotifyJson, "异步通知不存在！");

            Map<String, String> params = new HashMap<String, String>(callbackParameters);
            params.remove("req_info");
            params.putAll(plaintextMap);
            AsyncNotify asyncNotify = JacksonUtils.readValue(asyncNotifyJson, AsyncNotify.class);
            send(asyncNotify.getMqConfig(), JacksonUtils.writeValueAsString(params));

            CommonRedisUtils.del(outRefundNo);
            return Constants.WEI_XIN_PAY_CALLBACK_SUCCESS_RETURN_VALUE;
        } catch (Exception e) {
            LogUtils.error("微信支付回调处理失败", this.getClass().getName(), "handleXinRefundCallback", e, callbackParameters);
            return Constants.WEI_XIN_PAY_CALLBACK_FAILURE_RETURN_VALUE;
        }
    }

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
            ValidateUtils.notBlank(asyncNotifyJson, "异步通知不存在！");

            AsyncNotify asyncNotify = JacksonUtils.readValue(asyncNotifyJson, AsyncNotify.class);

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

            send(asyncNotify.getMqConfig(), JacksonUtils.writeValueAsString(callbackParameters));
            CommonRedisUtils.del(uuid);
            return Constants.SUCCESS;
        } catch (Exception e) {
            LogUtils.error("支付宝回调处理失败", this.getClass().getName(), "handleAlipayCallback", e, callbackParameters);
            return Constants.FAILURE;
        }
    }

    /**
     * 处理米雅回调
     *
     * @param callbackParameters
     * @param uuidKey
     * @return
     */
    public String handleMiyaCallback(Map<String, String> callbackParameters, String uuidKey) {
        try {
            String uuid = callbackParameters.get(uuidKey);
            String asyncNotifyJson = CommonRedisUtils.get(uuid);
            ValidateUtils.notBlank(asyncNotifyJson, "异步通知不存在！");

            AsyncNotify asyncNotify = JacksonUtils.readValue(asyncNotifyJson, AsyncNotify.class);

            // 开始验签
            ValidateUtils.isTrue(MiyaUtils.verifySign(callbackParameters, asyncNotify.getMiyaKey()), "签名错误！");
            send(asyncNotify.getMqConfig(), JacksonUtils.writeValueAsString(callbackParameters));
            CommonRedisUtils.del(uuid);
            return "<xml><D1>SUCCESS</D1></xml>";
        } catch (Exception e) {
            LogUtils.error("支付宝回调处理失败", this.getClass().getName(), "handleMiyaCallback", e, callbackParameters);
            return "<xml><D1>FAILURE</D1><D2><![CDATA[" + e.getMessage() + "]]></D2></xml>";
        }
    }

    /**
     * 处理新大陆回调
     *
     * @param body
     * @param orgNo
     * @return
     */
    public String handleNewLandCallback(String body, String orgNo) {
        try {
            Map<String, String> bodyMap = JacksonUtils.readValueAsMap(body, String.class, String.class);
            String datas = bodyMap.get("Datas");
            String signValue = bodyMap.get("signValue");

            NewLandOrgInfo newLandOrgInfo = NewLandUtils.obtainNewLandOrgInfo(orgNo);
            ValidateUtils.notNull(newLandOrgInfo, "机构信息不存在！");

            String md5Key = newLandOrgInfo.getMd5Key();
            ValidateUtils.isTrue(DigestUtils.md5Hex(datas + md5Key).toUpperCase().equals(signValue), "签名错误！");

            PrivateKey privateKey = RSAUtils.restorePrivateKey(newLandOrgInfo.getPrivateKey());

            byte[] encryptedData = Hex.decodeHex(datas.toCharArray());
            String plaintext = new String(RSAUtils.decryptByPrivateKey(encryptedData, privateKey, RSAUtils.PADDING_MODE_RSA_ECB_PKCS1PADDING), Constants.CHARSET_NAME_ISO_8859_1);

            Map<String, Object> plaintextMap = JacksonUtils.readValueAsMap(plaintext, String.class, Object.class);

            String channelId = MapUtils.getString(plaintextMap, "ChannelId");
            String asyncNotifyJson = CommonRedisUtils.get(channelId);
            ValidateUtils.notBlank(asyncNotifyJson, "异步通知不存在！");

            AsyncNotify asyncNotify = JacksonUtils.readValue(asyncNotifyJson, AsyncNotify.class);
            send(asyncNotify.getMqConfig(), JacksonUtils.writeValueAsString(plaintextMap));
            CommonRedisUtils.del(channelId);

            String rspCode = "000000";
            String rspDes = StringUtils.rightPad("处理成功", 64, " ");
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("rspCode", rspCode);
            resultMap.put("rspDes", rspDes);
            return JacksonUtils.writeValueAsString(resultMap);
        } catch (Exception e) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("body", body);
            params.put("orgNo", orgNo);
            LogUtils.error("新大陆回调处理失败", this.getClass().getName(), "handleNewLandCallback", e, params);

            String rspCode = "000001";
            String rspDes = e.getMessage();
            if (rspDes.length() > 64) {
                rspDes = rspDes.substring(0, 64);
            }
            rspDes = StringUtils.rightPad(rspDes, 64, " ");

            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("rspCode", rspCode);
            resultMap.put("rspDes", rspDes);
            return JacksonUtils.writeValueAsString(resultMap);
        }
    }

    /**
     * 处理联动支付回调
     *
     * @param callbackParameters
     * @param uuidKey
     * @return
     */
    public String handleUmPayCallback(Map<String, String> callbackParameters, String uuidKey) {
        String privateKey = null;
        String retCode = null;
        String retMsg = null;
        try {
            String uuid = callbackParameters.get(uuidKey);
            String asyncNotifyJson = CommonRedisUtils.get(uuid);
            ValidateUtils.notBlank(asyncNotifyJson, "异步通知不存在！");

            AsyncNotify asyncNotify = JacksonUtils.readValue(asyncNotifyJson, AsyncNotify.class);

            // 开始验签
            ValidateUtils.isTrue(UmPayUtils.verifySign(callbackParameters, asyncNotify.getUmPayPlatformCertificate()), "签名错误！");
            send(asyncNotify.getMqConfig(), JacksonUtils.writeValueAsString(callbackParameters));
            CommonRedisUtils.del(uuid);

            privateKey = asyncNotify.getUmPayPrivateKey();
            retCode = "0000";
            retMsg = "处理成功";
        } catch (Exception e) {
            LogUtils.error("支付宝回调处理失败", this.getClass().getName(), "handleUmPayCallback", e, callbackParameters);
            retCode = "0001";
            retMsg = e.getMessage();
        }

        if (StringUtils.isBlank(privateKey)) {
            return "";
        }

        String signType = callbackParameters.get("sign_type");
        Map<String, String> responseMap = new HashMap<String, String>();
        responseMap.put("mer_id", callbackParameters.get("mer_id"));
        responseMap.put("version", Constants.UM_PAY_VERSION);
        responseMap.put("order_id", callbackParameters.get("order_id"));
        responseMap.put("mer_date", callbackParameters.get("mer_date"));
        responseMap.put("ret_code", retCode);
        responseMap.put("ret_msg", retMsg);
        responseMap.put("sign", UmPayUtils.generateSign(responseMap, privateKey, signType));
        responseMap.put("sign_type", signType);
        return "<META NAME=\"MobilePayPlatform\" CONTENT=\"" + WebUtils.concat(responseMap) + "\" />";
    }

    public String handleDadaOrderCallback(String body, String uuidKey) {
        try {
            Map<String, Object> bodyMap = JacksonUtils.readValueAsMap(body, String.class, Object.class);
            String uuid = MapUtils.getString(bodyMap, uuidKey);

            String asyncNotifyJson = CommonRedisUtils.get(uuid);
            ValidateUtils.notBlank(asyncNotifyJson, "异步通知不存在！");

            AsyncNotify asyncNotify = JacksonUtils.readValue(asyncNotifyJson, AsyncNotify.class);

            send(asyncNotify.getMqConfig(), body);
            CommonRedisUtils.del(uuid);
        } catch (Exception e) {
            LogUtils.error("支付宝回调处理失败", this.getClass().getName(), "handleUmPayCallback", e, body);
        }
        return "";
    }

    public void send(MqConfig mqConfig, String body) {
        String mqType = mqConfig.getMqType();
        if (Constants.MQ_TYPE_KAFKA.equals(mqType)) {
            KafkaUtils.send(mqConfig.getTopic(), body);
        } else if (Constants.MQ_TYPE_ROCKETMQ.equals(mqType)) {
            Message message = new Message();
            message.setBody(body.getBytes(Constants.CHARSET_UTF_8));
            message.setTopic(mqConfig.getTopic());
            RocketMQUtils.send(message);
        } else if (Constants.MQ_TYPE_RABBITMQ.equals(mqType)) {
            RabbitUtils.convertAndSend(mqConfig.getExchange(), mqConfig.getRoutingKey(), body);
        }
    }
}
