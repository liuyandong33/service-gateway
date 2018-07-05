package build.dream.gateway.controllers;

import build.dream.common.controllers.BasicController;
import build.dream.common.utils.ApplicationHandler;
import build.dream.common.utils.LogUtils;
import build.dream.gateway.constants.Constants;
import build.dream.gateway.services.NotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping(value = "/notify")
public class NotifyController extends BasicController {
    @Autowired
    private NotifyService notifyService;

    @RequestMapping(value = "/alipayCallback")
    @ResponseBody
    public String alipayCallback() {
        String returnValue = null;
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        try {
            notifyService.alipayCallback(requestParameters);
            returnValue = Constants.SUCCESS;
        } catch (Exception e) {
            LogUtils.error("支付宝回调处理失败", className, "alipayCallback", e, requestParameters);
            returnValue = Constants.FAILURE;
        }
        return returnValue;
    }
}
