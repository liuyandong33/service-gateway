package build.dream.gateway.controllers;

import build.dream.common.controllers.BasicController;
import build.dream.common.utils.ApplicationHandler;
import build.dream.common.utils.LogUtils;
import build.dream.common.utils.WebUtils;
import build.dream.common.utils.XmlUtils;
import build.dream.gateway.constants.Constants;
import build.dream.gateway.services.NotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
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
            notifyService.handleAlipayCallback(requestParameters);
            returnValue = Constants.SUCCESS;
        } catch (Exception e) {
            LogUtils.error("支付宝回调处理失败", className, "alipayCallback", e, requestParameters);
            returnValue = Constants.FAILURE;
        }
        return returnValue;
    }

    @RequestMapping(value = "/weiPayXinCallback")
    @ResponseBody
    public String weiXinCallback(HttpServletRequest httpServletRequest) {
        String returnValue = null;
        Map<String, String> requestParameters = null;
        try {
            requestParameters = XmlUtils.xmlInputStreamToMap(httpServletRequest.getInputStream());
            notifyService.handleWeiXinPayCallback(requestParameters);
            returnValue = Constants.WEI_XIN_PAY_CALLBACK_SUCCESS_RETURN_VALUE;
        } catch (Exception e) {
            LogUtils.error("微信支付回调处理失败", className, "weiPayXinCallback", e, requestParameters);
            returnValue = Constants.WEI_XIN_PAY_CALLBACK_FAILURE_RETURN_VALUE;
        }
        return returnValue;
    }
}
