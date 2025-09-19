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

import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clientId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(nullable = false)
    private Double interestRate;

    @Column(nullable = false)
    private Boolean isRecalc;

    @Column(nullable = false)
    private Boolean cardExist;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    @PrePersist
    public void prePersist() {
        if(balance == null) {
            balance = BigDecimal.ZERO;
        }
        if(interestRate == null) {
            interestRate = 0.0;
        }
        if (isRecalc == null) {
            isRecalc = false;
        }
        if (cardExist == null) {
            cardExist = false;
        }
        if (status == null) {
            status = AccountStatus.ACTIVE;
        }
    }
}
