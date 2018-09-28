package build.dream.gateway.controllers;

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
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomUtils;
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
import java.util.UUID;

@Controller
@RequestMapping(value = "/weiXin")
public class WeiXinController {
    @Autowired
    private WeiXinService weiXinService;

    @RequestMapping(value = "/obtainUserInfo", method = RequestMethod.GET)
    public String obtainUserInfo() throws Exception {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        ObtainUserInfoModel obtainUserInfoModel = ApplicationHandler.instantiateObject(ObtainUserInfoModel.class, requestParameters);
        obtainUserInfoModel.validateAndThrow();

        String appId = obtainUserInfoModel.getAppId();
        String scope = obtainUserInfoModel.getScope();
        String redirectUri = obtainUserInfoModel.getRedirectUri();
        String state = obtainUserInfoModel.getState();

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("redirectUri", redirectUri);
        parameters.put("appId", appId);

        String outsideUrl = CommonUtils.getOutsideUrl(Constants.SERVICE_NAME_GATEWAY, "weiXin", "oauthCallback") + "?" + WebUtils.buildQueryString(parameters);
        String authorizeUrl = WeiXinUtils.generateAuthorizeUrl(appId, scope, outsideUrl, state);
        return "redirect:" + authorizeUrl;
    }

    @RequestMapping(value = "/oauthCallback", method = RequestMethod.GET)
    public String oauthCallback() throws IOException {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        String code = requestParameters.get("code");
        String redirectUri = requestParameters.get("redirectUri");
        String appId = requestParameters.get("appId");
        String state = requestParameters.get("state");

        WeiXinPublicAccount weiXinPublicAccount = weiXinService.obtainWeiXinPublicAccount(appId);
        ValidateUtils.notNull(weiXinPublicAccount, "微信公众号不存在！");

        WeiXinOAuthToken weiXinOAuthToken = WeiXinUtils.obtainOAuthToken(appId, weiXinPublicAccount.getAppSecret(), code);

        Map<String, String> parameters = new HashMap<String, String>();

        String openId = weiXinOAuthToken.getOpenId();
        parameters.put("openId", openId);

        String scope = weiXinOAuthToken.getScope();
        if (Constants.SNSAPI_BASE.equals(scope)) {

        } else if (Constants.SNSAPI_USERINFO.equals(scope)) {
            WeiXinUserInfo weiXinUserInfo = WeiXinUtils.obtainUserInfo(weiXinOAuthToken.getAccessToken(), openId, null);
            parameters.put("userInfo", GsonUtils.toJson(weiXinUserInfo));
        }

        if (StringUtils.isNotBlank(state)) {
            parameters.put("state", state);
        }

        StringBuilder url = new StringBuilder(redirectUri);
        if (redirectUri.indexOf("?") >= 0) {
            url.append("&");
        } else {
            url.append("?");
        }
        return "redirect:" + url.append(WebUtils.buildQueryString(parameters)).toString();
    }

    @RequestMapping(value = "/authCallback")
    @ResponseBody
    public String authCallback(HttpServletRequest httpServletRequest) throws IOException, DocumentException {
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
        String xmlContent = decrypt(encrypt, encodingAesKey);
        Map<String, String> encryptMap = XmlUtils.xmlStringToMap(xmlContent);

        ValidateUtils.isTrue(appId.equals(encryptMap.get("AppId")), "消息内容非法！");

        String componentVerifyTicket = encryptMap.get("ComponentVerifyTicket");
        CacheUtils.hset(Constants.KEY_WEI_XIN_COMPONENT_VERIFY_TICKET, appId, componentVerifyTicket);
        return Constants.SUCCESS;
    }

    private String decrypt(String data, String encodingAesKey) {
        byte[] encryptedData = Base64.decodeBase64(data);
        byte[] aesKey = Base64.decodeBase64(encodingAesKey);
        byte[] iv = Arrays.copyOfRange(aesKey, 0, 16);

        byte[] original = AESUtils.decrypt(encryptedData, aesKey, iv, AESUtils.ALGORITHM_AES_CBC_NOPADDING);
        byte[] bytes = original = decode(original);

        byte[] networkOrder = Arrays.copyOfRange(bytes, 16, 20);
        int xmlLength = recoverNetworkBytesOrder(networkOrder);

        String plaintext = new String(Arrays.copyOfRange(original, 20, 20 + xmlLength), Constants.CHARSET_UTF_8);
        return plaintext;
    }

    private byte[] decode(byte[] decrypted) {
        int pad = (int) decrypted[decrypted.length - 1];
        if (pad < 1 || pad > 32) {
            pad = 0;
        }
        return Arrays.copyOfRange(decrypted, 0, decrypted.length - pad);
    }

    private int recoverNetworkBytesOrder(byte[] orderBytes) {
        int sourceNumber = 0;
        for (int i = 0; i < 4; i++) {
            sourceNumber <<= 8;
            sourceNumber |= orderBytes[i] & 0xff;
        }
        return sourceNumber;
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

    @RequestMapping(value = "/callback")
    @ResponseBody
    public String callback() throws IOException {
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
        InputStream inputStream = httpServletRequest.getInputStream();
        String requestBody = IOUtils.toString(inputStream);

        WeiXinOpenPlatformApplication weiXinOpenPlatformApplication = weiXinService.obtainWeiXinOpenPlatformApplication(appId);
        if (weiXinOpenPlatformApplication == null) {
            return Constants.SUCCESS;
        }
        String encodingAesKey = weiXinOpenPlatformApplication.getEncodingAesKey();

        String message = decrypt(requestBody, encodingAesKey);
        Map<String, String> xmlMap = XmlUtils.xmlStringToMap(message);
        String msgType = xmlMap.get("MsgType");
        String event = xmlMap.get("Event");

        KafkaUtils.send(Constants.WEI_XIN_MESSAGE_TOPIC_ + "_" + msgType + "_" + event, UUID.randomUUID().toString(), GsonUtils.toJson(xmlMap));

        return Constants.SUCCESS;
    }

    @RequestMapping(value = "/coreReceive")
    @ResponseBody
    public String coreReceive(HttpServletRequest httpServletRequest) throws IOException, DocumentException {
        String method = httpServletRequest.getMethod();
        String returnValue = null;
        if ("GET".equals(method)) {
            Map<String, String> requestParameters = ApplicationHandler.getRequestParameters(httpServletRequest);
            String timestamp = requestParameters.get("timestamp");
            String nonce = requestParameters.get("nonce");
            String token = "DearFangXiang";

            String[] array = {token, timestamp, nonce};
            Arrays.sort(array);

            String signature = requestParameters.get("signature");
            ValidateUtils.isTrue(signature.equals(DigestUtils.sha1Hex(StringUtils.join(array, ""))), "签名验证未通过！");

            returnValue = requestParameters.get("echostr");
        } else {
            String requestBody = IOUtils.toString(httpServletRequest.getInputStream());
            Map<String, String> bodyMap = XmlUtils.xmlStringToMap(requestBody);
            String msgType = bodyMap.get("MsgType");
            String event = bodyMap.get("Event");
            if ("event".equals(msgType) && "subscribe".equals(event)) {
                Map<String, String> map = new HashMap<String, String>();
                String openId = bodyMap.get("FromUserName");
                String originalId = bodyMap.get("ToUserName");
                map.put("ToUserName", openId);
                map.put("FromUserName", originalId);
                map.put("CreateTime", String.valueOf(System.currentTimeMillis()));
                map.put("MsgType", "text");
                map.put("Content", UUID.randomUUID().toString());
                map.put("MsgId", String.valueOf(RandomUtils.nextLong()));
                returnValue = mapToXml(map);

                new Thread(() -> {
                    ThreadUtils.sleepSafe(2000);

                    Map<String, Object> messageBody = new HashMap<String, Object>();
                    messageBody.put("touser", openId);
                    messageBody.put("msgtype", "wxcard");

                    Map<String, Object> wxcard = new HashMap<String, Object>();
                    wxcard.put("card_id", "pmR87t_G_4SU5tehB5ZZsaFA7Fe8");
                    messageBody.put("wxcard", wxcard);

                    WeiXinUtils.sendCustomMessage("wx6bb9ea76a4242455", "dbceac55f21809dc0f7cbbac99c4eca6", GsonUtils.toJson(messageBody));
                }).start();
            } else {
                returnValue = Constants.SUCCESS;
            }
        }
        return returnValue;
    }

    public String mapToXml(Map<String, String> map) {
        StringBuilder xml = new StringBuilder("<xml>");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            xml.append("<").append(key).append(">");
            xml.append(String.format(Constants.CDATA_FORMAT, entry.getValue()));
            xml.append("</").append(key).append(">");
        }
        xml.append("</xml>");
        return xml.toString();
    }
}
