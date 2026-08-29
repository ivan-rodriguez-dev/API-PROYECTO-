package com.comerciocontrol.api.service;

import com.comerciocontrol.api.dto.ClienteRequest;
import com.comerciocontrol.api.exception.RecursoNoEncontradoException;
import com.comerciocontrol.api.exception.ReglaNegocioException;
import com.comerciocontrol.api.model.Cliente;
import com.comerciocontrol.api.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Reglas de negocio del modulo de clientes. */
@Service
public class ClienteService {

    private final ClienteRepository clientes;
    private final AuditoriaService auditoria;

    public ClienteService(ClienteRepository clientes, AuditoriaService auditoria) {
        this.clientes = clientes;
        this.auditoria = auditoria;
    }

    public List<Cliente> listar(String busqueda) {
        return clientes.listar(busqueda);
    }

    public Cliente obtener(int id) {
        return clientes.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("cliente", id));
    }

    @Transactional
    public Cliente crear(ClienteRequest peticion, int usuarioId) {
        validarCedulaUnica(peticion.cedula(), null);
        int id = clientes.crear(peticion);
        auditoria.registrar(usuarioId, "CREAR_CLIENTE", "clientes",
                "Registro al cliente " + peticion.nombre());
        return obtener(id);
    }

    @Transactional
    public Cliente actualizar(int id, ClienteRequest peticion, int usuarioId) {
        obtener(id);
        validarCedulaUnica(peticion.cedula(), id);
        clientes.actualizar(id, peticion);
        auditoria.registrar(usuarioId, "ACTUALIZAR_CLIENTE", "clientes",
                "Actualizo el cliente id " + id);
        return obtener(id);
    }

    /** Baja logica: el historial de ventas del cliente debe conservarse. */
    @Transactional
    public void desactivar(int id, int usuarioId) {
        Cliente cliente = obtener(id);
        clientes.desactivar(id);
        auditoria.registrar(usuarioId, "DESACTIVAR_CLIENTE", "clientes",
                "Dio de baja al cliente " + cliente.nombre());
    }

    private void validarCedulaUnica(String cedula, Integer idExcluido) {
        if (clientes.existeCedula(cedula, idExcluido)) {
            throw new ReglaNegocioException("Ya existe un cliente con la cedula " + cedula);
        }
    }
}
