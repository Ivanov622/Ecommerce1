package com.app.ecom.controller;

import com.app.ecom.dto.ProductRequest;
import com.app.ecom.dto.ProductResponse;
import com.app.ecom.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@DisplayName("Pruebas unitarias - ProductController")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductRequest productRequest;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        productRequest = new ProductRequest();
        productRequest.setName("Monitor 27\"");
        productRequest.setDescription("Monitor full HD");
        productRequest.setPrice(new BigDecimal("850000"));
        productRequest.setStockQuantity(15);
        productRequest.setCategory("Periféricos");
        productRequest.setImageUrl("http://img.com/monitor.jpg");

        productResponse = new ProductResponse();
        productResponse.setId(1L);
        productResponse.setName("Monitor 27\"");
        productResponse.setDescription("Monitor full HD");
        productResponse.setPrice(new BigDecimal("850000"));
        productResponse.setStockQuantity(15);
        productResponse.setCategory("Periféricos");
        productResponse.setImageUrl("http://img.com/monitor.jpg");
        productResponse.setActive(true);
    }

    // ─── POST /api/products ───────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/products: debe retornar 201 CREATED con el producto creado")
    void createProduct_debeRetornar201ConProducto() throws Exception {
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(productResponse);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Monitor 27\""))
                .andExpect(jsonPath("$.active").value(true));
    }

    // ─── GET /api/products ────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/products: debe retornar 200 OK con la lista de productos")
    void getProducts_debeRetornar200ConLista() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(productResponse));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Monitor 27\""));
    }

    @Test
    @DisplayName("GET /api/products: debe retornar 200 OK con lista vacía cuando no hay productos")
    void getProducts_sinProductos_debeRetornar200ConListaVacia() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── PUT /api/products/{id} ───────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/products/{id}: debe retornar 200 OK cuando el producto existe")
    void updateProduct_cuandoExiste_debeRetornar200() throws Exception {
        when(productService.updateProduct(eq(1L), any(ProductRequest.class)))
                .thenReturn(Optional.of(productResponse));

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PUT /api/products/{id}: debe retornar 404 NOT FOUND cuando el producto no existe")
    void updateProduct_cuandoNoExiste_debeRetornar404() throws Exception {
        when(productService.updateProduct(eq(99L), any(ProductRequest.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/products/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isNotFound());
    }

    // ─── DELETE /api/products/{id} ────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/products/{id}: debe retornar 204 NO CONTENT cuando el producto existe")
    void deleteProduct_cuandoExiste_debeRetornar204() throws Exception {
        when(productService.deleteProduct(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/products/{id}: debe retornar 404 NOT FOUND cuando el producto no existe")
    void deleteProduct_cuandoNoExiste_debeRetornar404() throws Exception {
        when(productService.deleteProduct(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/products/99"))
                .andExpect(status().isNotFound());
    }

    // ─── GET /api/products/search ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/products/search: debe retornar 200 OK con productos que coinciden con la búsqueda")
    void searchProducts_conCoincidencias_debeRetornar200() throws Exception {
        when(productService.searchProducts("monitor")).thenReturn(List.of(productResponse));

        mockMvc.perform(get("/api/products/search").param("keyWord", "monitor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Monitor 27\""));
    }

    @Test
    @DisplayName("GET /api/products/search: debe retornar 200 OK con lista vacía sin coincidencias")
    void searchProducts_sinCoincidencias_debeRetornar200ListaVacia() throws Exception {
        when(productService.searchProducts("xyz")).thenReturn(List.of());

        mockMvc.perform(get("/api/products/search").param("keyWord", "xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
