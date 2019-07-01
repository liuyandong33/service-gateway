package build.dream.gateway.controllers;

import build.dream.common.utils.ApplicationHandler;
import build.dream.gateway.constants.Constants;
import build.dream.gateway.services.DJSWService;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/djsw")
public class DJSWController {
    @Service
    private DJSWService djswService;

    @RequestMapping(value = "/newOrder")
    @ResponseBody
    public String newOrder() {
        djswService.handleCallback(Constants.DJSW_TYPE_NEW_ORDER, ApplicationHandler.getRequestParameters());
        return Constants.SUCCESS;
    }
}
