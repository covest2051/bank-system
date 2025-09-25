package repository;

import entity.ProductRegistry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRegistryRepository extends JpaRepository<ProductRegistry, Long> {
    List<ProductRegistry> findByClientId(Long clientId);
}
