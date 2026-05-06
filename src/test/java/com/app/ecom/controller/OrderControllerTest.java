package com.app.ecom.controller;

import com.app.ecom.dto.OrderItemDTO;
import com.app.ecom.dto.OrderResponse;
import com.app.ecom.model.OrderStatus;
import com.app.ecom.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@DisplayName("Pruebas unitarias - OrderController")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        OrderItemDTO itemDTO = new OrderItemDTO(
                1L, 10L, 2,
                new BigDecimal("240000"),
                new BigDecimal("480000")
        );

        orderResponse = new OrderResponse(
                100L,
                new BigDecimal("240000"),
                OrderStatus.CONFIRMED,
                List.of(itemDTO),
                LocalDateTime.now()
        );
    }

    // ─── POST /api/orders ─────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/orders: debe retornar 201 CREATED con la orden cuando el carrito tiene items")
    void createOrder_exitoso_debeRetornar201ConOrden() throws Exception {
        when(orderService.createOrder("1")).thenReturn(Optional.of(orderResponse));

        mockMvc.perform(post("/api/orders")
                        .header("X-User-ID", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.orderStatus").value("CONFIRMED"))
                .andExpect(jsonPath("$.orderItems.length()").value(1));
    }

    @Test
    @DisplayName("POST /api/orders: debe retornar 400 BAD REQUEST cuando el carrito está vacío o el usuario no existe")
    void createOrder_carritoVacioOUsuarioInvalido_debeRetornar400() throws Exception {
        when(orderService.createOrder("1")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/orders")
                        .header("X-User-ID", "1"))
                .andExpect(status().isBadRequest());
    }
}
