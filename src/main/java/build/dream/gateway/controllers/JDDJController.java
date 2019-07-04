package build.dream.gateway.controllers;

import build.dream.common.utils.ApplicationHandler;
import build.dream.common.utils.JDDJUtils;
import build.dream.common.utils.LogUtils;
import build.dream.gateway.services.JDDJService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping(value = "/jddj")
public class JDDJController {
    @Autowired
    private JDDJService jddjService;

    @RequestMapping(value = "/callback")
    @ResponseBody
    public String callback() {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        try {
            String code = requestParameters.get("code");
            String token = requestParameters.get("token");
            jddjService.handleCallback(code, token);
            return JDDJUtils.buildSuccessResult();
        } catch (Exception e) {
            LogUtils.error("处理京东到家回调失败", this.getClass().getName(), "callback", e, requestParameters);
            return JDDJUtils.buildFailureResult(e.getMessage());
        }
    }
}
