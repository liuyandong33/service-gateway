package build.dream.gateway.services;

import build.dream.common.saas.domains.NotifyRecord;
import build.dream.common.utils.*;
import build.dream.gateway.constants.Constants;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class NotifyService {
    @Autowired
    private RestTemplate restTemplate;

    @Transactional(rollbackFor = Exception.class)
    public void handleAlipayCallback(Map<String, String> callbackParameters) throws NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, InvalidKeyException, IOException {
        String outTradeNo = callbackParameters.get("out_trade_no");
        SearchModel searchModel = new SearchModel(true);
        searchModel.addSearchCondition("uuid", Constants.SQL_OPERATION_SYMBOL_EQUAL, outTradeNo);
        NotifyRecord notifyRecord = DatabaseHelper.find(NotifyRecord.class, searchModel);
        ValidateUtils.notNull(notifyRecord, "通知记录不存在！");

        if (notifyRecord.getNotifyResult() != 1) {
            return;
        }

        // 开始验签
        Map<String, String> sortedParameters = new TreeMap<String, String>(callbackParameters);
        String sign = sortedParameters.remove("sign");
        String charset = sortedParameters.get("charset");
        List<String> sortedParameterPair = new ArrayList<String>();
        for (Map.Entry<String, String> entry : sortedParameters.entrySet()) {
            sortedParameterPair.add(entry.getKey() + "=" + entry.getValue());
        }
        boolean isOk = AlipayUtils.verifySign(StringUtils.join(sortedParameterPair, "&"), notifyRecord.getAlipaySignType(), sign, charset, notifyRecord.getAlipayPublicKey());
        ValidateUtils.isTrue(isOk, "签名验证未通过！");
        executeNotify(notifyRecord, callbackParameters);
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleWeiXinPayCallback(Map<String, String> callbackParameters) throws IOException {
        String outTradeNo = callbackParameters.get("out_trade_no");
        SearchModel searchModel = new SearchModel(true);
        searchModel.addSearchCondition("uuid", Constants.SQL_OPERATION_SYMBOL_EQUAL, outTradeNo);
        NotifyRecord notifyRecord = DatabaseHelper.find(NotifyRecord.class, searchModel);
        ValidateUtils.notNull(notifyRecord, "通知记录不存在！");

        if (notifyRecord.getNotifyResult() != 1) {
            return;
        }

        // 开始验签
        executeNotify(notifyRecord, callbackParameters);
    }

    public void executeNotify(NotifyRecord notifyRecord, Map<String, String> callbackParameters) throws IOException {
        int notifyResult = 0;
        try {
            String callbackResult = restTemplate.postForObject(notifyRecord.getNotifyUrl(), ProxyUtils.buildHttpEntity(callbackParameters), String.class);
            if (Constants.SUCCESS.equals(callbackResult)) {
                notifyResult = 2;
            } else {
                notifyResult = 3;
            }
        } catch (Exception e) {
            notifyResult = 3;
        }
        notifyRecord.setNotifyResult(notifyResult);
        notifyRecord.setExternalSystemNotifyRequestBody(GsonUtils.toJson(callbackParameters));
        DatabaseHelper.update(notifyRecord);
    }

    @Transactional(readOnly = true)
    public List<NotifyRecord> obtainAllNotifyRecords() {
        SearchModel searchModel = new SearchModel(true);
        searchModel.addSearchCondition("notify_result", Constants.SQL_OPERATION_SYMBOL_EQUAL, 3);
        List<NotifyRecord> notifyRecords = DatabaseHelper.findAll(NotifyRecord.class, searchModel);
        return notifyRecords;
    }

    @Transactional(rollbackFor = Exception.class)
    public void executeNotify(NotifyRecord notifyRecord) throws IOException {
        Map<String, String> callbackParameters = JacksonUtils.readValueAsMap(notifyRecord.getExternalSystemNotifyRequestBody(), String.class, String.class);
        String callbackResult = restTemplate.postForObject(notifyRecord.getNotifyUrl(), ProxyUtils.buildHttpEntity(callbackParameters), String.class);
        if (Constants.SUCCESS.equals(callbackResult)) {
            notifyRecord.setNotifyResult(2);
            DatabaseHelper.update(notifyRecord);
        }
    }
}
