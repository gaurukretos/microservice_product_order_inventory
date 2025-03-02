package com.product.productservice;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.assertions.Assertions;
import com.product.productservice.dto.ProductDto;
import com.product.productservice.dto.ProductResponse;
import com.product.productservice.model.Product;
import com.product.productservice.repository.ProductRepository;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class ProductserviceApplicationTests {

	@Autowired
	private ProductRepository productRepository;

	@Container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {

		dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}

	@Test
	void shouldCreateProduct() throws Exception {
		ProductDto dto = getProductRequest();
		String dtoString = objectMapper.writeValueAsString(dto);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(dtoString))
				.andExpect(status().isCreated());

		Assertions.assertTrue(productRepository.findAll().size() == 1);

	}

	private ProductDto getProductRequest() {
		return ProductDto.builder().name("iPhone15").description("this i phone").price(BigDecimal.valueOf(120000))
				.build();
	}

	@Test
	void shouldGetProductById() throws Exception {
		ProductDto dto = getProductRequest();

		String dtoString = objectMapper.writeValueAsString(dto);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(dtoString))
				.andExpect(status().isCreated());

		String productId = waitForProductToBeSaved();

		mockMvc.perform(MockMvcRequestBuilders.get("/api/products/" + productId)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	private String waitForProductToBeSaved() throws InterruptedException {
		int retries = 5;
		while (retries-- > 0) {
			if (!productRepository.findAll().isEmpty()) {
				return productRepository.findAll().get(0).getId();
			}
			Thread.sleep(500);
		}
		throw new RuntimeException("Product was not saved!");
	}

	@Test
	void shouldUpdateProduct() throws Exception {
		ProductDto productDto = getProductRequest();
		String productDt = objectMapper.writeValueAsString(productDto);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(productDt))
				.andExpect(status().isCreated());

		String productId = waitForProductToBeSaved();

		ProductResponse updatedDto = new ProductResponse();
		updatedDto.setName("Updated iPhone");
		updatedDto.setDescription("Updated description");
		updatedDto.setPrice(BigDecimal.valueOf(130000));

		String updatedDtoString = objectMapper.writeValueAsString(updatedDto);
		mockMvc.perform(MockMvcRequestBuilders.put("/api/products/" + productId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(updatedDtoString))
				.andExpect(status().isOk());

		Product updatedProduct = productRepository.findById(productId).orElseThrow();
		assertEquals("Updated iPhone", updatedProduct.getName());
		assertEquals("Updated description", updatedProduct.getDescription());
		assertEquals(BigDecimal.valueOf(130000), updatedProduct.getPrice());

	}

	@Test
	void shouldDeleteProduct() throws Exception {
		ProductDto productDto = getProductRequest();
		String productJson = objectMapper.writeValueAsString(productDto);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(productJson))
				.andExpect(status().isCreated());

		String productId = waitForProductToBeSaved();

		mockMvc.perform(MockMvcRequestBuilders.delete("/api/products/" + productId))
				.andExpect(status().isNoContent());

		boolean exists = productRepository.findById(productId).isPresent();
		assertEquals(false, exists, "Product should be deleted from the database");
	}

}