package build.dream.gateway.services;

import build.dream.common.beans.JDDJVenderInfo;
import build.dream.common.utils.*;
import build.dream.gateway.constants.Constants;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DJSWService {
    public String handleCallback(int type, Map<String, String> params) {
        try {
            String appKey = params.get("app_key");
            JDDJVenderInfo jddjVenderInfo = JDDJUtils.obtainJDDJVenderInfo(appKey);

            ValidateUtils.isTrue(JDDJUtils.verifySign(params, jddjVenderInfo.getAppSecret()), "签名错误！");

            Map<String, Object> message = new HashMap<String, Object>();
            message.put("tenantId", jddjVenderInfo.getTenantId());
            message.put("tenantCode", jddjVenderInfo.getTenantCode());
            message.put("type", type);
            message.put("body", params);

            String topic = jddjVenderInfo.getPartitionCode() + "_" + ConfigurationUtils.getConfiguration(Constants.JDDJ_MESSAGE_TOPIC);
            KafkaUtils.send(topic, JacksonUtils.writeValueAsString(message));
            return JDDJUtils.buildSuccessResult();
        } catch (Exception e) {
            LogUtils.error("处理京东到家回调失败", this.getClass().getName(), "handleCallback", e, params);
            return JDDJUtils.buildFailureResult(e.getMessage());
        }
    }
}
