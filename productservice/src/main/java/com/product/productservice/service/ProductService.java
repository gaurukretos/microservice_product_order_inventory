package com.product.productservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.product.productservice.dto.ProductDto;
import com.product.productservice.dto.ProductResponse;
import com.product.productservice.model.Product;
import com.product.productservice.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // public ProductService(ProductRepository productRepository) {
    // this.productRepository = productRepository;
    // }

    public void createProduct(ProductDto productDto) {
        Product product = Product.builder().name(productDto.getName()).description(productDto.getDescription())
                .price(productDto.getPrice()).build();
        productRepository.save(product);
        log.info("Product {} is saved", product.getId());
    }

    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(this::mapToProductResponse).toList();
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId()).name(product.getName()).price(product.getPrice())
                .description(product.getDescription())
                .build();

    }

    private ProductDto mapToProductDto(Product product) {
        return ProductDto.builder()
                .name(product.getName()).price(product.getPrice())
                .description(product.getDescription())
                .build();

    }

    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id).orElse(null);
        return mapToProductResponse(product);
    }

    public ProductDto updateProduct(ProductResponse productResponse, String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product with ID " + id + " not found"));

        product.setName(productResponse.getName());
        product.setDescription(productResponse.getDescription());
        product.setPrice(productResponse.getPrice());

        product = productRepository.save(product);
        return mapToProductDto(product);
    }

    public void deleteProduct(String id) {
        if (!productExists(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with ID " + id + " not found");
        }
        productRepository.deleteById(id);
    }

    private boolean productExists(String id) {
        return productRepository.findById(id).isPresent();
    }

}
