package com.app.ecom.service;

import com.app.ecom.dto.ProductRequest;
import com.app.ecom.dto.ProductResponse;
import com.app.ecom.model.Product;
import com.app.ecom.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias - ProductService")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setDescription("Laptop gamer");
        product.setPrice(new BigDecimal("2500000"));
        product.setStockQuantity(10);
        product.setCategory("Electrónica");
        product.setImageUrl("http://imagen.com/laptop.jpg");
        product.setActive(true);

        productRequest = new ProductRequest();
        productRequest.setName("Laptop");
        productRequest.setDescription("Laptop gamer");
        productRequest.setPrice(new BigDecimal("2500000"));
        productRequest.setStockQuantity(10);
        productRequest.setCategory("Electrónica");
        productRequest.setImageUrl("http://imagen.com/laptop.jpg");
    }

    // ─── createProduct ────────────────────────────────────────────────────────

    @Test
    @DisplayName("createProduct: debe guardar el producto y retornar ProductResponse correctamente")
    void createProduct_debeGuardarYRetornarProductResponse() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.createProduct(productRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Laptop");
        assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("2500000"));
        assertThat(response.getActive()).isTrue();
        verify(productRepository, times(1)).save(any(Product.class));
    }

    // ─── updateProduct ────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateProduct: debe actualizar el producto cuando existe y retornar Optional con el response")
    void updateProduct_cuandoExiste_debeActualizarYRetornarResponse() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Optional<ProductResponse> result = productService.updateProduct(1L, productRequest);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Laptop");
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("updateProduct: debe retornar Optional vacío cuando el producto no existe")
    void updateProduct_cuandoNoExiste_debeRetornarOptionalVacio() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<ProductResponse> result = productService.updateProduct(99L, productRequest);

        assertThat(result).isEmpty();
        verify(productRepository, never()).save(any());
    }

    // ─── getAllProducts ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllProducts: debe retornar solo productos activos")
    void getAllProducts_debeRetornarSoloProductosActivos() {
        when(productRepository.findByActiveTrue()).thenReturn(List.of(product));

        List<ProductResponse> result = productService.getAllProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getActive()).isTrue();
        verify(productRepository).findByActiveTrue();
    }

    @Test
    @DisplayName("getAllProducts: debe retornar lista vacía cuando no hay productos activos")
    void getAllProducts_sinProductosActivos_debeRetornarListaVacia() {
        when(productRepository.findByActiveTrue()).thenReturn(List.of());

        List<ProductResponse> result = productService.getAllProducts();

        assertThat(result).isEmpty();
    }

    // ─── deleteProduct ────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteProduct: debe hacer soft delete (active=false) y retornar true cuando el producto existe")
    void deleteProduct_cuandoExiste_debeHacerSoftDeleteYRetornarTrue() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        boolean result = productService.deleteProduct(1L);

        assertThat(result).isTrue();
        assertThat(product.getActive()).isFalse();
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("deleteProduct: debe retornar false cuando el producto no existe")
    void deleteProduct_cuandoNoExiste_debeRetornarFalse() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        boolean result = productService.deleteProduct(99L);

        assertThat(result).isFalse();
        verify(productRepository, never()).save(any());
    }

    // ─── searchProducts ───────────────────────────────────────────────────────

    @Test
    @DisplayName("searchProducts: debe retornar productos que coincidan con la palabra clave")
    void searchProducts_conCoincidencias_debeRetornarProductos() {
        when(productRepository.searchProducts("laptop")).thenReturn(List.of(product));

        List<ProductResponse> result = productService.searchProducts("laptop");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Laptop");
        verify(productRepository).searchProducts("laptop");
    }

    @Test
    @DisplayName("searchProducts: debe retornar lista vacía cuando no hay coincidencias")
    void searchProducts_sinCoincidencias_debeRetornarListaVacia() {
        when(productRepository.searchProducts("inexistente")).thenReturn(List.of());

        List<ProductResponse> result = productService.searchProducts("inexistente");

        assertThat(result).isEmpty();
    }

    // ─── updateProductFromRequest ─────────────────────────────────────────────

    @Test
    @DisplayName("updateProductFromRequest: debe mapear todos los campos del request al producto")
    void updateProductFromRequest_debeMapearTodosLosCampos() {
        Product emptyProduct = new Product();

        Product updated = productService.updateProductFromRequest(emptyProduct, productRequest);

        assertThat(updated.getName()).isEqualTo("Laptop");
        assertThat(updated.getDescription()).isEqualTo("Laptop gamer");
        assertThat(updated.getPrice()).isEqualByComparingTo(new BigDecimal("2500000"));
        assertThat(updated.getStockQuantity()).isEqualTo(10);
        assertThat(updated.getCategory()).isEqualTo("Electrónica");
        assertThat(updated.getImageUrl()).isEqualTo("http://imagen.com/laptop.jpg");
    }
}
