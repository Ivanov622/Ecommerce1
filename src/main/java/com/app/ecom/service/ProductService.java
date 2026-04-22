package com.app.ecom.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.app.ecom.model.Product;
import com.app.ecom.dto.ProductRequest;
import com.app.ecom.dto.ProductResponse;
import com.app.ecom.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
	
	private final ProductRepository productRepository;

	public ProductResponse createProduct(ProductRequest productResquest) {
		Product product = new Product();
		updateProductFromRequest(product, productResquest);
		return mapToProductResponse(productRepository.save(product));
	}
	
	private ProductResponse mapToProductResponse(Product savedProduct) {
		ProductResponse productResponse = new ProductResponse();
		productResponse.setId(savedProduct.getId());
		productResponse.setName(savedProduct.getName());
		productResponse.setDescription(savedProduct.getDescription());
		productResponse.setCategory(savedProduct.getCategory());
		productResponse.setPrice(savedProduct.getPrice());
		productResponse.setStockQuantity(savedProduct.getStockQuantity());
		productResponse.setImageUrl(savedProduct.getImageUrl());
		productResponse.setActive(savedProduct.getActive());

		return productResponse;
	}

	public Product updateProductFromRequest(Product product, ProductRequest productResquest) {
		
		product.setName(productResquest.getName());
		product.setDescription(productResquest.getDescription());
		product.setPrice(productResquest.getPrice());
		product.setCategory(productResquest.getCategory());
		product.setStockQuantity(productResquest.getStockQuantity());
		product.setImageUrl(productResquest.getImageUrl());
		
		return product;
		
	}

	public Optional<ProductResponse> updateProduct(Long id, ProductRequest productResquest) {
		
		return productRepository.findById(id)
			.map(existingProduct -> {
				updateProductFromRequest(existingProduct, productResquest);
				Product savedProduct = productRepository.save(existingProduct);
				return mapToProductResponse(savedProduct);
			});
	}

	public List<ProductResponse> getAllProducts() {

		//jpa entiende el findBy, luego detecta el Active es un campo de product y True es valor
		// por eso en el repository no hay que hacer, únicamente nombrar el método
		return productRepository.findByActiveTrue().stream()
				.map(this::mapToProductResponse)
				.collect(Collectors.toList());
	}

	public boolean deleteProduct(Long id) {
		
		return productRepository.findById(id)
				.map(productToDelete -> {
					productToDelete.setActive(false);
					productRepository.save(productToDelete);
					return true;
				}).orElse(false);	
	}

	public List<ProductResponse> searchProducts(String keyWord) {
		
		return productRepository.searchProducts(keyWord).stream()
				.map(this::mapToProductResponse)
				.collect(Collectors.toList());
	}

}
