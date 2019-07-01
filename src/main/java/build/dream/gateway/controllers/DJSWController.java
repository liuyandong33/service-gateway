package build.dream.gateway.controllers;

import build.dream.common.utils.ApplicationHandler;
import build.dream.gateway.constants.Constants;
import build.dream.gateway.services.DJSWService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/djsw")
public class DJSWController {
    @Autowired
    private DJSWService djswService;

    /**
     * 创建新订单消息
     *
     * @return
     */
    @RequestMapping(value = "/newOrder")
    @ResponseBody
    public String newOrder() {
        return djswService.handleCallback(Constants.DJSW_TYPE_NEW_ORDER, ApplicationHandler.getRequestParameters());
    }

    /**
     * 订单调整消息
     *
     * @return
     */
    @RequestMapping(value = "/orderAdjust")
    @ResponseBody
    public String orderAdjust() {
        return djswService.handleCallback(Constants.DJSW_TYPE_ORDER_ADJUST, ApplicationHandler.getRequestParameters());
    }

    /**
     * 用户取消申请消息
     *
     * @return
     */
    @RequestMapping(value = "/applyCancelOrder")
    @ResponseBody
    public String applyCancelOrder() {
        return djswService.handleCallback(Constants.DJSW_TYPE_APPLY_CANCELORDER, ApplicationHandler.getRequestParameters());
    }

    /**
     * 用户取消申请消息
     *
     * @return
     */
    @RequestMapping(value = "/orderWaitOutStore")
    @ResponseBody
    public String orderWaitOutStore() {
        return djswService.handleCallback(Constants.DJSW_TYPE_ORDER_WAIT_OUT_STORE, ApplicationHandler.getRequestParameters());
    }

    /**
     * 订单开始配送消息
     *
     * @return
     */
    @RequestMapping(value = "/deliveryOrder")
    @ResponseBody
    public String deliveryOrder() {
        return djswService.handleCallback(Constants.DJSW_TYPE_DELIVERY_ORDER, ApplicationHandler.getRequestParameters());
    }

    /**
     * 订单拣货完成消息
     *
     * @return
     */
    @RequestMapping(value = "/pickFinishOrder")
    @ResponseBody
    public String pickFinishOrder() {
        return djswService.handleCallback(Constants.DJSW_TYPE_PICK_FINISH_ORDER, ApplicationHandler.getRequestParameters());
    }

    /**
     * 订单妥投消息
     *
     * @return
     */
    @RequestMapping(value = "/finishOrder")
    @ResponseBody
    public String finishOrder() {
        return djswService.handleCallback(Constants.DJSW_TYPE_FINISH_ORDER, ApplicationHandler.getRequestParameters());
    }

    /**
     * 订单锁定消息
     *
     * @return
     */
    @RequestMapping(value = "/lockOrder")
    @ResponseBody
    public String lockOrder() {
        return djswService.handleCallback(Constants.DJSW_TYPE_LOCK_ORDER, ApplicationHandler.getRequestParameters());
    }

    /**
     * 订单解锁消息
     *
     * @return
     */
    @RequestMapping(value = "/unlockOrder")
    @ResponseBody
    public String unlockOrder() {
        return djswService.handleCallback(Constants.DJSW_TYPE_UNLOCK_ORDER, ApplicationHandler.getRequestParameters());
    }

    /**
     * 用户取消消息
     *
     * @return
     */
    @RequestMapping(value = "/userCancelOrder")
    @ResponseBody
    public String userCancelOrder() {
        return djswService.handleCallback(Constants.DJSW_TYPE_USER_CANCEL_ORDER, ApplicationHandler.getRequestParameters());
    }

    /**
     * 订单运单状态消息
     *
     * @return
     */
    @RequestMapping(value = "/pushDeliveryStatus")
    @ResponseBody
    public String pushDeliveryStatus() {
        return djswService.handleCallback(Constants.DJSW_TYPE_PUSH_DELIVERY_STATUS, ApplicationHandler.getRequestParameters());
    }

    /**
     * 订单信息变更消息
     *
     * @return
     */
    @RequestMapping(value = "/orderInfoChange")
    @ResponseBody
    public String orderInfoChange() {
        return djswService.handleCallback(Constants.DJSW_TYPE_ORDER_INFO_CHANGE, ApplicationHandler.getRequestParameters());
    }

    /**
     * 订单商家小费消息
     *
     * @return
     */
    @RequestMapping(value = "/orderAddTips")
    @ResponseBody
    public String orderAddTips() {
        return djswService.handleCallback(Constants.DJSW_TYPE_ORDER_ADD_TIPS, ApplicationHandler.getRequestParameters());
    }

    /**
     * 订单应结消息
     *
     * @return
     */
    @RequestMapping(value = "/orderAccounting")
    @ResponseBody
    public String orderAccounting() {
        return djswService.handleCallback(Constants.DJSW_TYPE_ORDER_ACCOUNTING, ApplicationHandler.getRequestParameters());
    }

    /**
     * 订单转自送消息
     *
     * @return
     */
    @RequestMapping(value = "/deliveryCarrierModify")
    @ResponseBody
    public String deliveryCarrierModify() {
        return djswService.handleCallback(Constants.DJSW_TYPE_DELIVERY_CARRIER_MODIFY, ApplicationHandler.getRequestParameters());
    }
}
