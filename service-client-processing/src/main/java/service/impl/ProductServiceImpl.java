package service.impl;

import dto.ProductRequestDto;
import dto.ProductResponseDto;
import entity.Product;
import entity.ProductType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import repository.ProductRepository;
import service.ProductService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Override
    public ProductResponseDto createProduct(ProductRequestDto req) {
        productRepository.findByProductKey(req.getProductKey()).ifPresent(p -> {
            throw new IllegalArgumentException("Product key already exists");
        });

        Product productForSave = Product.builder()
                .name(req.getName())
                .createDate(req.getCreateDate())
                .productKey(ProductType.valueOf(req.getProductKey()))
                .productId(req.getProductId())
                .build();

        Product savedProduct = productRepository.save(productForSave);

        return mapToProductResponse(savedProduct);
    }

    @Override
    public ProductResponseDto getProduct(Long id) {
        return productRepository.findById(id).map(this::mapToProductResponse).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Override
    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findAll().stream().map(this::mapToProductResponse).collect(Collectors.toList());
    }

    @Override
    public ProductResponseDto updateProduct(Long id, ProductRequestDto req) {
        Product productForUpdate = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));

        productForUpdate.setName(req.getName());
        productForUpdate.setProductId(req.getProductId());

        Product updatedProduct = productRepository.save(productForUpdate);

        return mapToProductResponse(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) throw new RuntimeException("Product not found");
        productRepository.deleteById(id);
    }

    private ProductResponseDto mapToProductResponse(Product p) {
        return ProductResponseDto.builder()
                .id(p.getId())
                .name(p.getName())
                .productKey(String.valueOf(p.getProductKey()))
                .createDate(p.getCreateDate())
                .productId(p.getProductId())
                .build();
    }
}
