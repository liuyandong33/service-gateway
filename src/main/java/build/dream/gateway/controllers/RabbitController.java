package build.dream.gateway.controllers;

import build.dream.common.api.ApiRest;
import build.dream.common.utils.ApplicationHandler;
import build.dream.common.utils.GsonUtils;
import build.dream.common.utils.ThreadUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping(value = "/rabbit")
public class RabbitController {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RequestMapping(value = "/send")
    @ResponseBody
    public String send() {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        for (int index = 0; index < 100; index++) {
            rabbitTemplate.convertAndSend("hello", requestParameters);
            ThreadUtils.sleepSafe(1000);
        }
        return GsonUtils.toJson(ApiRest.builder().message("发送成功").successful(true).build());
    }
}
