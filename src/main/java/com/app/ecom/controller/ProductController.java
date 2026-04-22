package com.app.ecom.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.ecom.dto.ProductRequest;
import com.app.ecom.dto.ProductResponse;
import com.app.ecom.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/products")
public class ProductController {
	
	private final ProductService productService;
	
	@PostMapping
	public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest productRequest){
		return new ResponseEntity<ProductResponse>(productService.createProduct(productRequest),
				HttpStatus.CREATED);
		
	}
	
	@GetMapping
	public ResponseEntity<List<ProductResponse>> getProducts(){
		return ResponseEntity.ok(productService.getAllProducts());
		
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<ProductResponse> updateProduct(
			@PathVariable("id") Long id,
			@RequestBody ProductRequest productResquest){
		
		return productService.updateProduct(id, productResquest)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
		
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteProduct(
			@PathVariable("id") Long id){
		
		boolean isDeleted = productService.deleteProduct(id);	
		
		return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
		
	}
	
	@GetMapping("/search")
	public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam("keyWord") String keyWord){
		return ResponseEntity.ok(productService.searchProducts(keyWord));
		
	}

}
