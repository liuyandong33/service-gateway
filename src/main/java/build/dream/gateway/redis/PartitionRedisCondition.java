package build.dream.gateway.redis;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class PartitionRedisCondition implements Condition {
    private Environment environment;

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        environment = context.getEnvironment();

        if (containsProperty("partition.redis.host") && containsProperty("partition.redis.port") && containsProperty("partition.redis.password")) {
            return true;
        }

        return false;
    }

    private boolean containsProperty(String key) {
        return StringUtils.isNotBlank(environment.getProperty(key));
    }
}
