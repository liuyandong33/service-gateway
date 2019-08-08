package build.dream.gateway.controllers;

import build.dream.gateway.constants.Constants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IndexController {
    @RequestMapping(value = "/")
    @ResponseBody
    public String index() {
        return Constants.OK;
    }
}
