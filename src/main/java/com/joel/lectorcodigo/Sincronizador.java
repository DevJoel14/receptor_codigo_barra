package com.joel.lectorcodigo;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.joel.lectorcodigo.conexion.Conexion;
import com.joel.lectorcodigo.datos.Mensaje;
import com.joel.lectorcodigo.server.SockeServer;
import com.joel.lectorcodigo.utils.ETipoMensaje;
import com.joel.lectorcodigo.datos.MensajeEvento;
import com.joel.lectorcodigo.datos.MensajeManejador;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.UUID;

public class Sincronizador implements Initializable, MensajeEvento {

    @FXML
    private Label lblFechaTokenGenerado;

    @FXML
    private ImageView imgQrApp;

    @FXML
    private TextArea txaHistorialCod;
    private SystemTray systemTray;
    private TrayIcon trayIcon;
    private QRCodeWriter qrWriter;

    public Sincronizador() {
        qrWriter = new QRCodeWriter();
        MensajeManejador.registerListener(this);
        systemTray = SystemTray.getSystemTray();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try (Statement stmt = Conexion.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT puerto, token, fecha FROM token ORDER BY fecha DESC LIMIT 1")) {

            String ip = obtenerIpLocal();
            String token;
            int puerto = 0;
            String fecha = String.valueOf(LocalDate.now());

            if (rs.next()) {
                puerto = rs.getInt("puerto");
                token = rs.getString("token");
                fecha = rs.getTimestamp("fecha").toString();
                generarQr(ip, puerto, token);
            } else {
                System.out.println("No hay token en la db");
                puerto = 7070;
                token = UUID.randomUUID().toString().substring(0, 8);
                generarQr(ip, puerto, token);
            }

            lblFechaTokenGenerado.setText(fecha);

            SockeServer sockeServer = new SockeServer();
            sockeServer.startGo(puerto, ip, token);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void generarQr(String ip, int puerto, String token) {

        String conexcion = null;
        conexcion = String.format("ws://%s:%d/ws?token=%s", ip, puerto, token);

        BitMatrix bitMatrix = null;

        try {
            bitMatrix = qrWriter.encode(conexcion, BarcodeFormat.QR_CODE, 300, 300);
        } catch (WriterException e) {
            System.out.println("mensaje " + e.getMessage());
            throw new RuntimeException(e);
        }

        BufferedImage qrBufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        imgQrApp.setImage(SwingFXUtils.toFXImage(qrBufferedImage, null));

        Mensaje mensaje = new Mensaje();
        mensaje.setTipoMensaje(ETipoMensaje.CONECCION.name());
        MensajeManejador.notify(mensaje);
    }

    @FXML
    private void ResetearToken(ActionEvent event) {
        String ip = obtenerIpLocal();
        int puerto = 7070;
        String token = UUID.randomUUID().toString().substring(0, 8);

        generarQr(ip, puerto, token);
    }

    @FXML
    private void hideBandejaSistema(ActionEvent actionEvent) {
        aplicarSystem();
    }

    private String obtenerIpLocal() {
        try {
            InetAddress inetAddress = java.net.InetAddress.getLocalHost();
            return inetAddress.getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
            return "localhost";
        }
    }


    @Override
    public void onMensaje(Mensaje event) {
        // si habilito mostrar historial si no no
        if(event.getTipoMensaje().equals(ETipoMensaje.MENSAJE.name())) {
            if(!this.txaHistorialCod.getText().isEmpty()) {
                this.txaHistorialCod.setText(this.txaHistorialCod.getText().concat("\n").concat(event.getTextoCode()));
            }
            escribirCodigo(event.getTextoCode());
        }
    }

    private void aplicarSystem() {

        if (!SystemTray.isSupported()) {
            System.out.println("System tray no soportado.");
            return;
        }

        Stage stage = CodeBarApp.getStageApp();

        try {
            if (stage != null) {
                javafx.application.Platform.runLater(() -> stage.hide());
            }

            final PopupMenu popup = new PopupMenu("Menú");
            trayIcon = new TrayIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/barcode.png"))).getImage(), "Capturadora", popup);
            trayIcon.setImageAutoSize(true);

            MenuItem restaurar = new MenuItem("Restaurar");

            restaurar.addActionListener(e -> {
                Platform.runLater(() -> stage.show());
            });

            // Click izquierdo sobre el icono también abre ventana
            trayIcon.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        Platform.runLater(() -> {
                            if (stage != null) {
                                stage.show();
                                stage.toFront();
                            }
                        });
                    }
                }
            });

            MenuItem salir = new MenuItem("Salir");
            salir.addActionListener(e -> {
                SystemTray.getSystemTray().remove(trayIcon);
                System.exit(0);
            });

            popup.add(restaurar);
            popup.addSeparator();
            popup.add(salir);

            trayIcon.setPopupMenu(popup);
            systemTray.add(trayIcon);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void escribirCodigo(String codigo) {
        try {
            java.awt.Robot robot = new java.awt.Robot();

            for (char c : codigo.toCharArray()) {
                int keyCode = java.awt.event.KeyEvent.getExtendedKeyCodeForChar(c);

                if (KeyEvent.CHAR_UNDEFINED == keyCode) {
                    System.out.println("No se puede escribir: " + c);
                    continue;
                }

                // Si es mayúscula, activa Shift
                boolean isUpperCase = Character.isUpperCase(c);
                if (isUpperCase) {
                    robot.keyPress(KeyEvent.VK_SHIFT);
                }

                robot.keyPress(keyCode);
                robot.keyRelease(keyCode);

                if (isUpperCase) {
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                }
            }

            // Enter opcional
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
