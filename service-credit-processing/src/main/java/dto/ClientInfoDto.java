package dto;

import lombok.Data;

@Data
public class ClientInfoDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String documentNumber;
}

