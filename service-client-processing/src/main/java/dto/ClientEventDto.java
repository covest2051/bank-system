package dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientEventDto {
    private String eventType;
    private Long clientId;
    private String firstName;
    private String lastName;
}
