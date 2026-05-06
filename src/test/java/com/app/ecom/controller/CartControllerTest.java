package com.app.ecom.controller;

import com.app.ecom.dto.CartItemRequest;
import com.app.ecom.model.CartItem;
import com.app.ecom.model.Product;
import com.app.ecom.model.User;
import com.app.ecom.service.CartService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@DisplayName("Pruebas unitarias - CartController")
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    private CartItemRequest cartItemRequest;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        cartItemRequest = new CartItemRequest();
        cartItemRequest.setProductId(10L);
        cartItemRequest.setQuantity(2);

        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(10L);
        product.setName("Audífonos Bluetooth");
        product.setPrice(new BigDecimal("200000"));

        cartItem = new CartItem();
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setPrice(new BigDecimal("400000"));
    }

    // ─── POST /api/cart ───────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/cart: debe retornar 201 CREATED cuando se agrega el item exitosamente")
    void addToCart_exitoso_debeRetornar201() throws Exception {
        when(cartService.addToCart(eq("1"), any(CartItemRequest.class))).thenReturn(true);

        mockMvc.perform(post("/api/cart")
                        .header("X-User-ID", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartItemRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/cart: debe retornar 400 BAD REQUEST cuando el producto no existe o no hay stock")
    void addToCart_fallido_debeRetornar400() throws Exception {
        when(cartService.addToCart(eq("1"), any(CartItemRequest.class))).thenReturn(false);

        mockMvc.perform(post("/api/cart")
                        .header("X-User-ID", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartItemRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Product out of stock or user/product doesnt exist"));
    }

    // ─── DELETE /api/cart/items/{productId} ───────────────────────────────────

    @Test
    @DisplayName("DELETE /api/cart/items/{productId}: debe retornar 204 NO CONTENT cuando se elimina el item")
    void removeFromCart_exitoso_debeRetornar204() throws Exception {
        when(cartService.deleteItemFromCart("1", 10L)).thenReturn(true);

        mockMvc.perform(delete("/api/cart/items/10")
                        .header("X-User-ID", "1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/cart/items/{productId}: debe retornar 404 NOT FOUND cuando el item no existe")
    void removeFromCart_itemNoExistente_debeRetornar404() throws Exception {
        when(cartService.deleteItemFromCart("1", 99L)).thenReturn(false);

        mockMvc.perform(delete("/api/cart/items/99")
                        .header("X-User-ID", "1"))
                .andExpect(status().isNotFound());
    }

    // ─── GET /api/cart ────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/cart: debe retornar 200 OK con los items del carrito del usuario")
    void getCart_debeRetornar200ConItems() throws Exception {
        when(cartService.getCart("1")).thenReturn(List.of(cartItem));

        mockMvc.perform(get("/api/cart")
                        .header("X-User-ID", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].quantity").value(2));
    }

    @Test
    @DisplayName("GET /api/cart: debe retornar 200 OK con lista vacía cuando el carrito está vacío")
    void getCart_carritoVacio_debeRetornar200ListaVacia() throws Exception {
        when(cartService.getCart("1")).thenReturn(List.of());

        mockMvc.perform(get("/api/cart")
                        .header("X-User-ID", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
