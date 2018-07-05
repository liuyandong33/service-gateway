package build.dream.gateway.jobs;

import build.dream.common.saas.domains.NotifyRecord;
import build.dream.common.utils.LogUtils;
import build.dream.gateway.services.NotifyService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class NotifyJob implements Job {
    private String className = this.getClass().getName();
    @Autowired
    private NotifyService notifyService;

    @Override
    public void execute(JobExecutionContext context) {
        List<NotifyRecord> notifyRecords = notifyService.obtainAllNotifyRecords();
        for (NotifyRecord notifyRecord : notifyRecords) {
            try {
                notifyService.executeNotify(notifyRecord);
            } catch (Exception e) {
                LogUtils.error("调用异步通知失败", className, "execute", e);
            }
        }
    }
}
