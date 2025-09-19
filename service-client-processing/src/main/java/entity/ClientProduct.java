package entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "client_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String clientId;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private LocalDateTime openDate;

    private LocalDateTime closeDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClientProductStatus status;

    @PrePersist
    public void prePersist() {
        if(this.openDate == null) {
            this.openDate = LocalDateTime.now();
        }
        if(this.status == null) {
            this.status = ClientProductStatus.ACTIVE;
        }
    }
}
