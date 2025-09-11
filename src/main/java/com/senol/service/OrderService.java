package com.senol.service;

import com.senol.entity.Order;
import com.senol.entity.OrderItem;
import com.senol.entity.Product;
import com.senol.entity.User;
import com.senol.repository.OrderRepository;
import com.senol.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    // 获取用户订单列表
    public Page<Order> getUserOrders(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    // 根据ID获取订单
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    // 根据订单号获取订单
    public Order getOrderByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo);
    }

    // 获取订单项列表
    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
    }

    // 创建订单
    @Transactional
    public Order createOrder(Long userId, List<OrderItemRequest> items, String shippingAddress, 
                           String shippingPhone, String shippingName, String remark) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 生成订单号
        String orderNo = generateOrderNo();
        
        // 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        Order order = new Order(orderNo, user, totalAmount);
        order.setShippingAddress(shippingAddress);
        order.setShippingPhone(shippingPhone);
        order.setShippingName(shippingName);
        order.setRemark(remark);
        
        Order savedOrder = orderRepository.save(order);

        // 创建订单项并计算总金额
        for (OrderItemRequest itemRequest : items) {
            Product product = productService.getProductById(itemRequest.getProductId());
            if (product == null) {
                throw new RuntimeException("商品不存在: " + itemRequest.getProductId());
            }

            // 检查库存
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("商品库存不足: " + product.getName());
            }

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setProductPrice(product.getPrice());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setSubtotal(subtotal);
            orderItem.setSpecifications(itemRequest.getSpecifications());
            orderItemRepository.save(orderItem);

            // 减少库存
            productService.decreaseStock(product.getId(), itemRequest.getQuantity());
        }

        // 更新订单总金额
        savedOrder.setTotalAmount(totalAmount);
        return orderRepository.save(savedOrder);
    }

    // 更新订单状态
    @Transactional
    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        order.setStatus(status);
        
        switch (status) {
            case PAID:
                order.setPaidAt(LocalDateTime.now());
                // 增加销量
                List<OrderItem> items = getOrderItems(orderId);
                for (OrderItem item : items) {
                    productService.increaseSales(item.getProduct().getId(), item.getQuantity());
                }
                break;
            case SHIPPED:
                order.setShippedAt(LocalDateTime.now());
                break;
            case COMPLETED:
                order.setCompletedAt(LocalDateTime.now());
                break;
        }

        return orderRepository.save(order);
    }

    // 生成订单号
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.valueOf((int)(Math.random() * 1000));
        return "SN" + timestamp + String.format("%03d", Integer.parseInt(random));
    }

    // 订单项请求DTO
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
        private String specifications;

        public OrderItemRequest() {}

        public OrderItemRequest(Long productId, Integer quantity, String specifications) {
            this.productId = productId;
            this.quantity = quantity;
            this.specifications = specifications;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public String getSpecifications() {
            return specifications;
        }

        public void setSpecifications(String specifications) {
            this.specifications = specifications;
        }
    }
}
