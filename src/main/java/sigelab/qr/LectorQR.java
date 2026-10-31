package sigelab.qr;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import sigelab.exception.BusinessException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/** Lee un código QR desde una imagen y devuelve el texto que contiene. No consulta la base de datos. */
public class LectorQR {

    public String leer(File archivo) {
        if (archivo == null || !archivo.isFile()) {
            throw new BusinessException("Seleccione un archivo de imagen válido.");
        }
        try {
            BufferedImage imagen = ImageIO.read(archivo);
            if (imagen == null) {
                throw new BusinessException("El archivo seleccionado no es una imagen compatible.");
            }
            return leer(imagen);
        } catch (IOException e) {
            throw new BusinessException("No se pudo abrir la imagen seleccionada.");
        }
    }

    public String leer(BufferedImage imagen) {
        LuminanceSource fuente = new BufferedImageLuminanceSource(imagen);
        BinaryBitmap mapa = new BinaryBitmap(new HybridBinarizer(fuente));
        Map<DecodeHintType, Object> hints = Map.of(
                DecodeHintType.TRY_HARDER, Boolean.TRUE,
                DecodeHintType.POSSIBLE_FORMATS, List.of(BarcodeFormat.QR_CODE));
        try {
            return new QRCodeReader().decode(mapa, hints).getText().trim();
        } catch (ReaderException e) {
            throw new BusinessException("No se encontró un código QR legible en la imagen.");
        }
    }
}
