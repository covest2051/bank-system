package dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String documentType;
    private String documentId;
    private Long userId;
}
