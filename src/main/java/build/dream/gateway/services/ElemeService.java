package build.dream.gateway.services;

import build.dream.common.saas.domains.Tenant;
import build.dream.common.utils.*;
import build.dream.gateway.constants.Constants;
import build.dream.gateway.mappers.TenantMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class ElemeService {
    @Transactional(readOnly = true)
    public String handleCallback(String callbackRequestBody) throws ExecutionException, InterruptedException {
        Map<String, Object> callbackRequestBodyMap = JacksonUtils.readValueAsMap(callbackRequestBody, String.class, Object.class);
//        Validate.isTrue(ElemeUtils.checkSignature(callbackRequestBodyMap, ConfigurationUtils.getConfiguration(Constants.ELEME_APP_SECRET)), "签名校验未通过！");

        String uuid = DigestUtils.md5Hex(MapUtils.getString(callbackRequestBodyMap, "message"));
        String key = "eleme_callback_sign_" + uuid;
        boolean setnxSuccessful = CacheUtils.setnx(key, key);
        String handleResult = null;
        if (setnxSuccessful) {
            handleResult = handleCallback(key, uuid, callbackRequestBodyMap);
        } else {
            handleResult = Constants.ELEME_ORDER_CALLBACK_SUCCESS_RETURN_VALUE;
        }
        return handleResult;
    }

    private String handleCallback(String key, String uuid, Map<String, Object> callbackRequestBodyMap) throws ExecutionException, InterruptedException {
        String handleResult = null;
        try {
            CacheUtils.expire(key, 1800, TimeUnit.SECONDS);
            BigInteger shopId = BigInteger.valueOf(MapUtils.getLongValue(callbackRequestBodyMap, "shopId"));

            Map<String, Object> tenantInfo = DatabaseHelper.callMapperMethod(TenantMapper.class, "obtainTenantInfo", TupleUtils.buildTuple2(BigInteger.class, shopId));
            if (MapUtils.isEmpty(tenantInfo)) {
                handleResult = Constants.ELEME_ORDER_CALLBACK_SUCCESS_RETURN_VALUE;
            } else {
                BigInteger tenantId = BigInteger.valueOf(MapUtils.getLongValue(tenantInfo, Tenant.FieldName.ID));
                String partitionCode = MapUtils.getString(tenantInfo, Tenant.FieldName.PARTITION_CODE);
                Map<String, Object> elemeMessage = new HashMap<String, Object>();
                elemeMessage.put("uuid", uuid);
                elemeMessage.put("callbackRequestBody", callbackRequestBodyMap);
                elemeMessage.put("count", 10);
                elemeMessage.put("tenantId", tenantId);

                String topic = partitionCode + "_" + ConfigurationUtils.getConfiguration(Constants.ELEME_MESSAGE_TOPIC);
                ListenableFuture<SendResult<String, String>> listenableFuture = KafkaUtils.send(topic, uuid, GsonUtils.toJson(elemeMessage));
                SendResult<String, String> sendResult = listenableFuture.get();
                ProducerRecord<String, String> producerRecord = sendResult.getProducerRecord();
                RecordMetadata recordMetadata = sendResult.getRecordMetadata();
                handleResult = Constants.ELEME_ORDER_CALLBACK_SUCCESS_RETURN_VALUE;
            }
        } catch (Exception e) {
            CacheUtils.delete(key);
            throw e;
        }
        return handleResult;
    }
}
