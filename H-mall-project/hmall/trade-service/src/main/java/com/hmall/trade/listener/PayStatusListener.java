package com.hmall.trade.listener;

import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PayStatusListener {

    @Autowired
    private IOrderService orderService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "trade.pay.success.queue", durable = "true"),
            exchange = @Exchange(value = "pay.direct"),
            key = "pay.success"
    ))
    public void ListenPaySuccess(Long orderId) {
        // 实现业务幂等性
        // 1. 查询订单
        Order order = orderService.getById(orderId);
        // 2. 判断订单是否支付成功
        if (order == null || order.getStatus() != 1) {
            // 未支付则不做处理
            return;
        }
        orderService.markOrderPaySuccess(orderId);
    }
}
