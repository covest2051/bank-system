package service;

import dto.ClientRequestDto;
import dto.ClientResponseDto;
import entity.Client;

import java.util.List;

public interface ClientService {
    List<ClientResponseDto> registerClients(List<Client> clients);
    ClientResponseDto registerClient(ClientRequestDto clientForRegistration);
}
