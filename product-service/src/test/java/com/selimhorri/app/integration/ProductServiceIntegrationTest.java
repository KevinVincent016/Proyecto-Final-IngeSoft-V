package com.selimhorri.app.integration;

import com.selimhorri.app.domain.Category;
import com.selimhorri.app.domain.Product;
import com.selimhorri.app.repository.CategoryRepository;
import com.selimhorri.app.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ProductServiceIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void testSaveAndFindProduct() {
        Category category = Category.builder().categoryTitle("Electronics").build();
        category = categoryRepository.save(category);

        Product product = Product.builder().productTitle("Laptop").category(category).build();
        product = productRepository.save(product);

        Optional<Product> found = productRepository.findById(product.getProductId());
        assertTrue(found.isPresent());
        assertEquals("Laptop", found.get().getProductTitle());
        assertEquals("Electronics", found.get().getCategory().getCategoryTitle());
    }

    @Test
    void testDeleteProduct() {
        Category category = Category.builder().categoryTitle("Books").build();
        category = categoryRepository.save(category);

        Product product = Product.builder().productTitle("Spring in Action").category(category).build();
        product = productRepository.save(product);

        Integer productId = product.getProductId();
        productRepository.deleteById(productId);

        Optional<Product> found = productRepository.findById(productId);
        assertFalse(found.isPresent());
    }

}