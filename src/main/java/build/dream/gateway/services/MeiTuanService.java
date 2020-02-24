package build.dream.gateway.services;

import build.dream.common.domains.saas.Tenant;
import build.dream.common.utils.*;
import build.dream.gateway.constants.Constants;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class MeiTuanService {
    @Transactional(readOnly = true)
    public String handleCallback(Map<String, String> callbackParameters, Integer type) {
        String signKey = ConfigurationUtils.getConfiguration(Constants.MEI_TUAN_SIGN_KEY);
        Map<String, String> sortedMap = new TreeMap<String, String>(callbackParameters);
        String sign = sortedMap.remove("sign");
        StringBuilder finalData = new StringBuilder(signKey);
        for (Map.Entry<String, String> sortedRequestParameter : sortedMap.entrySet()) {
            finalData.append(sortedRequestParameter.getKey()).append(sortedRequestParameter.getValue());
        }
        ValidateUtils.isTrue(DigestUtils.sha1Hex(finalData.toString()).equals(sign), "签名错误！");

        String uuid = DigestUtils.md5Hex(JacksonUtils.writeValueAsString(callbackParameters));
        String key = "mei_tuan_callback_sign_" + uuid;
        boolean setnxSuccessful = CommonRedisUtils.setnx(key, key);
        if (setnxSuccessful) {
            return handleCallback(key, uuid, callbackParameters, type);
        }
        return Constants.MEI_TUAN_CALLBACK_SUCCESS_RETURN_VALUE;
    }

    public String handleCallback(String key, String uuid, Map<String, String> callbackParameters, Integer type) {
        try {
            CommonRedisUtils.expire(key, 1800, TimeUnit.SECONDS);

            String ePoiId = callbackParameters.get("ePoiId");
            Tenant tenant = TenantUtils.obtainTenantInfo(NumberUtils.createBigInteger(ePoiId.split("Z")[0]));

            if (Objects.nonNull(tenant)) {
                Map<String, Object> message = new HashMap<String, Object>();
                message.put("uuid", uuid);
                message.put("callbackParameters", callbackParameters);
                message.put("type", type);
                message.put("count", 10);

                String topic = tenant.getPartitionCode() + "_" + ConfigurationUtils.getConfiguration(Constants.MEI_TUAN_MESSAGE_TOPIC);
                KafkaUtils.send(topic, UUID.randomUUID().toString(), JacksonUtils.writeValueAsString(message));
            }
            return Constants.MEI_TUAN_CALLBACK_SUCCESS_RETURN_VALUE;
        } catch (Exception e) {
            CommonRedisUtils.del(uuid);
            throw e;
        }
    }
}
