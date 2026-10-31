package sigelab.controller;

import sigelab.model.Equipo;
import sigelab.model.Prestamo;
import sigelab.seguridad.SesionActual;
import sigelab.service.PrestamoService;
import sigelab.service.QrEquipoService;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class PrestamoController {

    private final QrEquipoService qrService = new QrEquipoService();
    private final PrestamoService prestamoService = new PrestamoService();

    public Equipo identificarPorCodigo(String codigo) {
        return qrService.identificarPorCodigo(codigo);
    }

    public Equipo identificarPorImagen(File imagenQr) {
        return qrService.identificarDesdeImagen(imagenQr);
    }

    public boolean puedeAgregarse(Equipo equipo) {
        return qrService.puedeAgregarseAPrestamo(equipo);
    }

    public Prestamo confirmar(String solicitante, LocalDate fechaEsperada,
                              List<Integer> idsEquipo, String observaciones) {
        return prestamoService.confirmarPrestamo(solicitante, SesionActual.getUsuario(),
                fechaEsperada, idsEquipo, observaciones);
    }
}
