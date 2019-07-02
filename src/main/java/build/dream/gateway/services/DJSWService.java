package build.dream.gateway.services;

import build.dream.common.utils.*;
import build.dream.gateway.constants.Constants;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DJSWService {
    public String handleCallback(int type, Map<String, String> params) {
        try {
            String token = params.get("token");
            String info = CommonRedisUtils.hget(Constants.KEY_JDDJ_VENDER_INFOS, token);
            Map<String, Object> infoMap = JacksonUtils.readValueAsMap(info, String.class, Object.class);

            Map<String, Object> message = new HashMap<String, Object>();
            message.put("tenantId", MapUtils.getLongValue(infoMap, "tenantId"));
            message.put("tenantCode", MapUtils.getString(infoMap, "tenantCode"));
            message.put("branchId", MapUtils.getString(infoMap, "branchId"));
            message.put("type", type);
            message.put("body", params);

            String partitionCode = MapUtils.getString(infoMap, "partitionCode");
            String topic = partitionCode + "_" + ConfigurationUtils.getConfiguration(Constants.JDDJ_MESSAGE_TOPIC);
            KafkaUtils.send(topic, JacksonUtils.writeValueAsString(message));
            return JDDJUtils.buildSuccessResult();
        } catch (Exception e) {
            return JDDJUtils.buildFailureResult(e.getMessage());
        }
    }
}
