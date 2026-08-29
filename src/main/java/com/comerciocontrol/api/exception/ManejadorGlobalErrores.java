package com.comerciocontrol.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduce cualquier excepcion de la aplicacion a una respuesta JSON uniforme.
 *
 * <p>Evita que el cliente reciba trazas de Java y garantiza que cada situacion
 * devuelva el codigo HTTP correcto.</p>
 */
@RestControllerAdvice
public class ManejadorGlobalErrores {

    /** 400 - la peticion no supero las validaciones de los DTO. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaError> validacion(MethodArgumentNotValidException ex,
                                                     HttpServletRequest req) {
        Map<String, String> detalles = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            detalles.put(fe.getField(), fe.getDefaultMessage());
        }
        return construir(HttpStatus.BAD_REQUEST, "Datos invalidos",
                "La peticion contiene campos que no cumplen las validaciones", req, detalles);
    }

    /** 400 - el cuerpo JSON esta mal formado o falta un parametro obligatorio. */
    @ExceptionHandler({HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class})
    public ResponseEntity<RespuestaError> peticionIncorrecta(Exception ex, HttpServletRequest req) {
        return construir(HttpStatus.BAD_REQUEST, "Peticion incorrecta",
                mensajeLegible(ex), req, null);
    }

    /** 401 - falta el token o no es valido. */
    @ExceptionHandler(NoAutenticadoException.class)
    public ResponseEntity<RespuestaError> noAutenticado(NoAutenticadoException ex,
                                                        HttpServletRequest req) {
        return construir(HttpStatus.UNAUTHORIZED, "No autenticado", ex.getMessage(), req, null);
    }

    /** 403 - el rol del usuario no tiene permiso sobre el recurso. */
    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<RespuestaError> accesoDenegado(AccesoDenegadoException ex,
                                                         HttpServletRequest req) {
        return construir(HttpStatus.FORBIDDEN, "Acceso denegado", ex.getMessage(), req, null);
    }

    /** 404 - el recurso no existe. */
    @ExceptionHandler({RecursoNoEncontradoException.class, NoHandlerFoundException.class})
    public ResponseEntity<RespuestaError> noEncontrado(Exception ex, HttpServletRequest req) {
        return construir(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), req, null);
    }

    /** 409 - una regla de negocio impide la operacion. */
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<RespuestaError> reglaNegocio(ReglaNegocioException ex,
                                                       HttpServletRequest req) {
        return construir(HttpStatus.CONFLICT, "Regla de negocio", ex.getMessage(), req, null);
    }

    /** 409 - la base de datos rechazo la operacion (unicidad, clave foranea, CHECK). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RespuestaError> integridad(DataIntegrityViolationException ex,
                                                     HttpServletRequest req) {
        String causa = ex.getMostSpecificCause().getMessage();
        String mensaje;
        if (causa != null && causa.contains("UNIQUE")) {
            mensaje = "Ya existe un registro con ese valor unico (codigo, cedula, NIT o usuario)";
        } else if (causa != null && causa.contains("FOREIGN KEY")) {
            mensaje = "El registro esta relacionado con otros datos o referencia un id inexistente";
        } else if (causa != null && causa.contains("CHECK")) {
            mensaje = "Un valor enviado no cumple las restricciones de la base de datos";
        } else {
            mensaje = "La operacion viola una restriccion de integridad de la base de datos";
        }
        return construir(HttpStatus.CONFLICT, "Conflicto de integridad", mensaje, req, null);
    }

    /** 500 - cualquier fallo no previsto. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> errorInterno(Exception ex, HttpServletRequest req) {
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrio un error inesperado procesando la peticion: " + mensajeLegible(ex),
                req, null);
    }

    private ResponseEntity<RespuestaError> construir(HttpStatus estado, String error, String mensaje,
                                                     HttpServletRequest req, Map<String, String> detalles) {
        return ResponseEntity.status(estado).body(
                RespuestaError.de(estado.value(), error, mensaje, req.getRequestURI(), detalles));
    }

    private String mensajeLegible(Exception ex) {
        String m = ex.getMessage();
        return (m == null || m.isBlank()) ? ex.getClass().getSimpleName() : m;
    }
}
