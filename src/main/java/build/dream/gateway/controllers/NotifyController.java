package build.dream.gateway.controllers;

import build.dream.common.controllers.BasicController;
import build.dream.common.utils.ApplicationHandler;
import build.dream.common.utils.LogUtils;
import build.dream.common.utils.XmlUtils;
import build.dream.gateway.constants.Constants;
import build.dream.gateway.services.NotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping(value = "/notify")
public class NotifyController extends BasicController {
    @Autowired
    private NotifyService notifyService;

    private String handleAlipayCallback(String uuidKey) {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        try {
            notifyService.handleAlipayCallback(requestParameters, uuidKey);
            return Constants.SUCCESS;
        } catch (Exception e) {
            LogUtils.error("支付宝回调处理失败", className, "alipayCallback", e, requestParameters);
            return Constants.FAILURE;
        }
    }

    /**
     * 支付宝统一收单交易关闭回调
     *
     * @return
     */
    @RequestMapping(value = "/alipayTradeCloseCallback")
    @ResponseBody
    public String alipayTradeCloseCallback() {
        return handleAlipayCallback("");
    }

    /**
     * 支付宝统一收单线下交易预创建回调
     *
     * @return
     */
    @RequestMapping(value = "/alipayTradePreCreateCallback")
    @ResponseBody
    public String alipayCallback() {
        return handleAlipayCallback("");
    }

    /**
     * 支付宝统一收单线下交易预创建回调
     *
     * @return
     */
    @RequestMapping(value = "/alipayTradeCreateCallback")
    @ResponseBody
    public String alipayTradeCreateCallback() {
        return handleAlipayCallback("");
    }

    /**
     * 支付宝统一收单交易支付接口回调
     *
     * @return
     */
    @RequestMapping(value = "/alipayTradePayCallback")
    @ResponseBody
    public String alipayTradePayCallback() {
        return handleAlipayCallback("out_trade_no");
    }

    /**
     * 口碑商品交易购买接口回调
     *
     * @return
     */
    @RequestMapping(value = "/koubeiTradeItemOrderBuyCallback")
    @ResponseBody
    public String koubeiTradeItemOrderBuyCallback() {
        return handleAlipayCallback("");
    }

    /**
     * 口碑凭证码撤销核销回调
     *
     * @return
     */
    @RequestMapping(value = "/koubeiTradeTicketTicketCodeCancelCallback")
    @ResponseBody
    public String koubeiTradeTicketTicketCodeCancelCallback() {
        return handleAlipayCallback("");
    }

    /**
     * 支付宝统一收单下单并支付页面接口回调
     *
     * @return
     */
    @RequestMapping(value = "/alipayTradePagePayCallback")
    @ResponseBody
    public String alipayTradePagePayCallback() {
        return handleAlipayCallback("");
    }

    /**
     * 支付宝手机网站支付接口2.0回调
     *
     * @return
     */
    @RequestMapping(value = "/alipayTradeWapPayCallback")
    @ResponseBody
    public String alipayTradeWapPayCallback() {
        return handleAlipayCallback("");
    }

    /**
     * 支付宝app支付接口2.0回调
     *
     * @return
     */
    @RequestMapping(value = "/alipayTradeAppPayCallback")
    @ResponseBody
    public String alipayTradeAppPayCallback() {
        return handleAlipayCallback("");
    }

    /**
     * 资金授权冻结接口回调
     *
     * @return
     */
    @RequestMapping(value = "/alipayFundAuthOrderFreezeCallback")
    @ResponseBody
    public String alipayFundAuthOrderFreezeCallback() {
        return handleAlipayCallback("");
    }

    /**
     * 修改门店信息回调
     *
     * @return
     */
    @RequestMapping(value = "/alipayOfflineMarketShopModifyCallback")
    @ResponseBody
    public String alipayOfflineMarketShopModifyCallback() {
        return handleAlipayCallback("");
    }

    /**
     * 创建门店信息回调
     *
     * @return
     */
    @RequestMapping(value = "/alipayOfflineMarketShopCreateCallback")
    @ResponseBody
    public String alipayOfflineMarketShopCreateCallback() {
        return handleAlipayCallback("");
    }

    /**
     * 服务市场商户确认订购通知回调
     *
     * @return
     */
    @RequestMapping(value = "/alipayOpenServiceMarketOrderNotifyCallback")
    @ResponseBody
    public String alipayOpenServiceMarketOrderNotifyCallback() {
        return handleAlipayCallback("");
    }

    /**
     * 微信支付回调
     *
     * @return
     */
    @RequestMapping(value = "/weiXinPayCallback")
    @ResponseBody
    public String weiXinPayCallback() {
        Map<String, String> requestParameters = null;
        try {
            requestParameters = XmlUtils.xmlStringToMap(ApplicationHandler.getRequestBody(Constants.CHARSET_NAME_UTF_8));
            notifyService.handleWeiXinPayCallback(requestParameters);
            return Constants.WEI_XIN_PAY_CALLBACK_SUCCESS_RETURN_VALUE;
        } catch (Exception e) {
            LogUtils.error("微信支付回调处理失败", className, "weiPayXinCallback", e, requestParameters);
            return Constants.WEI_XIN_PAY_CALLBACK_FAILURE_RETURN_VALUE;
        }
    }

    /**
     * 微信退款回调
     *
     * @return
     */
    @RequestMapping(value = "/weiXinRefundCallback")
    @ResponseBody
    public String weiXinRefundCallback() {
        Map<String, String> requestParameters = null;
        try {
            requestParameters = XmlUtils.xmlStringToMap(ApplicationHandler.getRequestBody(Constants.CHARSET_NAME_UTF_8));
            notifyService.handleXinRefundCallback(requestParameters);
            return Constants.WEI_XIN_PAY_CALLBACK_SUCCESS_RETURN_VALUE;
        } catch (Exception e) {
            LogUtils.error("微信支付回调处理失败", className, "weiPayXinCallback", e, requestParameters);
            return Constants.WEI_XIN_PAY_CALLBACK_FAILURE_RETURN_VALUE;
        }
    }
}
