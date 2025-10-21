package service.impl;

import aop.repository.RoleRepository;
import dto.ClientEvent;
import dto.ClientRequestDto;
import dto.ClientResponseDto;
import entity.Client;
import entity.DocumentType;
import entity.Role;
import entity.RoleEnum;
import entity.User;
import exception.BlacklistedException;
import exception.ClientAlreadyExistsException;
import exception.UserBlacklistedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.BlacklistRegistryRepository;
import repository.ClientRepository;
import repository.UserRepository;
import service.ClientService;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ClientServiceImpl implements ClientService {
    private static final String TOPIC_CLIENT_CARDS = "client_cards";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final BlacklistRegistryRepository blacklistRegistryRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public List<ClientResponseDto> registerClients(List<Client> clients) {
        return clients.stream()
                .map(client -> {
                    ClientRequestDto clientRequestDto = ClientRequestDto.mapClientToDto(client);
                    return registerClient(clientRequestDto);
        })
        .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ClientResponseDto registerClient(ClientRequestDto req) {
        DocumentType docType = req.getDocumentType();
        String docId = String.valueOf(req.getDocumentId());

        User user = new User();

        boolean blacklisted = blacklistRegistryRepository.existsActiveByDocumentTypeAndDocumentId(String.valueOf(docType), docId);
        if (blacklisted) {
            Role blockedRole = roleRepository.findByName(RoleEnum.BLOCKED_CLIENT)
                    .orElseGet(() -> roleRepository.save(new Role(RoleEnum.BLOCKED_CLIENT)));

            user.setRoles(Set.of(blockedRole));
            userRepository.save(user);

            throw new UserBlacklistedException("User is blacklisted");
        }
        Role currentClientRole = roleRepository.findByName(RoleEnum.CURRENT_CLIENT)
                .orElseGet(() -> roleRepository.save(new Role(RoleEnum.CURRENT_CLIENT)));

        Optional<Client> existingClientOpt = clientRepository.findByDocumentTypeAndDocumentId(docType, docId);
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

        user = User.builder()
                .login(req.getEmail())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .roles(Set.of(currentClientRole))
                .build();
        User savedUser = userRepository.save(user);

        savedClient.setUserId(savedUser.getId());
        clientRepository.save(savedClient);

        return mapToClientResponse(savedClient);
    }

    public void createCardForClient(Client c) {
        sendEvent(c);
    }

    private ClientResponseDto mapToClientResponse(Client c) {
        return ClientResponseDto.builder()
                .id(c.getId())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .documentType(String.valueOf(c.getDocumentType()))
                .documentId(c.getDocumentId())
                .userId(c.getUserId())
                .build();
    }

    private void sendEvent(Client c) {
        String topic = TOPIC_CLIENT_CARDS;

        ClientEvent event = ClientEvent.builder()
                .eventType("CREATE_CARD_REQUEST")
                .clientId(c.getId())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .build();

        try {
            kafkaTemplate.send(topic, String.valueOf(c.getId()), event);
            log.info("Sent event to topic {} for clientId={}, op={}", topic, c.getClientId(), "CREATE_CARD_REQUEST");
        } catch (Exception e) {
            log.error("Failed to send event to topic {} for clientId={}, op={}: {}", topic, c.getClientId(), "CREATE_CARD_REQUEST", e.getMessage());
        }
    }
}
