package dto;

import entity.DocumentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientCreateRequestDto {

    @NotBlank
    private String firstName;

    private String middleName;

    @NotBlank
    private String lastName;

    @NotNull
    @Past
    private LocalDate dateOfBirth;

    @NotBlank
    private DocumentType documentType;

    @NotBlank
    private String documentId;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}