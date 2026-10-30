package sigelab.service;

import sigelab.enums.EstadoEquipo;
import sigelab.exception.BusinessException;
import sigelab.model.Equipo;
import sigelab.qr.LectorQR;
import sigelab.repository.EquipoEstadoRepository;

import java.io.File;

/**
 * Identifica equipos a partir de un QR. RN-19: la lectura identifica, NUNCA modifica estados.
 * Decidir si el equipo puede prestarse sigue siendo responsabilidad de PrestamoService.
 */
public class QrEquipoService {

    private final LectorQR lector;
    private final EquipoEstadoRepository equipoRepository;

    public QrEquipoService() {
        this(new LectorQR(), new EquipoEstadoRepository());
    }

    public QrEquipoService(LectorQR lector, EquipoEstadoRepository equipoRepository) {
        this.lector = lector;
        this.equipoRepository = equipoRepository;
    }

    /** Lee la imagen, obtiene el código y consulta el equipo real en SQL Server. */
    public Equipo identificarDesdeImagen(File imagen) {
        return identificarPorCodigo(lector.leer(imagen));
    }

    public Equipo identificarPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new BusinessException("El código leído está vacío.");
        }
        String limpio = codigo.trim();
        return equipoRepository.buscarPorCodigo(limpio)
                .orElseThrow(() -> new BusinessException(
                        "El código " + limpio + " no corresponde a ningún equipo registrado."));
    }

    /** Solo informa: sirve para que la pantalla muestre "disponible" o "bloqueado". */
    public boolean puedeAgregarseAPrestamo(Equipo equipo) {
        return equipo != null && equipo.getEstado() == EstadoEquipo.DISPONIBLE;
    }
}
