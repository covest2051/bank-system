package dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDto {
    @NotBlank
    private String name;

    @NotBlank
    private String productKey;

    @NotBlank
    private LocalDateTime createDate;

    @NotBlank
    private String productId;
}
