package sigelab.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import sigelab.exception.BusinessException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/** Genera el QR de un equipo. El contenido es únicamente su código de inventario. */
public class GeneradorQR {

    private static final int TAMANO_POR_DEFECTO = 300;

    /** Devuelve la imagen para mostrarla en Swing (por ejemplo, en un JLabel). */
    public BufferedImage generar(String codigoInventario, int tamano) {
        if (codigoInventario == null || codigoInventario.isBlank()) {
            throw new BusinessException("El código de inventario es obligatorio para generar el QR.");
        }
        if (tamano <= 0) {
            throw new BusinessException("El tamaño del QR debe ser mayor que cero.");
        }
        try {
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.CHARACTER_SET, "UTF-8",
                    EncodeHintType.MARGIN, 2);
            BitMatrix matriz = new QRCodeWriter()
                    .encode(codigoInventario.trim(), BarcodeFormat.QR_CODE, tamano, tamano, hints);
            return MatrixToImageWriter.toBufferedImage(matriz);
        } catch (WriterException e) {
            throw new BusinessException("No se pudo generar el código QR para " + codigoInventario + ".");
        }
    }

    /** Guarda el QR como archivo PNG. */
    public File guardarPng(String codigoInventario, File destino) {
        BufferedImage imagen = generar(codigoInventario, TAMANO_POR_DEFECTO);
        try {
            File carpeta = destino.getAbsoluteFile().getParentFile();
            if (carpeta != null) {
                carpeta.mkdirs();
            }
            ImageIO.write(imagen, "PNG", destino);
            return destino;
        } catch (IOException e) {
            throw new BusinessException("No se pudo guardar la imagen del QR.");
        }
    }
}
