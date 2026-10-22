package com.joel.lectorcodigo.server;

import com.joel.lectorcodigo.datos.Mensaje;
import com.joel.lectorcodigo.utils.ETipoMensaje;
import com.joel.lectorcodigo.datos.MensajeEvento;
import com.joel.lectorcodigo.datos.MensajeManejador;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerController implements MensajeEvento {
    private final ExecutorService threadPool = Executors.newFixedThreadPool(2);

    public ServerController() {
        MensajeManejador.registerListener(this);
    }

    public void start(final int puerto, final String ipServer) {

        //final LinkedHashMap<String, SocketChannel> jugadores = new LinkedHashMap<>();

        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
             Selector selector = Selector.open()) {

            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(ipServer, puerto));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println(serverSocketChannel.getLocalAddress() + " localdireccion server creado");

            while (true) {
                if (selector.select() == 0) {
                    continue;
                }

                var selectedKeys = selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = selectedKeys.next();
                    selectedKeys.remove();

                    if (key.isAcceptable()) {
                        handleAceptar(key, selector);
                    } else if (key.isReadable()) {
                        handleLeer(key);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error de IO en el servidor: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            threadPool.shutdown();
            // cierro el cliente
        }
    }

    private void handleAceptar(SelectionKey key, Selector selector) throws IOException {
        // mostrar mensaje q fue vinculado exitosamente
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel cliente = server.accept();

        if (cliente != null) {
            cliente.configureBlocking(false);
            cliente.register(selector, SelectionKey.OP_READ);
            System.out.println("CONECTADO cliente codebar AL SERVER: " + cliente.getRemoteAddress());

            Mensaje mensaje = new Mensaje();
            mensaje.setTipoMensaje(ETipoMensaje.CONECCION.name());

            byte[] mensajeBytes = serializarMensaje(mensaje);

            ByteBuffer buffer = ByteBuffer.wrap(mensajeBytes);
            while (buffer.hasRemaining()) {
                cliente.write(buffer);
            }

        }
    }

    private void handleLeer(SelectionKey key) {

        SocketChannel cliente = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        try {
            int bytesRead = cliente.read(buffer);
            if (bytesRead == -1) {
                System.out.println("DESCONECTADO DEL SERVIDOR: " + cliente.getRemoteAddress());
                cliente.close();
                return;
            }

            buffer.flip();
            byte[] data = new byte[bytesRead];
            buffer.get(data);

            threadPool.execute(() -> procesarMensaje(data, cliente));

        } catch (IOException e) {
            try {
                System.out.println("Error leyendo cliente. Cerrando: " + cliente.getRemoteAddress());
                cliente.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void procesarMensaje(byte[] data, SocketChannel cliente) {

        try {
            Mensaje mensaje = deserializarMensaje(data);

            if (mensaje.getTipoMensaje() == ETipoMensaje.CONECCION.name()) {
                //TODO: envia un mensaje q fue conectado
            }

            if (mensaje.getTipoMensaje() == ETipoMensaje.DESCONECTADO.name()) {
                //TODO: envia un mensaje q fue conectado
            }

            if (mensaje.getTipoMensaje() == ETipoMensaje.MENSAJE.name()) {
                //TODO: escribe el mensaje con la clase robot
            }
            // TODO: ESCRIBO LO Q ENVIO LA APP // escribo con la clase robot
            Mensaje sms = new Mensaje();
            sms.setTipoMensaje(ETipoMensaje.MENSAJE.name());
            sms.setTextoCode(mensaje.getTextoCode());

            MensajeManejador.notify(sms);

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private byte[] serializarMensaje(Mensaje mensaje) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(mensaje);
            return baos.toByteArray();
        }
    }

    private Mensaje deserializarMensaje(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (Mensaje) ois.readObject();
        }
    }

    @Override
    public void onMensaje(Mensaje event) {
        // enviamos mensaje de confirmacion q se vinculo opcional
        //Platform.runLater(() -> escribirCodigo(event.getTextoCode()));
    }
}
