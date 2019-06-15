package build.dream.gateway.controllers;

import build.dream.common.controllers.BasicController;
import build.dream.common.utils.ApplicationHandler;
import build.dream.common.utils.XmlUtils;
import build.dream.gateway.constants.Constants;
import build.dream.gateway.services.NotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
@RequestMapping(value = "/notify")
public class NotifyController extends BasicController {
    @Autowired
    private NotifyService notifyService;

    @RequestMapping(value = "/callback/{type}/{uuidKey}")
    @ResponseBody
    public String callback(@PathVariable(value = "type") String type, @PathVariable(value = "uuidKey") String uuidKey) throws IOException {
        if (Constants.NOTIFY_TYPE_WEI_XIN_PAY.equals(type)) {
            return notifyService.handleWeiXinPayCallback(XmlUtils.xmlStringToMap(ApplicationHandler.getRequestBody(Constants.CHARSET_NAME_UTF_8)), uuidKey);
        } else if (Constants.NOTIFY_TYPE_WEI_XIN_REFUND.equals(type)) {
            return notifyService.handleXinRefundCallback(XmlUtils.xmlStringToMap(ApplicationHandler.getRequestBody(Constants.CHARSET_NAME_UTF_8)), uuidKey);
        } else if (Constants.NOTIFY_TYPE_ALIPAY.equals(type)) {
            return notifyService.handleAlipayCallback(ApplicationHandler.getRequestParameters(), uuidKey);
        } else if (Constants.NOTIFY_TYPE_MIYA.equals(type)) {

        } else if (Constants.NOTIFY_TYPE_NEW_LAND.equals(type)) {

        } else if (Constants.NOTIFY_TYPE_UM_PAY.equals(type)) {

        }
        return null;
    }
}
