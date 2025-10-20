package service;

import entity.ClientProduct;

import java.util.List;

public interface ClientProductService {
    ClientProduct create(ClientProduct cp);
    ClientProduct update(Long id, ClientProduct cp);
    void delete(Long id);
    ClientProduct getById(Long id);
}