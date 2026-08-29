package com.comerciocontrol.api.service;

import com.comerciocontrol.api.dto.AperturaCajaRequest;
import com.comerciocontrol.api.dto.CierreCajaRequest;
import com.comerciocontrol.api.exception.RecursoNoEncontradoException;
import com.comerciocontrol.api.exception.ReglaNegocioException;
import com.comerciocontrol.api.model.Caja;
import com.comerciocontrol.api.repository.CajaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Reglas de negocio de la caja diaria.
 *
 * <p>Regla principal del proyecto: <b>solo puede haber una caja abierta a la vez</b>,
 * y sin caja abierta no se pueden registrar ventas.</p>
 */
@Service
public class CajaService {

    private final CajaRepository cajas;
    private final AuditoriaService auditoria;

    public CajaService(CajaRepository cajas, AuditoriaService auditoria) {
        this.cajas = cajas;
        this.auditoria = auditoria;
    }

    public Optional<Caja> cajaAbierta() {
        return cajas.buscarAbierta();
    }

    /** Caja abierta o error 409 si no la hay. Lo usa el punto de venta. */
    public Caja exigirCajaAbierta() {
        return cajas.buscarAbierta().orElseThrow(() -> new ReglaNegocioException(
                "No hay una caja abierta. Abra la caja en POST /api/caja/apertura antes de registrar ventas"));
    }

    public List<Caja> listar() {
        return cajas.listar();
    }

    public Caja obtener(int id) {
        return cajas.buscarPorId(id).orElseThrow(() -> new RecursoNoEncontradoException("caja", id));
    }

    @Transactional
    public Caja abrir(AperturaCajaRequest peticion, int usuarioId) {
        cajas.buscarAbierta().ifPresent(c -> {
            throw new ReglaNegocioException("Ya existe una caja abierta (id " + c.id()
                    + ", fecha " + c.fecha() + "). Cierrela antes de abrir otra");
        });
        int id = cajas.abrir(LocalDate.now().toString(), peticion.apertura(), usuarioId,
                peticion.observacion());
        auditoria.registrar(usuarioId, "ABRIR_CAJA", "caja",
                "Abrio la caja id " + id + " con base " + peticion.apertura());
        return obtener(id);
    }

    /**
     * Cierra la caja abierta.
     *
     * <p>La diferencia se calcula como
     * {@code efectivo contado - (base de apertura + ventas del dia)}: si es negativa falta
     * dinero en caja y si es positiva sobra.</p>
     */
    @Transactional
    public Caja cerrar(CierreCajaRequest peticion, int usuarioId) {
        Caja abierta = exigirCajaAbierta();
        double ventasDia = cajas.totalVentasDeCaja(abierta.id());
        double esperado = abierta.apertura() + ventasDia;
        double diferencia = redondear(peticion.efectivoContado() - esperado);

        cajas.cerrar(abierta.id(), peticion.efectivoContado(), redondear(ventasDia), diferencia,
                peticion.observacion());

        auditoria.registrar(usuarioId, "CERRAR_CAJA", "caja",
                "Cerro la caja id " + abierta.id() + ". Esperado " + esperado
                        + ", contado " + peticion.efectivoContado() + ", diferencia " + diferencia);
        return obtener(abierta.id());
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
