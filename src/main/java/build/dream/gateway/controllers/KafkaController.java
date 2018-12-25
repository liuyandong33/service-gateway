package build.dream.gateway.controllers;

import build.dream.common.api.ApiRest;
import build.dream.common.utils.ApplicationHandler;
import build.dream.common.utils.GsonUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Controller;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Controller
@RequestMapping(value = "/kafka")
public class KafkaController {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @RequestMapping(value = "/send")
    @ResponseBody
    public String send() throws ExecutionException, InterruptedException {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        ListenableFuture<SendResult<String, String>> listenableFuture = kafkaTemplate.send("zd1_eleme_message_topic", UUID.randomUUID().toString(), GsonUtils.toJson(requestParameters));
        SendResult<String, String> sendResult = listenableFuture.get();
        RecordMetadata recordMetadata = sendResult.getRecordMetadata();
        ProducerRecord<String, String> producerRecord = sendResult.getProducerRecord();
        return GsonUtils.toJson(ApiRest.builder().message("发送成功").successful(true).build());
    }
}
