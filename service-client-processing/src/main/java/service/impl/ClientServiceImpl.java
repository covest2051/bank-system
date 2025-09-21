package service.impl;

import dto.ClientCreateRequestDto;
import dto.ClientResponseDto;
import entity.Client;
import entity.DocumentType;
import entity.User;
import exception.BlacklistedException;
import exception.ClientAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import repository.BlacklistRegistryRepository;
import repository.ClientRepository;
import repository.UserRepository;
import service.ClientService;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final BlacklistRegistryRepository blacklistRegistryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<Client> registerClients(List<Client> clients) {
        return null;
    }

    @Override
    public ClientResponseDto registerClient(ClientCreateRequestDto clientForRegistration) {
        DocumentType docType = clientForRegistration.getDocumentType();
        String docId = String.valueOf(clientForRegistration.getDocumentId());

        boolean blacklisted = blacklistRegistryRepository.existsActiveByDocumentTypeAndDocumentId(String.valueOf(docType), docId);
        if (blacklisted) {
            throw new BlacklistedException("Client with document " + docType + ":" + docId + " is in black list");
        }
        Optional<Client> existingClientOpt = clientRepository.findByDocumentTypeAndDocumentId(String.valueOf(docType), docId);
        if (existingClientOpt.isPresent()) {
            throw new ClientAlreadyExistsException("Client is already registered");
        }

        Client clientForSave = Client.builder()
                .firstName(clientForRegistration.getFirstName())
                .middleName(clientForRegistration.getMiddleName())
                .lastName(clientForRegistration.getLastName())
                .dateOfBirth(clientForRegistration.getDateOfBirth())
                .documentType(DocumentType.valueOf(String.valueOf(docType)))
                .documentId(clientForRegistration.getDocumentId())
                .build();

        Client savedClient = clientRepository.save(clientForSave);

        userRepository.findByEmail(clientForRegistration.getEmail()).ifPresent(u -> {
            throw new ClientAlreadyExistsException("User with this email already exists");
        });

        User user = User.builder()
                .login(clientForRegistration.getEmail())
                .email(clientForRegistration.getEmail())
                .password(passwordEncoder.encode(clientForRegistration.getPassword()))
                .build();
        User savedUser = userRepository.save(user);

        savedClient.setUserId(savedUser.getId());
        clientRepository.save(savedClient);

        return ClientResponseDto.builder()
                .id(savedClient.getId())
                .firstName(savedClient.getFirstName())
                .lastName(savedClient.getLastName())
                .documentType(String.valueOf(docType))
                .documentId(savedClient.getDocumentId())
                .userId(savedClient.getUserId())
                .build();
    }
}
