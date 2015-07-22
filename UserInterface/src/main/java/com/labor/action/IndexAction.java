package com.labor.action;

import com.labor.common.ReturnStatus;
import com.labor.order.OrderStatus;
import com.labor.response.RetResponse;
import com.labor.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**首页action
 * 获取首页相关信息数据
 * Created by wyp on 15-7-22.
 */
@RestController
@RequestMapping("/index")
public class IndexAction {

    @Autowired
    private IOrderService orderService;


    @RequestMapping(value = "getData", method = RequestMethod.GET)
    @ResponseBody
    public RetResponse getIndexData() {
        RetResponse retResponse = new RetResponse();
        //1.获取订单数据
        Map<String, Integer> orderData = new HashMap<String, Integer>();
        //未处理
        orderData.put("UNCONFIRMED", orderService.getOrderNumByStatus(OrderStatus.UNCONFIRMED.getStatus()));
        //待发货
        orderData.put("CONFIRMED", orderService.getOrderNumByStatus(OrderStatus.CONFIRMED.getStatus()));
        //待结算
        orderData.put("UNCONFIRMED", orderService.getOrderNumByStatus(OrderStatus.UNCONFIRMED.getStatus()));
        //已成交
        orderData.put("FINISHED", orderService.getOrderNumByStatus(OrderStatus.FINISHED.getStatus()));
        retResponse.setStatus(ReturnStatus.SUCCESS);
        retResponse.setData(orderData);
        return retResponse;
    }

}
