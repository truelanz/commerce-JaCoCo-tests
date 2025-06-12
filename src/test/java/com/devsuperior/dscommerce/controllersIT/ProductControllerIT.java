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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscommerce.dto.ProductDTO;
import com.devsuperior.dscommerce.entities.Product;
import com.devsuperior.dscommerce.tests.ProductFactory;
import com.devsuperior.dscommerce.tests.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenUtil tokenUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private Product product;
    private ProductDTO productDTO;
    private String productName;
    private String adminToken, clientToken, invalidToken;
    private Long existingId, nonExistingId, dependentId;

    @BeforeEach
    void setUp() throws Exception {
    
        productName = "Macbook";
        existingId = 1L; // Existente no DB
        nonExistingId = 100L; // Não existe no DB
        dependentId = 3L; // Tal qual o DB
        adminToken = tokenUtil.obtainAccessToken(mockMvc, "alex@gmail.com", "123456");
        clientToken = tokenUtil.obtainAccessToken(mockMvc, "maria@gmail.com", "123456");
        invalidToken = adminToken + "8564";

        product = ProductFactory.createProduct();
    }

    @Test // 204
    public void deleteShouldReturnNoContentWhenExistingIdAndAdminLogged() throws Exception {
        
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete("/products/{id}", existingId)
            .header("Authorization", "Bearer " + adminToken)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print());

        result.andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test //404
    public void deleteShouldReturnNotFoundWhenNonExistingIdAndAdminLogged() throws Exception {

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete("/products/{id}", nonExistingId)
            .header("Authorization", "Bearer " + adminToken)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print());

        result.andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test //400
    @Transactional(propagation = Propagation.SUPPORTS) // Para validar erros de DB Violation
    public void deleteShouldReturnBadRequestdWhenDependentIdAndAdminLogged() throws Exception {

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete("/products/{id}", dependentId)
            .header("Authorization", "Bearer " + adminToken)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print());

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test //403
    public void deleteShouldReturnForbiddenWhenExitingIdClientLogged() throws Exception {

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete("/products/{id}", dependentId)
            .header("Authorization", "Bearer " + clientToken)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print());

        result.andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test //403
    public void deleteShouldReturnUnauthorizesnWhenExitingIdAndInvalidToken() throws Exception {

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete("/products/{id}", existingId)
            .header("Authorization", "Bearer " + invalidToken)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print());

        result.andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test //200
    public void findAllShouldReturnPageWhenNameParamIsNotEmpty() throws Exception {

        ResultActions result = mockMvc
            .perform(MockMvcRequestBuilders.get("/products?name={productName}", productName)
            .accept(MediaType.APPLICATION_JSON));
        
        result.andExpect(MockMvcResultMatchers.status().isOk());
        result.andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(3L));
        result.andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").value("Macbook Pro"));
        result.andExpect(MockMvcResultMatchers.jsonPath("$.content[0].price").value(1250.0));
    }

    @Test //200
    public void findAllShouldReturnPageWhenNameParamIsIsEmpty() throws Exception {

        ResultActions result = mockMvc
            .perform(MockMvcRequestBuilders.get("/products")
            .accept(MediaType.APPLICATION_JSON));
        
        result.andExpect(MockMvcResultMatchers.status().isOk());
        result.andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(1L));
        result.andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").value("The Lord of the Rings"));
        result.andExpect(MockMvcResultMatchers.jsonPath("$.content[0].price").value(90.5));
    }

    @Test //201
    public void insertShouldReturnProductDTOCreatedWhenAdminLogged() throws Exception {

        productDTO = new ProductDTO(product);
        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/products")
            .header("Authorization", "Bearer " + adminToken) // Deve haver um espaço entre Bearer e o token.
            .content(jsonBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print());

        result.andExpect(MockMvcResultMatchers.status().isCreated());
        result.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(26L));
        result.andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Play5"));
        result.andExpect(MockMvcResultMatchers.jsonPath("$.price").value(4000.0));
        result.andExpect(MockMvcResultMatchers.jsonPath("$.imgUrl").value("url/img"));
        result.andExpect(MockMvcResultMatchers.jsonPath("$.categories[0].id").value(1L));

    }

    @Test //401
    public void insertShouldReturnUnauthorizedWhenInvalidToken() throws Exception {

        productDTO = new ProductDTO(product);
        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/products")
            .header("Authorization", "Bearer " + invalidToken)
            .content(jsonBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));

        result.andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test //403
    public void insertShouldReturnForbiddenWhenClientLogged() throws Exception {

        productDTO = new ProductDTO(product);
        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/products")
            .header("Authorization", "Bearer " + clientToken)
            .content(jsonBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));

        result.andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test //422
    public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndInvalidName() throws Exception {

        product.setName("ad");
        productDTO = new ProductDTO(product);
        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/products")
            .header("Authorization", "Bearer " + adminToken)
            .content(jsonBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));

        result.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
    }

    @Test //422
    public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndInvalidDescription() throws Exception {

        product.setDescription("videogame");
        productDTO = new ProductDTO(product);
        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/products")
            .header("Authorization", "Bearer " + adminToken)
            .content(jsonBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));

        result.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
    }

    @Test //422
    public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndNegativePrice() throws Exception {

        product.setPrice(-400.0);
        productDTO = new ProductDTO(product);
        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/products")
            .header("Authorization", "Bearer " + adminToken)
            .content(jsonBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));

        result.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
    }

    @Test //422
    public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndZeroPrice() throws Exception {

        product.setPrice(0.0);
        productDTO = new ProductDTO(product);
        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/products")
            .header("Authorization", "Bearer " + adminToken)
            .content(jsonBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));

        result.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
    }

    @Test //422
    public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndProductHasNoCategories() throws Exception {

        product.getCategories().clear();
        productDTO = new ProductDTO(product);
        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/products")
            .header("Authorization", "Bearer " + adminToken)
            .content(jsonBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));

        result.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
    }
}
