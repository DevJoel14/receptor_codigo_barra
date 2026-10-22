package com.joel.lectorcodigo.server;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class SockeServer {
    private SocketChannel socketChannel;

    public SockeServer() {
        try {
            socketChannel = SocketChannel.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void startGo(final int puerto, final String ipServer, String token) {

        /*try {
            socketChannel.connect(new InetSocketAddress(ipServer, puerto));  // Se conecta al servidor en el puerto proporcionado.  192.168.100.9
            socketChannel.configureBlocking(true);  // Configura el canal para operar en modo bloqueante.
            System.out.println("Conectado a la primera. Soy CLIENTE.");
            //clienteController = new ClienteController(socketChannel).start(puerto, ipServer, token, uuid);

        } catch (IOException e) {*/

        System.out.println("No hay servidor. Yo seré el SERVIDOR.");
        // TODO: MOSTRAR UNA spinner : SERVIDOR CREANDO....
        new Thread(() -> new ServerController().start(puerto, ipServer)) {{
            setDaemon(true);
        }}.start();

            /*try {

                Thread.sleep(2000);
                socketChannel = SocketChannel.open();  // crear nuevo SocketChannel para reconectar
                socketChannel.connect(new InetSocketAddress(ipServer, puerto));
                socketChannel.configureBlocking(true);
                //clienteController = new ClienteController(socketChannel).start(puerto, ipServer, token, uuid);

            } catch (Exception ex) {
                System.out.println("Error al reconectar como cliente.");
                ex.printStackTrace();
            }*/
    }

//}
}
