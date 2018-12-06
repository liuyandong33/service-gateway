package build.dream.gateway.controllers;

import build.dream.common.beans.ComponentAccessToken;
import build.dream.common.beans.WeiXinOAuthToken;
import build.dream.common.beans.WeiXinUserInfo;
import build.dream.common.constants.Constants;
import build.dream.common.saas.domains.WeiXinAuthorizerInfo;
import build.dream.common.saas.domains.WeiXinAuthorizerToken;
import build.dream.common.saas.domains.WeiXinOpenPlatformApplication;
import build.dream.common.saas.domains.WeiXinPublicAccount;
import build.dream.common.utils.*;
import build.dream.gateway.models.weixin.ObtainUserInfoModel;
import build.dream.gateway.services.WeiXinService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/weiXin")
public class WeiXinController {
    private static final String WEI_XIN_AUTOMATED_TESTING_PUBLIC_ACCOUNT_APP_ID = "wx570bc396a51b8ff8";
    private static final String WEI_XIN_AUTOMATED_TESTING_PUBLIC_ACCOUNT_ORIGINAL_ID = "gh_3c884a361561";
    private static final String WEI_XIN_AUTOMATED_TESTING_MINI_PROGRAM_APP_ID = "wxd101a85aa106f53e";
    private static final String WEI_XIN_AUTOMATED_TESTING_MINI_PROGRAM_ORIGINAL_ID = "gh_8dad206e9538";
    @Autowired
    private WeiXinService weiXinService;

    @RequestMapping(value = "/callback")
    @ResponseBody
    public String callback(HttpServletRequest httpServletRequest) throws IOException, DocumentException {
        InputStream inputStream = httpServletRequest.getInputStream();
        String requestBody = IOUtils.toString(inputStream);
        Map<String, String> requestBodyMap = XmlUtils.xmlStringToMap(requestBody);
        String appId = requestBodyMap.get("AppId");
        String encrypt = requestBodyMap.get("Encrypt");

        WeiXinOpenPlatformApplication weiXinOpenPlatformApplication = weiXinService.obtainWeiXinOpenPlatformApplication(appId);
        if (weiXinOpenPlatformApplication == null) {
            return Constants.SUCCESS;
        }
        String encodingAesKey = weiXinOpenPlatformApplication.getEncodingAesKey();
        String xmlContent = weiXinService.decrypt(encrypt, encodingAesKey);
        Map<String, String> encryptMap = XmlUtils.xmlStringToMap(xmlContent);

        ValidateUtils.isTrue(appId.equals(encryptMap.get("AppId")), "消息内容非法！");

        String infoType = encryptMap.get("InfoType");
        if ("component_verify_ticket".equals(infoType)) {
            String componentVerifyTicket = encryptMap.get("ComponentVerifyTicket");
            CacheUtils.hset(Constants.KEY_WEI_XIN_COMPONENT_VERIFY_TICKET, appId, componentVerifyTicket);
        } else if ("authorized".equals(infoType)) {

        } else if ("unauthorized".equals(infoType)) {

        } else if ("updateauthorized".equals(infoType)) {

        }

        return Constants.SUCCESS;
    }

    @RequestMapping(value = "/demo")
    public ModelAndView demo() throws IOException {
        String componentAppId = "wx3465dea1e67a3131";
        String componentAppSecret = "587ad4920d1767e10ce7503da86ac1a3";
        String preAuthCode = WeiXinUtils.obtainPreAuthCode(componentAppId, componentAppSecret);
        String redirectUri = "http://check-local.smartpos.top/zd1/ct2/weiXin/callback?tenantId=100&componentAppId=" + componentAppId;
        String url = WeiXinUtils.generateComponentLoginPageUrl(componentAppId, preAuthCode, redirectUri, "3");
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("weiXin/demo");
        modelAndView.addObject("url", url);
        return modelAndView;
    }

    @RequestMapping(value = "/authCallback")
    @ResponseBody
    public String authCallback() throws IOException {
        String tenantId = ApplicationHandler.getRequestParameter("tenantId");
        String componentAppId = ApplicationHandler.getRequestParameter("componentAppId");
        String authorizationCode = ApplicationHandler.getRequestParameter("auth_code");
        SearchModel searchModel = new SearchModel(true);
        searchModel.addSearchCondition(WeiXinOpenPlatformApplication.ColumnName.APP_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, componentAppId);

        WeiXinOpenPlatformApplication weiXinOpenPlatformApplication = DatabaseHelper.find(WeiXinOpenPlatformApplication.class, searchModel);

        String componentAppSecret = weiXinOpenPlatformApplication.getAppSecret();
        String componentAccessToken = WeiXinUtils.obtainComponentAccessToken(componentAppId, componentAppSecret).getComponentAccessToken();
        WeiXinAuthorizerToken weiXinAuthorizerToken = WeiXinUtils.apiQueryAuth(componentAccessToken, componentAppId, authorizationCode);
        WeiXinAuthorizerInfo weiXinAuthorizerInfo = WeiXinUtils.apiGetAuthorizerInfo(componentAccessToken, componentAppId, weiXinAuthorizerToken.getAuthorizerAppId());
        weiXinAuthorizerInfo.setTenantId(BigInteger.valueOf(Long.valueOf(tenantId)));
        weiXinAuthorizerInfo.setCreateUserId(BigInteger.ONE);
        weiXinAuthorizerInfo.setLastUpdateUserId(BigInteger.ONE);
        DatabaseHelper.insert(weiXinAuthorizerInfo);
        return Constants.SUCCESS;
    }

    @RequestMapping(value = "/messageCallback/{appId}")
    @ResponseBody
    public String messageCallback(@PathVariable(value = "appId") String appId, HttpServletRequest httpServletRequest) throws IOException, DocumentException {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters(httpServletRequest);
        InputStream inputStream = httpServletRequest.getInputStream();
        String requestBody = IOUtils.toString(inputStream);

        WeiXinOpenPlatformApplication weiXinOpenPlatformApplication = weiXinService.obtainWeiXinOpenPlatformApplication(appId);
        if (weiXinOpenPlatformApplication == null) {
            return Constants.SUCCESS;
        }

        String componentAppId = weiXinOpenPlatformApplication.getAppId();
        String componentAppSecret = weiXinOpenPlatformApplication.getAppSecret();
        String encodingAesKey = weiXinOpenPlatformApplication.getEncodingAesKey();
        String token = weiXinOpenPlatformApplication.getToken();

        String message = weiXinService.decrypt(requestBody, encodingAesKey);
        Map<String, String> xmlMap = XmlUtils.xmlStringToMap(message);
        String fromUserName = xmlMap.get("FromUserName");
        String toUserName = xmlMap.get("ToUserName");

        String returnValue = null;
        if (WEI_XIN_AUTOMATED_TESTING_PUBLIC_ACCOUNT_APP_ID.equals(appId) || WEI_XIN_AUTOMATED_TESTING_MINI_PROGRAM_APP_ID.equals(appId)) {
            String content = xmlMap.get("Content");
            String nonce = requestParameters.get("nonce");
            if ("TESTCOMPONENT_MSG_TYPE_TEXT".equals(content)) {
                String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
                Map<String, String> map = new HashMap<String, String>();
                map.put("ToUserName", fromUserName);
                map.put("FromUserName", toUserName);
                map.put("CreateTime", timeStamp);
                map.put("MsgType", "text");
                map.put("Content", "TESTCOMPONENT_MSG_TYPE_TEXT_callback");
                map.put("MsgId", String.valueOf(RandomUtils.nextLong()));

                String encryptedData = weiXinService.encrypt(XmlUtils.mapToXmlString(map), encodingAesKey, componentAppId);
                String[] array = new String[]{token, timeStamp, nonce, encryptedData};
                Arrays.sort(array);

                String msgSignature = DigestUtils.sha1Hex(StringUtils.join(array, ""));

                Map<String, String> encryptMap = new HashMap<String, String>();
                encryptMap.put("ToUserName", fromUserName);
                encryptMap.put("Encrypt", encryptedData);
                encryptMap.put("Nonce", nonce);
                encryptMap.put("TimeStamp", timeStamp);
                encryptMap.put("MsgSignature", msgSignature);

                returnValue = XmlUtils.mapToXmlString(encryptMap);
            } else if (content.startsWith("QUERY_AUTH_CODE")) {
                String queryAuthCode = content.substring(16);
                ComponentAccessToken componentAccessToken = WeiXinUtils.obtainComponentAccessToken(componentAppId, componentAppSecret);
                WeiXinAuthorizerToken weiXinAuthorizerToken = WeiXinUtils.apiQueryAuth(componentAccessToken.getComponentAccessToken(), componentAppId, queryAuthCode);

                Map<String, Object> messageBody = new HashMap<String, Object>();
                messageBody.put("touser", fromUserName);
                messageBody.put("msgtype", "text");

                Map<String, Object> text = new HashMap<String, Object>();
                text.put("content", queryAuthCode + "_from_api");
                messageBody.put("text", text);

                WeiXinUtils.sendCustomMessage(weiXinAuthorizerToken.getAuthorizerAccessToken(), GsonUtils.toJson(messageBody));

                returnValue = Constants.SUCCESS;
            }
        } else {
            String msgType = xmlMap.get("MsgType");
            if ("event".equals(msgType)) {
                String event = xmlMap.get("Event");
                if ("subscribe".equals(event)) {

                } else if ("unsubscribe".equals(event)) {

                }
            }
        }
        return returnValue;
    }
}
