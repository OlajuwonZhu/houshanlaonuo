package com.senol.repository;

import com.senol.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    // 根据订单ID获取订单项
    List<OrderItem> findByOrderIdOrderByCreatedAtAsc(Long orderId);
}
