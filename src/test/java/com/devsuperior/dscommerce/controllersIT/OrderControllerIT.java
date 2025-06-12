package com.devsuperior.dscommerce.controllersIT;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscommerce.entities.Order;
import com.devsuperior.dscommerce.entities.User;
import com.devsuperior.dscommerce.tests.OrderFactory;
import com.devsuperior.dscommerce.tests.TokenUtil;
import com.devsuperior.dscommerce.tests.UserFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrderControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenUtil tokenUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private User clientUser, adminUser;
    @SuppressWarnings("unused")
    private Order orderAdmin, orderClient;
    private String adminToken, clientToken, invalidToken;
    private Long existingClientOrderId, existingAdminOrderId, nonExistingId;
    
    @BeforeEach
    void setUp() throws Exception {
        existingClientOrderId = 1L; // Existente no DB;
        existingAdminOrderId = 2L; // Existente no DB somente para admin;
        nonExistingId = 100L; // Não existe no DB
        adminToken = tokenUtil.obtainAccessToken(mockMvc, "alex@gmail.com", "123456");
        clientToken = tokenUtil.obtainAccessToken(mockMvc, "maria@gmail.com", "123456");
        invalidToken = adminToken + "8564";

        adminUser = UserFactory.createAdminUser();
        orderAdmin = OrderFactory.createOrder(adminUser);

        clientUser = UserFactory.createClientUser();
        orderClient = OrderFactory.createOrder(clientUser);
    }

    @Test // 200
    public void findByIdShouldReturnOrderDTOWhenExistingIdAdminLogged() throws Exception {
        
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/orders/{id}", existingAdminOrderId)
            .header("Authorization", "Bearer " + adminToken)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print());

        result.andExpect(MockMvcResultMatchers.status().isOk());
        result.andExpect(jsonPath("$.id").value(existingAdminOrderId));
        result.andExpect(jsonPath("$.status").value("DELIVERED"));
        result.andExpect(jsonPath("$.client").exists());
        result.andExpect(jsonPath("$.client.name").value("Alex Green"));
        result.andExpect(jsonPath("$.payment").exists());
        result.andExpect(jsonPath("$.items").exists());
        result.andExpect(jsonPath("$.items[0].name").value("Macbook Pro"));
    }

    @Test // 200
    public void findByIdShouldReturnOrderDTOWhenExistingIdBelongClientAndClientLogged() throws Exception {
        
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/orders/{id}", existingClientOrderId)
            .header("Authorization", "Bearer " + clientToken)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print());

        result.andExpect(MockMvcResultMatchers.status().isOk());
        result.andExpect(jsonPath("$.id").value(existingClientOrderId));
        result.andExpect(jsonPath("$.status").value("PAID"));
        result.andExpect(jsonPath("$.client").exists());
        result.andExpect(jsonPath("$.client.name").value("Maria Brown"));
        result.andExpect(jsonPath("$.payment").exists());
        result.andExpect(jsonPath("$.items").exists());
        result.andExpect(jsonPath("$.items[0].name").value("The Lord of the Rings"));
        result.andExpect(jsonPath("$.items[1].name").value("Macbook Pro"));
    }
    
    @Test // 403
    public void findByIdShouldReturnForbiddenWhenIdDoesNotBelongUserAndClientLogged() throws Exception {
        
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/orders/{id}", existingAdminOrderId)
            .header("Authorization", "Bearer " + clientToken)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print());

        result.andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test // 404
    public void findByIdShouldReturnNotFoundWhenExistingIdAndAdminLogged() throws Exception {
        
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/orders/{id}", nonExistingId)
            .header("Authorization", "Bearer " + adminToken)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print());

        result.andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test // 404
    public void findByIdShouldReturnNotFoundWhenNonExistingIdAndClientLogged() throws Exception {
        
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/orders/{id}", nonExistingId)
            .header("Authorization", "Bearer " + clientToken)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print());

        result.andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test // 401
    public void findByIdShouldReturnUnauthorizedWhenInvalidToken() throws Exception {
        
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/orders/{id}", existingClientOrderId)
            .header("Authorization", "Bearer " + invalidToken)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print());

        result.andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }
    
}
