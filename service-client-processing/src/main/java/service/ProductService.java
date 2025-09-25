package service;

import dto.ProductRequestDto;
import dto.ProductResponseDto;
import entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    ProductResponseDto createProduct(ProductRequestDto req);
    ProductResponseDto getProduct(Long id);
    List<ProductResponseDto> getAllProducts();
    ProductResponseDto updateProduct(Long id, ProductRequestDto req);
    void deleteProduct(Long id);
}
