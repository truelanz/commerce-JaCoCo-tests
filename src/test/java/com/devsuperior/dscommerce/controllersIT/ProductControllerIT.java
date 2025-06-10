package com.devsuperior.dscommerce.controllersIT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductControllerIT {

    @Autowired
    private MockMvc mockMvc;

    private String productName;

    @BeforeEach
    void setUp() {
        productName = "Macbook";

    }

    @Test
    public void findAllShouldReturnPageWhenNameParamIsNotEmpty() throws Exception {

        ResultActions result = mockMvc
            .perform(MockMvcRequestBuilders.get("/products?name={productName}", productName)
            .accept(MediaType.APPLICATION_JSON));
        
        result.andExpect(MockMvcResultMatchers.status().isOk());
        result.andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(3L));
        result.andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").value("Macbook Pro"));
        result.andExpect(MockMvcResultMatchers.jsonPath("$.content[0].price").value(1250.0));
    }

    @Test
    public void findAllShouldReturnPageWhenNameParamIsIsEmpty() throws Exception {

        ResultActions result = mockMvc
            .perform(MockMvcRequestBuilders.get("/products")
            .accept(MediaType.APPLICATION_JSON));
        
        result.andExpect(MockMvcResultMatchers.status().isOk());
        result.andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(1L));
        result.andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").value("The Lord of the Rings"));
        result.andExpect(MockMvcResultMatchers.jsonPath("$.content[0].price").value(90.5));
    }
}
