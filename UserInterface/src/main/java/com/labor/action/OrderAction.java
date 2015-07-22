package com.labor.action;

import com.labor.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**订单操作action
 * Created by wyp on 15-7-22.
 */
@RestController
@RequestMapping("/orderInfo")
public class OrderAction {

    @Autowired
    private IOrderService orderService;





}
