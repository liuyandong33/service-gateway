package build.dream.gateway.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.util.UUID;

public class NotifyJob implements Job {
    @Override
    public void execute(JobExecutionContext context) {
        System.out.println(UUID.randomUUID().toString());
    }
}
