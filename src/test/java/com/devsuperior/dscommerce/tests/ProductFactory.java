package com.devsuperior.dscommerce.tests;

import com.devsuperior.dscommerce.entities.Category;
import com.devsuperior.dscommerce.entities.Product;

public class ProductFactory {

    public static Product createProduct() {
        Category category = CategoryFactory.createCategory();
        Product product = new Product(1L, "Play5", "videogame description", 4000.0, "url/img");
        product.getCategories().add(category);
        return product;
    }

    public static Product createProduct(String name) {
        Product product = createProduct();
        product.setName(name);
        return product;
    }

    /* public static ProductDTO createProductDTO() {
        Category category = CategoryFactory.createCategory(2L, "Eletro");
        Product product = new Product(null, "Play4", "videogame description", 4000.0, "url/img");
        product.getCategories().add(category);
        ProductDTO productDTO = new ProductDTO(product);
        return productDTO;
    }

    public static ProductDTO createCustomProductDTO(String name, String description, double price) {
        Category category = CategoryFactory.createCategory(2L, "Eletro");
        Product product = new Product(null, name, description, price, "url/img");
        product.getCategories().add(category);
        ProductDTO productDTO = new ProductDTO(product);
        return productDTO;
    } */
    
}
