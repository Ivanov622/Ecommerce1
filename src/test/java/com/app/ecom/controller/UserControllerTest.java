package com.app.ecom.controller;

import com.app.ecom.dto.UserRequest;
import com.app.ecom.dto.UserResponse;
import com.app.ecom.model.UserRole;
import com.app.ecom.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("Pruebas unitarias - UserController")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserRequest userRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userRequest = new UserRequest();
        userRequest.setFirstName("Pedro");
        userRequest.setLastName("Ramírez");
        userRequest.setEmail("pedro@email.com");
        userRequest.setPhone("3109876543");

        userResponse = new UserResponse();
        userResponse.setId("1");
        userResponse.setFirstName("Pedro");
        userResponse.setLastName("Ramírez");
        userResponse.setEmail("pedro@email.com");
        userResponse.setPhone("3109876543");
        userResponse.setRole(UserRole.CUSTOMER);
    }

    // ─── GET /api/users ───────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/users: debe retornar 200 OK con la lista de usuarios")
    void getAllUsers_debeRetornar200ConLista() throws Exception {
        when(userService.fetchAllUsers()).thenReturn(List.of(userResponse));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].firstName").value("Pedro"))
                .andExpect(jsonPath("$[0].role").value("CUSTOMER"));
    }

    @Test
    @DisplayName("GET /api/users: debe retornar 200 OK con lista vacía cuando no hay usuarios")
    void getAllUsers_sinUsuarios_debeRetornar200ListaVacia() throws Exception {
        when(userService.fetchAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── GET /api/users/{id} ──────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/users/{id}: debe retornar 200 OK con el usuario cuando existe")
    void findUserById_cuandoExiste_debeRetornar200() throws Exception {
        when(userService.fetchUser(1L)).thenReturn(Optional.of(userResponse));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.email").value("pedro@email.com"));
    }

    @Test
    @DisplayName("GET /api/users/{id}: debe retornar 404 NOT FOUND cuando el usuario no existe")
    void findUserById_cuandoNoExiste_debeRetornar404() throws Exception {
        when(userService.fetchUser(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    // ─── POST /api/users ──────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/users: debe retornar 200 OK con mensaje de éxito al crear usuario")
    void createUser_debeRetornar200ConMensaje() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("User added successfully"));
    }

    // ─── PUT /api/users/{id} ──────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/users/{id}: debe retornar 200 OK con mensaje de éxito cuando el usuario existe")
    void updateUser_cuandoExiste_debeRetornar200() throws Exception {
        when(userService.updateUser(eq(1L), any(UserRequest.class))).thenReturn(true);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("User updated successfully"));
    }

    @Test
    @DisplayName("PUT /api/users/{id}: debe retornar 404 NOT FOUND cuando el usuario no existe")
    void updateUser_cuandoNoExiste_debeRetornar404() throws Exception {
        when(userService.updateUser(eq(99L), any(UserRequest.class))).thenReturn(false);

        mockMvc.perform(put("/api/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isNotFound());
    }
}
