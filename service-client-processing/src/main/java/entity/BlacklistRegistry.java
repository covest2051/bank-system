package entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blacklist_registry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlacklistRegistry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType;

    @Column(name = "document_id", nullable = false, length = 100)
    private String documentId;

    @Column(name = "blacklisted_at", nullable = false)
    private LocalDateTime blacklistedAt;

    @Column(length = 500)
    private String reason;

    @Column(name = "blacklist_expiration_date")
    private LocalDateTime blacklistExpirationDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (blacklistedAt == null) {
            blacklistedAt = LocalDateTime.now();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
