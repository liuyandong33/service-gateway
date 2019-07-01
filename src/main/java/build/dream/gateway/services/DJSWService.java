package build.dream.gateway.services;

import build.dream.common.utils.CommonRedisUtils;
import build.dream.common.utils.ConfigurationUtils;
import build.dream.common.utils.JacksonUtils;
import build.dream.common.utils.KafkaUtils;
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
            return buildSuccessResult();
        } catch (Exception e) {
            return buildFailureResult(e.getMessage());
        }
    }

    public String buildResult(String code, String msg, String data) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("code", code);
        result.put("msg", msg);
        result.put("data", data);
        return JacksonUtils.writeValueAsString(result);
    }

    public String buildSuccessResult() {
        return buildResult("0", "success", "");
    }

    public String buildSuccessResult(String msg) {
        return buildResult("0", msg, "");
    }

    public String buildSuccessResult(String msg, String data) {
        return buildResult("0", msg, data);
    }

    public String buildFailureResult() {
        return buildResult("-1", "failure", "");
    }

    public String buildFailureResult(String msg) {
        return buildResult("-1", msg, "");
    }

    public String buildFailureResult(String msg, String data) {
        return buildResult("-1", msg, data);
    }
}
