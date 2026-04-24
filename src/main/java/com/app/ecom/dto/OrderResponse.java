package com.app.ecom.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.app.ecom.model.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderResponse {
	
	private Long id;
	private BigDecimal totalAmount;
	private OrderStatus orderStatus;
	private List<OrderItemDTO> orderItems;
	private LocalDateTime createdAt;

}
