package build.dream.gateway.services;

import build.dream.common.saas.domains.Tenant;
import build.dream.common.utils.*;
import build.dream.gateway.constants.Constants;
import build.dream.gateway.mappers.ElemeMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ElemeService {
    @Autowired
    private ElemeMapper elemeMapper;

    @Transactional(readOnly = true)
    public String handleCallback(String callbackRequestBody) {
        Map<String, Object> callbackRequestBodyMap = JacksonUtils.readValueAsMap(callbackRequestBody, String.class, Object.class);
        ValidateUtils.isTrue(ElemeUtils.verifySignature(callbackRequestBodyMap, ConfigurationUtils.getConfiguration(Constants.ELEME_APP_SECRET)), "签名校验未通过！");

        String uuid = DigestUtils.md5Hex(MapUtils.getString(callbackRequestBodyMap, "message"));
        String key = "eleme_callback_sign_" + uuid;
        boolean setnxSuccessful = CommonRedisUtils.setnx(key, key);
        if (setnxSuccessful) {
            return handleCallback(key, uuid, callbackRequestBodyMap);
        }
        return Constants.ELEME_ORDER_CALLBACK_SUCCESS_RETURN_VALUE;
    }

    private String handleCallback(String key, String uuid, Map<String, Object> callbackRequestBodyMap) {
        try {
            CommonRedisUtils.expire(key, 1800, TimeUnit.SECONDS);
            BigInteger shopId = BigInteger.valueOf(MapUtils.getLongValue(callbackRequestBodyMap, "shopId"));

            Map<String, Object> mappingInfo = elemeMapper.obtainMappingInfo(shopId);
            if (MapUtils.isEmpty(mappingInfo)) {
                return Constants.ELEME_ORDER_CALLBACK_SUCCESS_RETURN_VALUE;
            }

            BigInteger tenantId = BigInteger.valueOf(MapUtils.getLongValue(mappingInfo, "tenantId"));
            String tenantCode = MapUtils.getString(mappingInfo, "tenantCode");
            BigInteger branchId = BigInteger.valueOf(MapUtils.getLongValue(mappingInfo, "branchId"));
            String partitionCode = MapUtils.getString(mappingInfo, "partitionCode");
            Map<String, Object> elemeMessage = new HashMap<String, Object>();
            elemeMessage.put("uuid", uuid);
            elemeMessage.put("callbackRequestBody", callbackRequestBodyMap);
            elemeMessage.put("count", 10);
            elemeMessage.put("tenantId", tenantId);
            elemeMessage.put("tenantCode", tenantCode);
            elemeMessage.put("branchId", branchId);

            String topic = partitionCode + "_" + ConfigurationUtils.getConfiguration(Constants.ELEME_MESSAGE_TOPIC);
            KafkaUtils.send(topic, uuid, GsonUtils.toJson(elemeMessage));
            return Constants.ELEME_ORDER_CALLBACK_SUCCESS_RETURN_VALUE;
        } catch (Exception e) {
            CommonRedisUtils.del(key);
            throw e;
        }
    }
}
