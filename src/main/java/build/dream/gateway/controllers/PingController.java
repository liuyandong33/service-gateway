package build.dream.gateway.controllers;

import build.dream.gateway.constants.Constants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/ping")
public class PingController {
    @RequestMapping(value = "/ok")
    @ResponseBody
    public String ping() {
        return Constants.OK;
    }

    @RequestMapping(value = "/success")
    @ResponseBody
    public String success() {
        return Constants.SUCCESS;
    }

    @RequestMapping(value = "/pong")
    @ResponseBody
    public String pong() {
        return Constants.PONG;
    }
}
