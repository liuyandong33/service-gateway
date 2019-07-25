package build.dream.gateway.configurations;

import build.dream.common.server.HttpCondition;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional(value = HttpCondition.class)
public class HttpConfiguration {
    @Value(value = "${server.http.port}")
    private int port;

    @Bean
    public ServletWebServerFactory servletContainer() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setPort(port);

        TomcatServletWebServerFactory tomcatServletWebServerFactory = new TomcatServletWebServerFactory();
        tomcatServletWebServerFactory.addAdditionalTomcatConnectors(connector);

        return tomcatServletWebServerFactory;
    }
}
