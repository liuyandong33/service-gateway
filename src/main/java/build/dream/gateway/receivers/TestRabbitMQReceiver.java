package build.dream.gateway.receivers;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TestRabbitMQReceiver {
    @RabbitListener(queues = "${rabbitmq.eleme.queue}")
    public void elemeMessageReceived(Message message) {
        System.out.println(message);
    }

    @RabbitListener(queues = "${rabbitmq.mei.tuan.queue}")
    public void meiTuanMessageReceived(Message message) {
        System.out.println(message);
    }
}
