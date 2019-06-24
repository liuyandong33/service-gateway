package build.dream.gateway.controllers;

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
public class NotifyController {
    @Autowired
    private NotifyService notifyService;

    @RequestMapping(value = "/callback/{type}/{uuidKey}")
    @ResponseBody
    public String callback(@PathVariable(value = "type") String type, @PathVariable(value = "uuidKey") String uuidKey) throws IOException {
        switch (type) {
            case Constants.NOTIFY_TYPE_WEI_XIN_PAY:
                return notifyService.handleWeiXinPayCallback(XmlUtils.xmlStringToMap(ApplicationHandler.getRequestBody(Constants.CHARSET_NAME_UTF_8)), uuidKey);
            case Constants.NOTIFY_TYPE_WEI_XIN_REFUND:
                return notifyService.handleXinRefundCallback(XmlUtils.xmlStringToMap(ApplicationHandler.getRequestBody(Constants.CHARSET_NAME_UTF_8)), uuidKey);
            case Constants.NOTIFY_TYPE_ALIPAY:
                return notifyService.handleAlipayCallback(ApplicationHandler.getRequestParameters(), uuidKey);
            case Constants.NOTIFY_TYPE_MIYA:
                return notifyService.handleMiyaCallback(XmlUtils.xmlStringToMap(ApplicationHandler.getRequestBody(Constants.CHARSET_NAME_UTF_8)), uuidKey);
            case Constants.NOTIFY_TYPE_NEW_LAND:
                return notifyService.handleNewLandCallback(ApplicationHandler.getRequestBody(Constants.CHARSET_NAME_UTF_8), uuidKey);
            case Constants.NOTIFY_TYPE_UM_PAY:
                return notifyService.handleUmPayCallback(ApplicationHandler.getRequestParameters(), uuidKey);
            case Constants.NOTIFY_TYPE_DADA_ORDER_CALLBACK:
                return notifyService.handleDadaOrderCallback(ApplicationHandler.getRequestBody(Constants.CHARSET_NAME_UTF_8), uuidKey);
            default:
                return null;
        }
    }
}
