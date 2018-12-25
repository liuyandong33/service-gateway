package build.dream.gateway.listeners;

import build.dream.common.listeners.BasicServletContextListener;
import build.dream.gateway.jobs.JobScheduler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

@WebListener
public class GatewayServiceServletContextListener extends BasicServletContextListener {
    @Autowired
    private JobScheduler jobScheduler;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        super.contextInitialized(servletContextEvent);
        jobScheduler.scheduler();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
