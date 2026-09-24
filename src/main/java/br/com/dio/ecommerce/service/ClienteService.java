package br.com.dio.ecommerce.service;

import br.com.dio.ecommerce.dto.ClienteRequest;
import br.com.dio.ecommerce.dto.ClienteResponse;
import br.com.dio.ecommerce.exception.RecursoNaoEncontradoException;
import br.com.dio.ecommerce.exception.RegraNegocioException;
import br.com.dio.ecommerce.model.Cliente;
import br.com.dio.ecommerce.repository.ClienteRepository;
import br.com.dio.ecommerce.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;

    public ClienteService(ClienteRepository clienteRepository, PedidoRepository pedidoRepository) {
        this.clienteRepository = clienteRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clienteRepository.findAll().stream().map(ClienteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscar(Long id) {
        return ClienteResponse.from(buscarEntidade(id));
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest request) {
        if (clienteRepository.existsByEmailIgnoreCase(request.email())) {
            throw new RegraNegocioException("E-mail já cadastrado");
        }
        if (clienteRepository.existsByCpf(request.cpf())) {
            throw new RegraNegocioException("CPF já cadastrado");
        }
        Cliente cliente = new Cliente(request.nome(), request.email(), request.cpf());
        return ClienteResponse.from(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest request) {
        Cliente cliente = buscarEntidade(id);
        if (clienteRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), id)) {
            throw new RegraNegocioException("E-mail já cadastrado");
        }
        if (clienteRepository.existsByCpfAndIdNot(request.cpf(), id)) {
            throw new RegraNegocioException("CPF já cadastrado");
        }
        cliente.setNome(request.nome());
        cliente.setEmail(request.email());
        cliente.setCpf(request.cpf());
        return ClienteResponse.from(cliente);
    }

    @Transactional
    public void excluir(Long id) {
        Cliente cliente = buscarEntidade(id);
        if (pedidoRepository.existsByClienteId(id)) {
            throw new RegraNegocioException("Cliente possui pedidos e não pode ser excluído");
        }
        clienteRepository.delete(cliente);
    }

    Cliente buscarEntidade(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
    }
}
