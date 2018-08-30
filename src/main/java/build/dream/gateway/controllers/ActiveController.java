package build.dream.gateway.controllers;

import build.dream.common.api.ApiRest;
import build.dream.common.utils.ApplicationHandler;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.jms.Destination;

@Controller
@RequestMapping(value = "/active")
public class ActiveController {
    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    @RequestMapping(value = "/send")
    @ResponseBody
    public String send() {
        String destinationName = ApplicationHandler.getRequestParameter("destinationName");
        String message = ApplicationHandler.getRequestParameter("message");
        Destination destination = new ActiveMQQueue(destinationName);
        jmsMessagingTemplate.convertAndSend(destination, message);
        return ApiRest.builder().message("消息发送成功！").successful(true).build().toJson();
    }
}
