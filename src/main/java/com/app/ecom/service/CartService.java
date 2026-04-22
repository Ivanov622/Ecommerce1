package com.app.ecom.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.ecom.dto.CartItemRequest;
import com.app.ecom.model.Product;
import com.app.ecom.model.User;
import com.app.ecom.model.CartItem;
import com.app.ecom.repository.CartItemRepository;
import com.app.ecom.repository.ProductRepository;
import com.app.ecom.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional 
public class CartService {
	
	private final ProductRepository productRepository;
	private final CartItemRepository cartItemRepository;
	private final UserRepository userRepository;

	public boolean addToCart(String userId, CartItemRequest cartRequest) {	
		Optional<Product> productOpt = productRepository.findById(cartRequest.getProductId());
		
		if(productOpt.isEmpty())
			return false;
		
		Product product = productOpt.get();
		if(product.getStockQuantity() < cartRequest.getQuantity())
			return false;
		
		Optional<User> userOpt = userRepository.findById(Long.valueOf(userId));
		if(userOpt.isEmpty())
			return false;
		
		User user = userOpt.get();
		
		CartItem existingCartItem = cartItemRepository.findByUserAndProduct(user,product);
		if(existingCartItem !=null) {
			// ya había productos en el carrito y adicionó más de ese producto
			existingCartItem.setQuantity(existingCartItem.getQuantity() + cartRequest.getQuantity());
			existingCartItem.setPrice(product.getPrice().multiply(BigDecimal.valueOf(existingCartItem.getQuantity())));
			cartItemRepository.save(existingCartItem);
		}
		else {
			// crear un nuevo elemento al carrito
			CartItem cartItem = new CartItem();
			cartItem.setUser(user);
			cartItem.setProduct(product);
			cartItem.setQuantity(cartRequest.getQuantity());
			cartItem.setPrice(product.getPrice().multiply(BigDecimal.valueOf(cartRequest.getQuantity())));
			cartItemRepository.save(cartItem);			
		}		
		return true;		
	}

	public boolean deleteItemFromCart(String userId, Long productId) {
		
		Optional<Product> productOpt = productRepository.findById(productId);
		Optional<User> userOpt = userRepository.findById(Long.valueOf(userId));
		
		if(productOpt.isPresent() && userOpt.isPresent()) {
			cartItemRepository.deleteByUserAndProduct(userOpt.get(),productOpt.get());
			return true;
		}			
		return false;		
	}

	public List<CartItem> getCart(String userId) {
		return userRepository.findById(Long.valueOf(userId))
				.map(cartItemRepository::findByUser)
				.orElseGet(List::of);
	}	

}
