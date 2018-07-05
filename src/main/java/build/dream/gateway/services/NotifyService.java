package build.dream.gateway.services;

import build.dream.common.saas.domains.NotifyRecord;
import build.dream.common.utils.*;
import build.dream.gateway.constants.Constants;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(rollbackFor = Exception.class)
    public void alipayCallback(Map<String, String> callbackParameters) throws NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, InvalidKeyException, IOException {
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

        int notifyResult = 0;
        try {
            String callbackResult = WebUtils.doPostWithRequestParameters(notifyRecord.getNotifyUrl(), callbackParameters);
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
}
