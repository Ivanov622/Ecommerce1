package com.app.ecom.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.app.ecom.dto.OrderItemDTO;
import com.app.ecom.dto.OrderResponse;
import com.app.ecom.model.CartItem;
import com.app.ecom.model.User;
import com.app.ecom.model.Order;
import com.app.ecom.model.OrderItem;
import com.app.ecom.model.OrderStatus;
import com.app.ecom.repository.OrderRepository;
import com.app.ecom.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
	
	private final OrderRepository orderRepository;
	//no se usa CartItemRepository.getCart porque no existe, se manejó en el service con streams
	//private final CartItemRepository cartItemRepository;
	private final CartService cartService;
	private final UserRepository userRepository;

	public Optional<OrderResponse> createOrder(String userId) {
		//validate caritems
		List<CartItem> cartItems = cartService.getCart(userId);
		if(cartItems.isEmpty()) {
			return Optional.empty();			
		}		
		
		//validate user
		Optional<User> userOptional = userRepository.findById(Long.valueOf(userId));
		if(userOptional.isEmpty()) {
			return Optional.empty();			
		}
		User user = userOptional.get();		
		
		//calcular total price, sacamos todo lo que está en el carrito y reducimos a un valor total
		BigDecimal totalPrice = cartItems.stream()
								.map(CartItem::getPrice)
								.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		//crear orden
		
		Order order = new Order();
		order.setUser(user);
		order.setOrderStatus(OrderStatus.CONFIRMED);
		order.setTotalAmount(totalPrice);
		
		List<OrderItem> orderItems = cartItems.stream()
									.map(item -> new OrderItem(null, item.getProduct(), 
												item.getPrice(),item.getQuantity(), order))
									.collect(Collectors.toList());
		order.setOrderItem(orderItems);
		
		Order savedOrder = orderRepository.save(order);
		
		//clear cart
		cartService.clearCart(userId);
		
		return Optional.of(mapToOrdenReponse(savedOrder));

	}

	public OrderResponse mapToOrdenReponse(Order order) {
		
		return new OrderResponse(
				order.getId(), 
				order.getTotalAmount(), 
				order.getOrderStatus(),
				order.getOrderItem().stream().map(orderItem -> new OrderItemDTO(
									orderItem.getId(), 
									orderItem.getProduct().getId(), 
									orderItem.getQuantity(), 
									orderItem.getPrice(),
									orderItem.getPrice().multiply(new BigDecimal(orderItem.getQuantity()))
									)).toList(), 
				order.getCretedAt());
	}

}
