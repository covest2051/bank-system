package service;

import dto.ClientCreateRequestDto;
import dto.ClientResponseDto;
import entity.Client;

import java.util.List;

public interface ClientService {
    List<Client> registerClients(List<Client> clients);

    ClientResponseDto registerClient(ClientCreateRequestDto clientForRegistration);
}
