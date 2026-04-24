package com.app.ecom.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderItemDTO {
	
	private Long id;
	private Long productId;
	private Integer quantity;
	private BigDecimal price;	
	//ventaja del dto, subtotal es calculado y no está en bd, pero lo podemos devolver
	private BigDecimal subTotal;	

}
