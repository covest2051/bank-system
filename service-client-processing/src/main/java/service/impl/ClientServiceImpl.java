package service.impl;

import dto.ClientRequestDto;
import dto.ClientResponseDto;
import dto.ProductResponseDto;
import entity.Client;
import entity.DocumentType;
import entity.Product;
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
    public ClientResponseDto registerClient(ClientRequestDto req) {
        DocumentType docType = req.getDocumentType();
        String docId = String.valueOf(req.getDocumentId());

        boolean blacklisted = blacklistRegistryRepository.existsActiveByDocumentTypeAndDocumentId(String.valueOf(docType), docId);
        if (blacklisted) {
            throw new BlacklistedException("Client with document " + docType + ":" + docId + " is in black list");
        }
        Optional<Client> existingClientOpt = clientRepository.findByDocumentTypeAndDocumentId(String.valueOf(docType), docId);
        if (existingClientOpt.isPresent()) {
            throw new ClientAlreadyExistsException("Client is already registered");
        }

        Client clientForSave = Client.builder()
                .firstName(req.getFirstName())
                .middleName(req.getMiddleName())
                .lastName(req.getLastName())
                .dateOfBirth(req.getDateOfBirth())
                .documentType(DocumentType.valueOf(String.valueOf(docType)))
                .documentId(req.getDocumentId())
                .build();

        Client savedClient = clientRepository.save(clientForSave);

        userRepository.findByEmail(req.getEmail()).ifPresent(u -> {
            throw new ClientAlreadyExistsException("User with this email already exists");
        });

        User user = User.builder()
                .login(req.getEmail())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .build();
        User savedUser = userRepository.save(user);

        savedClient.setUserId(savedUser.getId());
        clientRepository.save(savedClient);

        return mapToClientResponse(savedClient);
    }

    private ClientResponseDto mapToClientResponse(Client c) {
        return ClientResponseDto.builder()
                .id(c.getId())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .documentType(String.valueOf(c))
                .documentId(c.getDocumentId())
                .userId(c.getUserId())
                .build();
    }
}
