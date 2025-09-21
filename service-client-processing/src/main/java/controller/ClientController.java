package controller;

import entity.Client;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/clients")
public class ClientController {
    private final ClientDTO clientDTO;
    private final ClientValidator clientValidator;
    private final ClientService clientService;

    public ClientController(ClientDTO clientDAO, ClientValidator clientValidator, ClientService clientService) {
        this.clientDTO = clientDAO;
        this.clientValidator = clientValidator;
        this.clientService = clientService;
    }

    @PostMapping("/register")
    public String newPerson(@ModelAttribute("client") Client client) {
        return "clients/register";
    }
}
