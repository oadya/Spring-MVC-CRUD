package com.springmvc.crud.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springmvc.crud.controller.UserController;
import com.springmvc.crud.filter.CORSFilter;
import com.springmvc.crud.model.User;
import com.springmvc.crud.service.UserService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserControllerTest {

    private static final int UNKNOWN_ID = Integer.MAX_VALUE;

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Before
    public void init(){
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .addFilters(new CORSFilter())
                .build();
    }

    // =========================================== Get All Users ==========================================

    @Test
    public void test_get_all_success() throws Exception {
        List<User> users = Arrays.asList(
                new User(1, "Daenerys Targaryen"),
                new User(2, "John Snow"));

        when(userService.getAll()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].username", is("Daenerys Targaryen")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].username", is("John Snow")));

        verify(userService, times(1)).getAll();
        verifyNoMoreInteractions(userService);
    }

    // =========================================== Get User By ID =========================================

    @Test
    public void test_get_by_id_success() throws Exception {
        User user = new User(1, "Daenerys Targaryen");

        when(userService.findById(1)).thenReturn(user);

        mockMvc.perform(get("/users/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("Daenerys Targaryen")));

        verify(userService, times(1)).findById(1);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void test_get_by_id_fail_404_not_found() throws Exception {

        when(userService.findById(1)).thenReturn(null);

        mockMvc.perform(get("/users/{id}", 1))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findById(1);
        verifyNoMoreInteractions(userService);
    }

    // =========================================== Create New User ========================================

    @Test
    public void test_create_user_success() throws Exception {
        User user = new User("Arya Stark");

        when(userService.exists(user)).thenReturn(false);
        doNothing().when(userService).create(user);

        mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(user)))
                .andExpect(status().isCreated())
                .andExpect(header().string("location", containsString("http://localhost/users/")));

        verify(userService, times(1)).exists(user);
        verify(userService, times(1)).create(user);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void test_create_user_fail_409_conflict() throws Exception {
        User user = new User("username exists");

        when(userService.exists(user)).thenReturn(true);

        mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(user)))
                .andExpect(status().isConflict());

        verify(userService, times(1)).exists(user);
        verifyNoMoreInteractions(userService);
    }

    // =========================================== Update Existing User ===================================

    @Test
    public void test_update_user_success() throws Exception {
        User user = new User(1, "Arya Stark");

        when(userService.findById(user.getId())).thenReturn(user);
        doNothing().when(userService).update(user);

        mockMvc.perform(
                put("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(user)))
                .andExpect(status().isOk());

        verify(userService, times(1)).findById(user.getId());
        verify(userService, times(1)).update(user);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void test_update_user_fail_404_not_found() throws Exception {
        User user = new User(UNKNOWN_ID, "user not found");

        when(userService.findById(user.getId())).thenReturn(null);

        mockMvc.perform(
                put("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(user)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findById(user.getId());
        verifyNoMoreInteractions(userService);
    }

    // =========================================== Delete User ============================================

    @Test
    public void test_delete_user_success() throws Exception {
        User user = new User(1, "Arya Stark");

        when(userService.findById(user.getId())).thenReturn(user);
        doNothing().when(userService).delete(user.getId());

        mockMvc.perform(
                delete("/users/{id}", user.getId()))
                .andExpect(status().isOk());

        verify(userService, times(1)).findById(user.getId());
        verify(userService, times(1)).delete(user.getId());
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void test_delete_user_fail_404_not_found() throws Exception {
        User user = new User(UNKNOWN_ID, "user not found");

        when(userService.findById(user.getId())).thenReturn(null);

        mockMvc.perform(
                delete("/users/{id}", user.getId()))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findById(user.getId());
        verifyNoMoreInteractions(userService);
    }

    // =========================================== CORS Headers ===========================================

    @Test
    public void test_cors_headers() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(header().string("Access-Control-Allow-Origin", "*"))
                .andExpect(header().string("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE"))
                .andExpect(header().string("Access-Control-Allow-Headers", "*"))
                .andExpect(header().string("Access-Control-Max-Age", "3600"));
    }

    /*
     * converts a Java object into JSON representation
     */
    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}