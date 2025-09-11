package com.senol.controller;

import com.senol.entity.Order;
import com.senol.entity.OrderItem;
import com.senol.service.OrderService;
import com.senol.service.UserService;
import com.senol.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    // 获取用户订单列表
    @GetMapping("/user")
    public ResponseUtil<Page<Order>> getUserOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            // 使用默认系统用户ID=1
            Long userId = 1L;
            
            Page<Order> orders = orderService.getUserOrders(userId, page, size);
            return ResponseUtil.success(orders);
        } catch (Exception e) {
            return ResponseUtil.error("获取订单列表失败: " + e.getMessage());
        }
    }

    // 根据ID获取订单详情
    @GetMapping("/{id}")
    public ResponseUtil<Order> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        if (order == null) {
            return ResponseUtil.error("订单不存在");
        }
        return ResponseUtil.success(order);
    }

    // 获取订单项列表
    @GetMapping("/{id}/items")
    public ResponseUtil<List<OrderItem>> getOrderItems(@PathVariable Long id) {
        List<OrderItem> items = orderService.getOrderItems(id);
        return ResponseUtil.success(items);
    }

    // 创建订单
    @PostMapping
    public ResponseUtil<Order> createOrder(@RequestBody OrderRequest request) {
        try {
            // 使用默认系统用户ID=1
            Long userId = 1L;
            
            Order order = orderService.createOrder(
                userId,
                request.getItems(),
                request.getShippingAddress(),
                request.getShippingPhone(),
                request.getShippingName(),
                request.getRemark()
            );
            return ResponseUtil.success(order);
        } catch (Exception e) {
            return ResponseUtil.error("创建订单失败: " + e.getMessage());
        }
    }

    // 更新订单状态
    @PutMapping("/{id}/status")
    public ResponseUtil<Order> updateOrderStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        try {
            Order order = orderService.updateOrderStatus(id, request.getStatus());
            return ResponseUtil.success(order);
        } catch (Exception e) {
            return ResponseUtil.error("更新订单状态失败: " + e.getMessage());
        }
    }

    // 订单请求DTO
    public static class OrderRequest {
        private List<OrderService.OrderItemRequest> items;
        private String shippingAddress;
        private String shippingPhone;
        private String shippingName;
        private String remark;

        public OrderRequest() {}

        public List<OrderService.OrderItemRequest> getItems() {
            return items;
        }

        public void setItems(List<OrderService.OrderItemRequest> items) {
            this.items = items;
        }

        public String getShippingAddress() {
            return shippingAddress;
        }

        public void setShippingAddress(String shippingAddress) {
            this.shippingAddress = shippingAddress;
        }

        public String getShippingPhone() {
            return shippingPhone;
        }

        public void setShippingPhone(String shippingPhone) {
            this.shippingPhone = shippingPhone;
        }

        public String getShippingName() {
            return shippingName;
        }

        public void setShippingName(String shippingName) {
            this.shippingName = shippingName;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }

    // 状态更新请求DTO
    public static class StatusRequest {
        private Order.OrderStatus status;

        public StatusRequest() {}

        public Order.OrderStatus getStatus() {
            return status;
        }

        public void setStatus(Order.OrderStatus status) {
            this.status = status;
        }
    }
}
