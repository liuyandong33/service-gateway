package build.dream.gateway.controllers;

import build.dream.common.controllers.BasicController;
import build.dream.common.utils.ApplicationHandler;
import build.dream.common.utils.IOUtils;
import build.dream.common.utils.LogUtils;
import build.dream.gateway.constants.Constants;
import build.dream.gateway.services.ElemeService;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(value = "/eleme")
public class ElemeController extends BasicController {
    @Autowired
    private ElemeService elemeService;

    @RequestMapping(value = "/elemeCallback")
    @ResponseBody
    public String elemeCallback() {
        String returnValue = null;
        try {
            HttpServletRequest httpServletRequest = ApplicationHandler.getHttpServletRequest();
            String method = httpServletRequest.getMethod();
            if (Constants.REQUEST_METHOD_GET.equals(method)) {
                returnValue = Constants.ELEME_ORDER_CALLBACK_SUCCESS_RETURN_VALUE;
            } else if (Constants.REQUEST_METHOD_POST.equals(method)) {
                String callbackRequestBody = IOUtils.toString(httpServletRequest.getInputStream());
                Validate.notNull(callbackRequestBody, "回调请求体不能为空！");
                returnValue = elemeService.handleElemeCallback(callbackRequestBody);
            }
        } catch (Exception e) {
            LogUtils.error("饿了么订单回调处理失败", className, "elemeCallback", e);
            returnValue = e.getMessage();
        }
        return returnValue;
    }
}
