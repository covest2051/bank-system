package dto;

import entity.ClientProductStatus;
import entity.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientProductDto {
    private String clientId;
    private String productId;
    private ProductType productType;
    private ClientProductStatus status;
    private LocalDateTime openDate;
    private LocalDateTime closeDate;
}
