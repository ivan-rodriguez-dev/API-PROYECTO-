package com.comerciocontrol.api.service;

import com.comerciocontrol.api.model.Auditoria;
import com.comerciocontrol.api.repository.AuditoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Registro y consulta de la traza de auditoria.
 *
 * <p>Toda operacion que modifica datos deja una huella con el usuario responsable,
 * tal como exige el requerimiento no funcional de trazabilidad del proyecto.</p>
 */
@Service
public class AuditoriaService {

    private final AuditoriaRepository repositorio;

    public AuditoriaService(AuditoriaRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void registrar(int usuarioId, String accion, String tabla, String detalle) {
        repositorio.registrar(usuarioId, accion, tabla, detalle);
    }

    public List<Auditoria> listar(String tabla, Integer usuarioId, int limite) {
        return repositorio.listar(tabla, usuarioId, Math.min(Math.max(limite, 1), 500));
    }
}
