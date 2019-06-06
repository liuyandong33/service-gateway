package build.dream.gateway.services;

import build.dream.common.saas.domains.AsyncNotify;
import build.dream.common.utils.*;
import build.dream.gateway.constants.Constants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
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
     * @throws IOException
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleAlipayCallback(Map<String, String> callbackParameters) throws IOException {
        String outTradeNo = callbackParameters.get("out_trade_no");
        SearchModel searchModel = new SearchModel(true);
        searchModel.addSearchCondition(AsyncNotify.ColumnName.UUID, Constants.SQL_OPERATION_SYMBOL_EQUAL, outTradeNo);
        AsyncNotify asyncNotify = DatabaseHelper.find(AsyncNotify.class, searchModel);
        ValidateUtils.notNull(asyncNotify, "异步通知不存在！");

        String externalSystemNotifyRequestBody = JacksonUtils.writeValueAsString(callbackParameters);
        asyncNotify.setExternalSystemNotifyRequestBody(externalSystemNotifyRequestBody);
        asyncNotify.setNotifyResult(Constants.NOTIFY_RESULT_NOTIFY_SUCCESS);
        DatabaseHelper.update(asyncNotify);

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
    }

    /**
     * 处理微信支付回调
     *
     * @param callbackParameters
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleWeiXinPayCallback(Map<String, String> callbackParameters) {
        String outTradeNo = callbackParameters.get("out_trade_no");
        SearchModel searchModel = SearchModel.builder()
                .autoSetDeletedFalse()
                .addSearchCondition(AsyncNotify.ColumnName.UUID, Constants.SQL_OPERATION_SYMBOL_EQUAL, outTradeNo)
                .build();
        AsyncNotify asyncNotify = DatabaseHelper.find(AsyncNotify.class, searchModel);
        ValidateUtils.notNull(asyncNotify, "异步通知不存在！");

        String externalSystemNotifyRequestBody = JacksonUtils.writeValueAsString(callbackParameters);
        asyncNotify.setExternalSystemNotifyRequestBody(externalSystemNotifyRequestBody);
        asyncNotify.setNotifyResult(Constants.NOTIFY_RESULT_NOTIFY_SUCCESS);
        DatabaseHelper.update(asyncNotify);

        KafkaUtils.send(asyncNotify.getTopic(), externalSystemNotifyRequestBody);
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
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleXinRefundCallback(Map<String, String> callbackParameters) {
        String appId = callbackParameters.get("appid");
        String reqInfo = callbackParameters.get("req_info");
        String apiV3Key = obtainApiV3Key(appId);
        byte[] bytes = AESUtils.decrypt(Base64.decodeBase64(reqInfo), DigestUtils.md5Hex(apiV3Key).getBytes(Constants.CHARSET_UTF_8), AESUtils.ALGORITHM_AES_ECB_PKCS7PADDING, AESUtils.PROVIDER_NAME_BC);
        String plaintext = new String(bytes, Constants.CHARSET_UTF_8);
        Map<String, String> plaintextMap = XmlUtils.xmlStringToMap(plaintext);
        String outRefundNo = plaintextMap.get("out_refund_no");

        SearchModel searchModel = SearchModel.builder()
                .autoSetDeletedFalse()
                .addSearchCondition(AsyncNotify.ColumnName.UUID, Constants.SQL_OPERATION_SYMBOL_EQUAL, outRefundNo)
                .build();
        AsyncNotify asyncNotify = DatabaseHelper.find(AsyncNotify.class, searchModel);
        ValidateUtils.notNull(asyncNotify, "异步通知不存在！");

        String externalSystemNotifyRequestBody = JacksonUtils.writeValueAsString(plaintextMap);
        asyncNotify.setExternalSystemNotifyRequestBody(externalSystemNotifyRequestBody);
        asyncNotify.setNotifyResult(Constants.NOTIFY_RESULT_NOTIFY_SUCCESS);
        DatabaseHelper.update(asyncNotify);

        KafkaUtils.send(asyncNotify.getTopic(), externalSystemNotifyRequestBody);
    }
}
