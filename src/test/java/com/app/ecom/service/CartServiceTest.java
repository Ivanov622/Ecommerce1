package com.app.ecom.service;

import com.app.ecom.dto.CartItemRequest;
import com.app.ecom.model.CartItem;
import com.app.ecom.model.Product;
import com.app.ecom.model.User;
import com.app.ecom.repository.CartItemRepository;
import com.app.ecom.repository.ProductRepository;
import com.app.ecom.repository.UserRepository;
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
@DisplayName("Pruebas unitarias - CartService")
class CartServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Product product;
    private CartItemRequest cartItemRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFirstName("Ana");
        user.setLastName("Gómez");

        product = new Product();
        product.setId(10L);
        product.setName("Teclado Mecánico");
        product.setPrice(new BigDecimal("350000"));
        product.setStockQuantity(5);
        product.setActive(true);

        cartItemRequest = new CartItemRequest();
        cartItemRequest.setProductId(10L);
        cartItemRequest.setQuantity(2);
    }

    // ─── addToCart ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addToCart: debe crear un nuevo CartItem y retornar true cuando el producto y usuario existen")
    void addToCart_nuevoItem_debeCrearCartItemYRetornarTrue() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserAndProduct(user, product)).thenReturn(null);
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean result = cartService.addToCart("1", cartItemRequest);

        assertThat(result).isTrue();
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("addToCart: debe actualizar cantidad y precio cuando el producto ya estaba en el carrito")
    void addToCart_itemExistente_debeActualizarCantidadYPrecio() {
        CartItem existingItem = new CartItem();
        existingItem.setUser(user);
        existingItem.setProduct(product);
        existingItem.setQuantity(1);
        existingItem.setPrice(new BigDecimal("350000"));

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserAndProduct(user, product)).thenReturn(existingItem);
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean result = cartService.addToCart("1", cartItemRequest);

        assertThat(result).isTrue();
        // 1 existente + 2 nuevos = 3
        assertThat(existingItem.getQuantity()).isEqualTo(3);
        assertThat(existingItem.getPrice()).isEqualByComparingTo(new BigDecimal("1050000"));
    }

    @Test
    @DisplayName("addToCart: debe retornar false cuando el producto no existe")
    void addToCart_productoNoExistente_debeRetornarFalse() {
        when(productRepository.findById(10L)).thenReturn(Optional.empty());

        boolean result = cartService.addToCart("1", cartItemRequest);

        assertThat(result).isFalse();
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("addToCart: debe retornar false cuando el stock es insuficiente")
    void addToCart_stockInsuficiente_debeRetornarFalse() {
        product.setStockQuantity(1); // solo 1 en stock, se piden 2
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        boolean result = cartService.addToCart("1", cartItemRequest);

        assertThat(result).isFalse();
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("addToCart: debe retornar false cuando el usuario no existe")
    void addToCart_usuarioNoExistente_debeRetornarFalse() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = cartService.addToCart("1", cartItemRequest);

        assertThat(result).isFalse();
        verify(cartItemRepository, never()).save(any());
    }

    // ─── deleteItemFromCart ───────────────────────────────────────────────────

    @Test
    @DisplayName("deleteItemFromCart: debe eliminar el item y retornar true cuando producto y usuario existen")
    void deleteItemFromCart_cuandoExisten_debeEliminarYRetornarTrue() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        boolean result = cartService.deleteItemFromCart("1", 10L);

        assertThat(result).isTrue();
        verify(cartItemRepository).deleteByUserAndProduct(user, product);
    }

    @Test
    @DisplayName("deleteItemFromCart: debe retornar false cuando el producto no existe")
    void deleteItemFromCart_productoNoExistente_debeRetornarFalse() {
        when(productRepository.findById(10L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        boolean result = cartService.deleteItemFromCart("1", 10L);

        assertThat(result).isFalse();
        verify(cartItemRepository, never()).deleteByUserAndProduct(any(), any());
    }

    @Test
    @DisplayName("deleteItemFromCart: debe retornar false cuando el usuario no existe")
    void deleteItemFromCart_usuarioNoExistente_debeRetornarFalse() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = cartService.deleteItemFromCart("1", 10L);

        assertThat(result).isFalse();
        verify(cartItemRepository, never()).deleteByUserAndProduct(any(), any());
    }

    // ─── getCart ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getCart: debe retornar la lista de CartItems del usuario")
    void getCart_usuarioExistente_debeRetornarItems() {
        CartItem item = new CartItem();
        item.setUser(user);
        item.setProduct(product);
        item.setQuantity(2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUser(user)).thenReturn(List.of(item));

        List<CartItem> result = cartService.getCart("1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProduct().getName()).isEqualTo("Teclado Mecánico");
    }

    @Test
    @DisplayName("getCart: debe retornar lista vacía cuando el usuario no existe")
    void getCart_usuarioNoExistente_debeRetornarListaVacia() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        List<CartItem> result = cartService.getCart("99");

        assertThat(result).isEmpty();
    }

    // ─── clearCart ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("clearCart: debe llamar deleteByUser cuando el usuario existe")
    void clearCart_usuarioExistente_debeEliminarTodosLosItems() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        cartService.clearCart("1");

        verify(cartItemRepository).deleteByUser(user);
    }

    @Test
    @DisplayName("clearCart: no debe llamar deleteByUser cuando el usuario no existe")
    void clearCart_usuarioNoExistente_noDebeEliminarNada() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        cartService.clearCart("99");

        verify(cartItemRepository, never()).deleteByUser(any());
    }
}
