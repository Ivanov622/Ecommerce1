package com.app.ecom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.ecom.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	List<Product> findByActiveTrue();

	@Query("SELECT p FROM products p where p.active = true AND p.stockQuantity > 0 AND LOWER(p.name) like LOWER(CONCAT('%', :keyWord, '%'))")
	List<Product> searchProducts(@Param("keyWord") String keyWord);

}
