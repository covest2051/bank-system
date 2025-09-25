package entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_registry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRegistry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clientId;

    private Long accountId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Double interestRate;

    @Column(nullable = false)
    private LocalDateTime openDate;

    @Column(nullable = false)
    private Integer monthCount;

    @PrePersist
    public void prePersist() {
        if (interestRate == null) {
            interestRate = 0.0;
        }
        if (openDate == null) {
            openDate = LocalDateTime.now();
        }
        if (monthCount == null) {
            monthCount = 0;
        }

    }
}
