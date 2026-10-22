package com.joel.lectorcodigo.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class Conexion {
    private SocketChannel socketChannel;
    private ClienteController clienteController;

    public Conexion() {
        try {
            socketChannel = SocketChannel.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ClienteController startGo(final int puerto, final String ipServer, String nickJugador, final String uuid) {

        try {

            socketChannel.connect(new InetSocketAddress(ipServer, puerto));  // Se conecta al servidor en el puerto proporcionado.  192.168.100.9
            socketChannel.configureBlocking(true);  // Configura el canal para operar en modo bloqueante.
            System.out.println("Conectado a la primera. Soy CLIENTE.");

        } catch (IOException e) {

            System.out.println("No hay servidor. Yo seré el SERVIDOR.");
            // TODO: MOSTRAR UNA ALERTA DE MENSAJE : SERVIDOR CREANDO....
            new Thread(() -> new ServerController().start(puerto, ipServer)) {{
                setDaemon(true);
            }}.start();

            try {

                Thread.sleep(2000);
                socketChannel = SocketChannel.open();  // crear nuevo SocketChannel para reconectar
                socketChannel.connect(new InetSocketAddress(ipServer, puerto));
                socketChannel.configureBlocking(true);
                System.out.println("Ahora me conecté como CLIENTE después de crear el servidor.");

            } catch (Exception ex) {
                System.out.println("Error al reconectar como cliente.");
                ex.printStackTrace();
            }

        }

        return clienteController;
    }
}
