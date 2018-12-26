package build.dream.gateway.controllers;

import build.dream.common.controllers.BasicController;
import build.dream.common.utils.ApplicationHandler;
import build.dream.common.utils.LogUtils;
import build.dream.gateway.constants.Constants;
import build.dream.gateway.services.MeiTuanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping(value = "/meiTuan")
public class MeiTuanController extends BasicController {
    @Autowired
    private MeiTuanService meiTuanService;

    /**
     * 订单生效回调
     *
     * @return
     */
    @RequestMapping(value = "/orderEffectiveCallback")
    @ResponseBody
    public String orderEffectiveCallback() {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        String handleResult = null;
        try {
            handleResult = meiTuanService.handleCallback(requestParameters, Constants.MEI_TUAN_CALLBACK_TYPE_ORDER_EFFECTIVE);
        } catch (Exception e) {
            LogUtils.error("美团订单生效回调处理失败", className, "orderEffectiveCallback", e, requestParameters);
            handleResult = e.getMessage();
        }
        return handleResult;
    }

    /**
     * 订单取消回调
     *
     * @return
     */
    @RequestMapping(value = "/orderCancelCallback")
    @ResponseBody
    public String orderCancelCallback() {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        String handleResult = null;
        try {
            handleResult = meiTuanService.handleCallback(requestParameters, Constants.MEI_TUAN_CALLBACK_TYPE_ORDER_CANCEL);
        } catch (Exception e) {
            LogUtils.error("美团订单取消回调处理失败", className, "orderCancelCallback", e, requestParameters);
            handleResult = e.getMessage();
        }
        return handleResult;
    }

    /**
     * 订单退款回调
     *
     * @return
     */
    @RequestMapping(value = "/orderRefundCallback")
    @ResponseBody
    public String orderRefundCallback() {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        String handleResult = null;
        try {
            handleResult = meiTuanService.handleCallback(requestParameters, Constants.MEI_TUAN_CALLBACK_TYPE_ORDER_REFUND);
        } catch (Exception e) {
            LogUtils.error("美团订单退款回调处理失败", className, "orderRefundCallback", e, requestParameters);
            handleResult = e.getMessage();
        }
        return handleResult;
    }

    /**
     * 确认订单回调
     *
     * @return
     */
    @RequestMapping(value = "/orderConfirmCallback")
    @ResponseBody
    public String orderConfirmCallback() {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        String handleResult = null;
        try {
            handleResult = meiTuanService.handleCallback(requestParameters, Constants.MEI_TUAN_CALLBACK_TYPE_ORDER_CONFIRM);
        } catch (Exception e) {
            LogUtils.error("美团订单确认回调处理失败", className, "orderConfirmCallback", e, requestParameters);
            handleResult = e.getMessage();
        }
        return handleResult;
    }

    /**
     * 订单完成回调
     *
     * @return
     */
    @RequestMapping(value = "/orderSettledCallback")
    @ResponseBody
    public String orderSettledCallback() {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        String handleResult = null;
        try {
            handleResult = meiTuanService.handleCallback(requestParameters, Constants.MEI_TUAN_CALLBACK_TYPE_ORDER_SETTLED);
        } catch (Exception e) {
            LogUtils.error("美团订单完成回调处理失败", className, "orderSettledCallback", e, requestParameters);
            handleResult = e.getMessage();
        }
        return handleResult;
    }

    /**
     * 订单配送状态回调
     *
     * @return
     */
    @RequestMapping(value = "/orderShippingStatusCallback")
    @ResponseBody
    public String orderShippingStatusCallback() {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        String handleResult = null;
        try {
            handleResult = meiTuanService.handleCallback(requestParameters, Constants.MEI_TUAN_CALLBACK_TYPE_ORDER_SHIPPING_STATUS);
        } catch (Exception e) {
            LogUtils.error("美团订单配送状态回调处理失败", className, "orderShippingStatusCallback", e, requestParameters);
            handleResult = e.getMessage();
        }
        return handleResult;
    }

    /**
     * 门店状态变更回调
     *
     * @return
     */
    @RequestMapping(value = "/poiStatusCallback")
    @ResponseBody
    public String poiStatusCallback() {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        String handleResult = null;
        try {
            handleResult = meiTuanService.handleCallback(requestParameters, Constants.MEI_TUAN_CALLBACK_TYPE_POI_STATUS);
        } catch (Exception e) {
            LogUtils.error("美团订单配送状态回调处理失败", className, "poiStatusCallback", e, requestParameters);
            handleResult = e.getMessage();
        }
        return handleResult;
    }

    /**
     * 门店绑定回调
     *
     * @return
     */
    @RequestMapping(value = "/bindingStoreCallback")
    @ResponseBody
    public String bindingStoreCallback() {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        String handleResult = null;
        try {
            handleResult = meiTuanService.handleCallback(requestParameters, Constants.MEI_TUAN_CALLBACK_TYPE_BINDING_STORE);
        } catch (Exception e) {
            LogUtils.error("美团门店绑定回调处理失败", className, "bindingStoreCallback", e, requestParameters);
            handleResult = e.getMessage();
        }
        return handleResult;
    }
}
