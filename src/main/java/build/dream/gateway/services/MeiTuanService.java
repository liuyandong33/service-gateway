package build.dream.gateway.services;

import build.dream.common.saas.domains.Tenant;
import build.dream.common.utils.*;
import build.dream.gateway.constants.Constants;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class MeiTuanService {
    @Transactional(readOnly = true)
    public String handleCallback(Map<String, String> callbackParameters, Integer type) throws ExecutionException, InterruptedException {
        String uuid = DigestUtils.md5Hex(GsonUtils.toJson(callbackParameters));
        boolean setnxSuccessful = CacheUtils.setnx(uuid, uuid);
        String handleResult = null;
        if (setnxSuccessful) {
            handleCallback(uuid, callbackParameters, type);
        } else {
            handleResult = Constants.MEI_TUAN_CALLBACK_SUCCESS_RETURN_VALUE;
        }
        return handleResult;
    }

    public String handleCallback(String uuid, Map<String, String> callbackParameters, Integer type) throws ExecutionException, InterruptedException {
        String handleResult = null;
        try {
            CacheUtils.expire(uuid, 1800, TimeUnit.SECONDS);

            String ePoiId = callbackParameters.get("ePoiId");
            ApplicationHandler.notBlank(ePoiId, "ePoiId");

            String[] tenantIdAndBranchIdArray = ePoiId.split("Z");
            BigInteger tenantId = NumberUtils.createBigInteger(tenantIdAndBranchIdArray[0]);
            SearchModel searchModel = new SearchModel(true);
            searchModel.addSearchCondition(Tenant.ColumnName.ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, tenantId);
            Tenant tenant = DatabaseHelper.find(Tenant.class, searchModel);

            if (tenant == null) {
                handleResult = Constants.MEI_TUAN_CALLBACK_SUCCESS_RETURN_VALUE;
            } else {
                Map<String, Object> message = new HashMap<String, Object>();
                message.put("uuid", uuid);
                message.put("callbackParameters", callbackParameters);
                message.put("type", type);
                message.put("count", 10);

                String topic = tenant.getPartitionCode() + "_" + ConfigurationUtils.getConfiguration(Constants.MEI_TUAN_MESSAGE_TOPIC);
                ListenableFuture<SendResult<String, String>> listenableFuture = KafkaUtils.send(topic, UUID.randomUUID().toString(), GsonUtils.toJson(message));
                SendResult<String, String> sendResult = listenableFuture.get();
                ProducerRecord<String, String> producerRecord = sendResult.getProducerRecord();
                RecordMetadata recordMetadata = sendResult.getRecordMetadata();
            }
        } catch (Exception e) {
            CacheUtils.delete(uuid);
            throw e;
        }
        return handleResult;
    }
}
