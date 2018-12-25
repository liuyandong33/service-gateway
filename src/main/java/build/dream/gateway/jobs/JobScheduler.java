package build.dream.gateway.jobs;

import build.dream.common.utils.ConfigurationUtils;
import build.dream.gateway.constants.Constants;
import org.apache.commons.lang.StringUtils;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

@Component
public class JobScheduler {
    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    public void scheduler() {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        try {
            String notifyJobCronExpression = ConfigurationUtils.getConfiguration(Constants.NOTIFY_JOB_CRON_EXPRESSION);
            if (StringUtils.isNotBlank(notifyJobCronExpression)) {
                JobDetail notifyJobDetail = JobBuilder.newJob(NotifyJob.class).withIdentity("notifyJob", "gatewayJobGroup").build();
                CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(notifyJobCronExpression);
                Trigger notifyJobCronTrigger = TriggerBuilder.newTrigger().withIdentity("notifyJob", "gatewayJobTriggerGroup").withSchedule(cronScheduleBuilder).build();
                scheduler.scheduleJob(notifyJobDetail, notifyJobCronTrigger);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
