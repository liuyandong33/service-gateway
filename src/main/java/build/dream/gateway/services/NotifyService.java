package build.dream.gateway.services;

import build.dream.common.saas.domains.AsyncNotify;
import build.dream.common.utils.*;
import build.dream.gateway.constants.Constants;
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

        KafkaUtils.send(asyncNotify.getTopic(), JacksonUtils.writeValueAsString(callbackParameters));
    }
}
