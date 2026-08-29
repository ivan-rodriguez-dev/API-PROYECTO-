package com.comerciocontrol.api.service;

import com.comerciocontrol.api.dto.ProveedorRequest;
import com.comerciocontrol.api.exception.RecursoNoEncontradoException;
import com.comerciocontrol.api.exception.ReglaNegocioException;
import com.comerciocontrol.api.model.Proveedor;
import com.comerciocontrol.api.repository.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Reglas de negocio del modulo de proveedores. */
@Service
public class ProveedorService {

    private final ProveedorRepository proveedores;
    private final AuditoriaService auditoria;

    public ProveedorService(ProveedorRepository proveedores, AuditoriaService auditoria) {
        this.proveedores = proveedores;
        this.auditoria = auditoria;
    }

    public List<Proveedor> listar() {
        return proveedores.listar();
    }

    public Proveedor obtener(int id) {
        return proveedores.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("proveedor", id));
    }

    @Transactional
    public Proveedor crear(ProveedorRequest peticion, int usuarioId) {
        validarNitUnico(peticion.nit(), null);
        int id = proveedores.crear(peticion);
        auditoria.registrar(usuarioId, "CREAR_PROVEEDOR", "proveedores",
                "Registro al proveedor " + peticion.razonSocial());
        return obtener(id);
    }

    @Transactional
    public Proveedor actualizar(int id, ProveedorRequest peticion, int usuarioId) {
        obtener(id);
        validarNitUnico(peticion.nit(), id);
        proveedores.actualizar(id, peticion);
        auditoria.registrar(usuarioId, "ACTUALIZAR_PROVEEDOR", "proveedores",
                "Actualizo el proveedor id " + id);
        return obtener(id);
    }

    @Transactional
    public void desactivar(int id, int usuarioId) {
        Proveedor proveedor = obtener(id);
        proveedores.desactivar(id);
        auditoria.registrar(usuarioId, "DESACTIVAR_PROVEEDOR", "proveedores",
                "Dio de baja al proveedor " + proveedor.razonSocial());
    }

    private void validarNitUnico(String nit, Integer idExcluido) {
        if (proveedores.existeNit(nit, idExcluido)) {
            throw new ReglaNegocioException("Ya existe un proveedor con el NIT " + nit);
        }
    }
}
