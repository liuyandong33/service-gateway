package build.dream.gateway.controllers;

import build.dream.common.utils.ApplicationHandler;
import build.dream.common.utils.CommonRedisUtils;
import build.dream.common.utils.JDDJUtils;
import build.dream.common.utils.JacksonUtils;
import build.dream.gateway.constants.Constants;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping(value = "/jddj")
public class JDDJController {
    @RequestMapping(value = "/callback")
    @ResponseBody
    public String callback() {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        String token = requestParameters.get("token");
        if (StringUtils.isNotBlank(token)) {
            Map<String, Object> tokenMap = JacksonUtils.readValueAsMap(token, String.class, Object.class);
            String venderId = MapUtils.getString(tokenMap, "venderId");
            CommonRedisUtils.hset(Constants.KEY_JDDJ_TOKENS, venderId, JacksonUtils.writeValueAsString(tokenMap));
        }

        String code = requestParameters.get("code");
        if (StringUtils.isNotBlank(code)) {
            Map<String, Object> codeMap = JacksonUtils.readValueAsMap(token, String.class, Object.class);
            String venderId = MapUtils.getString(codeMap, "venderId");
            CommonRedisUtils.hset(Constants.KEY_JDDJ_CODES, venderId, JacksonUtils.writeValueAsString(codeMap));
        }

        return JDDJUtils.buildSuccessResult();
    }
}
