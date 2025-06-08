package com.selimhorri.app.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.selimhorri.app.domain.Category;
import com.selimhorri.app.domain.Product;
import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.helper.ProductMappingHelper;
import com.selimhorri.app.repository.ProductRepository;
import com.selimhorri.app.service.impl.ProductServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindById_ReturnsProductDto() {
        Category category = Category.builder().categoryId(1).categoryTitle("Test Category").build();
        Product product = Product.builder().productId(1).productTitle("Test Product").category(category).build();
        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        ProductDto result = productService.findById(1);

        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals("Test Product", result.getProductTitle());
        assertNotNull(result.getCategoryDto());
        assertEquals("Test Category", result.getCategoryDto().getCategoryTitle());
    }

    @Test
    void testSave_ReturnsSavedProductDto() {
        CategoryDto categoryDto = CategoryDto.builder().categoryId(1).categoryTitle("New Category").build();
        ProductDto dto = ProductDto.builder().productId(2).productTitle("New Product").categoryDto(categoryDto).build();
        Product product = ProductMappingHelper.map(dto);

        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDto result = productService.save(dto);

        assertNotNull(result);
        assertEquals("New Product", result.getProductTitle());
        assertNotNull(result.getCategoryDto());
        assertEquals("New Category", result.getCategoryDto().getCategoryTitle());
    }

    @Test
    void testFindById_ThrowsException_WhenNotFound() {
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(
            com.selimhorri.app.exception.wrapper.ProductNotFoundException.class,
            () -> productService.findById(99)
        );
    }

}