package com.app.ecom.service;

import com.app.ecom.dto.AddressDTO;
import com.app.ecom.dto.UserRequest;
import com.app.ecom.dto.UserResponse;
import com.app.ecom.model.Address;
import com.app.ecom.model.User;
import com.app.ecom.model.UserRole;
import com.app.ecom.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias - UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserRequest userRequest;

    @BeforeEach
    void setUp() {
        Address address = new Address();
        address.setStreet("Calle 123");
        address.setCity("Bogotá");
        address.setState("Cundinamarca");
        address.setCountry("Colombia");
        address.setZipcode("110111");

        user = new User();
        user.setId(1L);
        user.setFirstName("Carlos");
        user.setLastName("López");
        user.setEmail("carlos@email.com");
        user.setPhone("3001234567");
        user.setRole(UserRole.CUSTOMER);
        user.setAddress(address);

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setStreet("Calle 123");
        addressDTO.setCity("Bogotá");
        addressDTO.setState("Cundinamarca");
        addressDTO.setCountry("Colombia");
        addressDTO.setZipcode("110111");

        userRequest = new UserRequest();
        userRequest.setFirstName("Carlos");
        userRequest.setLastName("López");
        userRequest.setEmail("carlos@email.com");
        userRequest.setPhone("3001234567");
        userRequest.setAddress(addressDTO);
    }

    // ─── fetchAllUsers ────────────────────────────────────────────────────────

    @Test
    @DisplayName("fetchAllUsers: debe retornar lista de UserResponse con todos los usuarios")
    void fetchAllUsers_debeRetornarListaDeUsuarios() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponse> result = userService.fetchAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Carlos");
        assertThat(result.get(0).getEmail()).isEqualTo("carlos@email.com");
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("fetchAllUsers: debe retornar lista vacía cuando no hay usuarios")
    void fetchAllUsers_sinUsuarios_debeRetornarListaVacia() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponse> result = userService.fetchAllUsers();

        assertThat(result).isEmpty();
    }

    // ─── fetchUser ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("fetchUser: debe retornar Optional con UserResponse cuando el usuario existe")
    void fetchUser_cuandoExiste_debeRetornarUserResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<UserResponse> result = userService.fetchUser(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("1");
        assertThat(result.get().getRole()).isEqualTo(UserRole.CUSTOMER);
    }

    @Test
    @DisplayName("fetchUser: debe retornar Optional vacío cuando el usuario no existe")
    void fetchUser_cuandoNoExiste_debeRetornarOptionalVacio() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<UserResponse> result = userService.fetchUser(99L);

        assertThat(result).isEmpty();
    }

    // ─── addUser ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addUser: debe guardar el usuario una vez en el repositorio")
    void addUser_debeGuardarElUsuario() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.addUser(userRequest);

        verify(userRepository, times(1)).save(any(User.class));
    }

    // ─── updateUser ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateUser: debe actualizar y retornar true cuando el usuario existe")
    void updateUser_cuandoExiste_debeActualizarYRetornarTrue() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        boolean result = userService.updateUser(1L, userRequest);

        assertThat(result).isTrue();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser: debe retornar false cuando el usuario no existe")
    void updateUser_cuandoNoExiste_debeRetornarFalse() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        boolean result = userService.updateUser(99L, userRequest);

        assertThat(result).isFalse();
        verify(userRepository, never()).save(any());
    }

    // ─── mapToUserResponse ────────────────────────────────────────────────────

    @Test
    @DisplayName("mapToUserResponse: debe mapear correctamente todos los campos incluyendo dirección")
    void mapToUserResponse_debeMapearTodosLosCampos() {
        UserResponse response = userService.mapToUserResponse(user);

        assertThat(response.getId()).isEqualTo("1");
        assertThat(response.getFirstName()).isEqualTo("Carlos");
        assertThat(response.getLastName()).isEqualTo("López");
        assertThat(response.getEmail()).isEqualTo("carlos@email.com");
        assertThat(response.getPhone()).isEqualTo("3001234567");
        assertThat(response.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(response.getAddress()).isNotNull();
        assertThat(response.getAddress().getCity()).isEqualTo("Bogotá");
    }

    @Test
    @DisplayName("mapToUserResponse: debe funcionar sin dirección y retornar address null")
    void mapToUserResponse_sinDireccion_debeRetornarAddressNull() {
        user.setAddress(null);

        UserResponse response = userService.mapToUserResponse(user);

        assertThat(response.getAddress()).isNull();
    }

    // ─── updateUserFromRequest ────────────────────────────────────────────────

    @Test
    @DisplayName("updateUserFromRequest: debe asignar dirección cuando el request tiene address")
    void updateUserFromRequest_conDireccion_debeAsignarAddress() {
        User emptyUser = new User();

        User result = userService.updateUserFromRequest(emptyUser, userRequest);

        assertThat(result.getFirstName()).isEqualTo("Carlos");
        assertThat(result.getAddress()).isNotNull();
        assertThat(result.getAddress().getCity()).isEqualTo("Bogotá");
    }

    @Test
    @DisplayName("updateUserFromRequest: no debe asignar dirección cuando el request no tiene address")
    void updateUserFromRequest_sinDireccion_noDebeAsignarAddress() {
        userRequest.setAddress(null);
        User emptyUser = new User();

        User result = userService.updateUserFromRequest(emptyUser, userRequest);

        assertThat(result.getAddress()).isNull();
        assertThat(result.getEmail()).isEqualTo("carlos@email.com");
    }
}
