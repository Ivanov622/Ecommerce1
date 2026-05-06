package com.app.ecom.service;

import com.app.ecom.dto.OrderResponse;
import com.app.ecom.model.*;
import com.app.ecom.repository.OrderRepository;
import com.app.ecom.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias - OrderService")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Product product;
    private CartItem cartItem;
    private Order savedOrder;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFirstName("Laura");
        user.setLastName("Martínez");

        product = new Product();
        product.setId(10L);
        product.setName("Mouse Inalámbrico");
        product.setPrice(new BigDecimal("120000"));
        product.setStockQuantity(20);

        cartItem = new CartItem();
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setPrice(new BigDecimal("240000"));

        savedOrder = new Order();
        savedOrder.setId(100L);
        savedOrder.setUser(user);
        savedOrder.setTotalAmount(new BigDecimal("240000"));
        savedOrder.setOrderStatus(OrderStatus.CONFIRMED);

        OrderItem orderItem = new OrderItem(null, product, new BigDecimal("240000"), 2, savedOrder);
        savedOrder.setOrderItem(List.of(orderItem));
    }

    // ─── createOrder ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("createOrder: debe crear la orden y retornar Optional con OrderResponse cuando todo es válido")
    void createOrder_todoValido_debeRetornarOrderResponse() {
        when(cartService.getCart("1")).thenReturn(List.of(cartItem));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        Optional<OrderResponse> result = orderService.createOrder("1");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(100L);
        assertThat(result.get().getTotalAmount()).isEqualByComparingTo(new BigDecimal("240000"));
        assertThat(result.get().getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(result.get().getOrderItems()).hasSize(1);
        verify(cartService).clearCart("1");
    }

    @Test
    @DisplayName("createOrder: debe retornar Optional vacío cuando el carrito está vacío")
    void createOrder_carritoVacio_debeRetornarOptionalVacio() {
        when(cartService.getCart("1")).thenReturn(List.of());

        Optional<OrderResponse> result = orderService.createOrder("1");

        assertThat(result).isEmpty();
        verify(orderRepository, never()).save(any());
        verify(cartService, never()).clearCart(any());
    }

    @Test
    @DisplayName("createOrder: debe retornar Optional vacío cuando el usuario no existe")
    void createOrder_usuarioNoExistente_debeRetornarOptionalVacio() {
        when(cartService.getCart("1")).thenReturn(List.of(cartItem));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<OrderResponse> result = orderService.createOrder("1");

        assertThat(result).isEmpty();
        verify(orderRepository, never()).save(any());
        verify(cartService, never()).clearCart(any());
    }

    @Test
    @DisplayName("createOrder: debe calcular correctamente el total con múltiples items del carrito")
    void createOrder_variosItems_debeCalcularTotalCorrectamente() {
        CartItem cartItem2 = new CartItem();
        cartItem2.setUser(user);
        cartItem2.setProduct(product);
        cartItem2.setQuantity(1);
        cartItem2.setPrice(new BigDecimal("120000"));

        BigDecimal expectedTotal = new BigDecimal("360000"); // 240000 + 120000

        Order orderConTotal = new Order();
        orderConTotal.setId(200L);
        orderConTotal.setUser(user);
        orderConTotal.setTotalAmount(expectedTotal);
        orderConTotal.setOrderStatus(OrderStatus.CONFIRMED);
        orderConTotal.setOrderItem(List.of());

        when(cartService.getCart("1")).thenReturn(List.of(cartItem, cartItem2));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            assertThat(o.getTotalAmount()).isEqualByComparingTo(expectedTotal);
            return orderConTotal;
        });

        Optional<OrderResponse> result = orderService.createOrder("1");

        assertThat(result).isPresent();
        assertThat(result.get().getTotalAmount()).isEqualByComparingTo(expectedTotal);
    }

    @Test
    @DisplayName("createOrder: debe limpiar el carrito después de crear la orden exitosamente")
    void createOrder_exitosa_debeLimpiarCarrito() {
        when(cartService.getCart("1")).thenReturn(List.of(cartItem));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        orderService.createOrder("1");

        verify(cartService, times(1)).clearCart("1");
    }

    // ─── mapToOrdenReponse ────────────────────────────────────────────────────

    @Test
    @DisplayName("mapToOrdenReponse: debe mapear correctamente todos los campos de la orden")
    void mapToOrdenReponse_debeMappearCorrectamente() {
        OrderResponse response = orderService.mapToOrdenReponse(savedOrder);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("240000"));
        assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(response.getOrderItems()).hasSize(1);
        assertThat(response.getOrderItems().get(0).getProductId()).isEqualTo(10L);
        assertThat(response.getOrderItems().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("mapToOrdenReponse: debe calcular el subtotal correctamente por item")
    void mapToOrdenReponse_debeCalcularSubtotalPorItem() {
        OrderResponse response = orderService.mapToOrdenReponse(savedOrder);

        // precio * cantidad = 240000 * 2 = 480000
        BigDecimal expectedSubtotal = new BigDecimal("240000").multiply(new BigDecimal(2));
        assertThat(response.getOrderItems().get(0).getSubTotal())
                .isEqualByComparingTo(expectedSubtotal);
    }
}
