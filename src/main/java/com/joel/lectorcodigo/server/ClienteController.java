package com.joel.lectorcodigo.server;

import com.joel.lectorcodigo.datos.Mensaje;
import com.joel.lectorcodigo.utils.ETipoMensaje;
import javafx.application.Platform;

import java.io.*;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ClienteController {
    /*//System.out.println(serverChannel.getLocalAddress()+"cliente localAddres");
    //System.out.println(serverChannel.getRemoteAddress()+"cliente remoteAddres");
    private final SocketChannel socketChannel;
    private String nickJugador;

    public ClienteController(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        // Se registra como receptor de mensajes desde el juego
        JuegoEventDispatcher.register(this);
    }

    public ClienteController start(final int puerto, final String ipServer, String nickJugador, String uuid) {

        //try (var serverChannel = SocketChannel.open()) {  // Abre un canal de socket para la conexión con el servidor.
        try {
            if (socketChannel.isConnected()) {
                System.out.println("cliente creado conectado!");  // Imprime en consola que la conexión con el servidor fue exitosa.
                // recrear si falla
            }

            InetAddress localHost = InetAddress.getLocalHost();
            String localIP = localHost.getHostAddress();
            System.out.println("Cliente conectado desde IP local: " + localIP);

            // Runnable que corre en un hilo separado para leer los mensajes del servidor.
            Runnable r = () -> {
                this.nickJugador = nickJugador;
                ByteBuffer buffer2 = ByteBuffer.allocate(1024);  // Crea un búfer de 1024 bytes para almacenar los datos leídos del servidor.

                while (true) {
                    try {
                        int bytesRead = socketChannel.read(buffer2);  // Lee los datos del servidor y los almacena en el búfer.
                        if (bytesRead > 0) {  // Si se han leído datos (bytesRead > 0).
                            System.out.println("Leyendo datos...");

                            buffer2.flip();  // Cambia el búfer a modo de lectura (preparación para extraer datos).

                            // crea un arreglo de bytes para los datos leídos.
                            byte[] readData = new byte[bytesRead];
                            buffer2.get(readData);  // Extrae los datos del búfer al arreglo.

                            // deserializa los bytes para obtener el objeto original (Mensaje).
                            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(readData);
                                 ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {

                                // dserializa el objeto Mensaje.
                                Mensaje mensajeRecibido = (Mensaje) objectInputStream.readObject();
                                System.out.println("cliente: Mensaje recibido de : " + mensajeRecibido.getNickJugador() + " color " + mensajeRecibido.getColor());

                                manejarMensajeDelServidor(mensajeRecibido);

                            } catch (IOException | ClassNotFoundException e) {
                                System.err.println("Error al deserializar el mensaje: " + e.getMessage());
                                e.printStackTrace();
                            }

                            buffer2.clear();  // limpia el búfer para nuevas lecturas.
                        }

                        if (bytesRead == -1) {
                            System.out.println("Servidor cerró la conexión.");
                            socketChannel.close();
                            break; // termino el hilo
                        }

                    } catch (IOException e_) {
                        System.err.println("Error al leer los datos del servidor: " + e_.getMessage());
                    }
                }
            };

            new Thread(r) {{
                setDaemon(true);
            }}.start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public void mensajeEnviar(Mensaje mensaje) {

        ByteBuffer buffer = ByteBuffer.allocate(2048);

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

            objectOutputStream.writeObject(mensaje);
            byte[] mensajeBytes = byteArrayOutputStream.toByteArray();
            buffer.clear().put(mensajeBytes).flip();

            while (buffer.hasRemaining()) {
                socketChannel.write(buffer);
            }

            buffer.clear();
        } catch (IOException e) {
            System.err.println("Error al serializar el mensaje: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void mensajeConexion(String color, String nickJugador, String uuid) throws IOException {
        Mensaje mensaje = new Mensaje();
        mensaje.setTipoMensaje(ETipoMensaje.CONECCION.name());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // Serializar el mensaje
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(mensaje);  // Escribe el objeto al flujo
        }

        byte[] mensajeBytes = byteArrayOutputStream.toByteArray();  // Obtener los bytes serializados

        // creo el buffer y agregar los bytes serializados
        ByteBuffer buffer = ByteBuffer.allocate(mensajeBytes.length);
        buffer.put(mensajeBytes);
        buffer.flip();  // Prepara el buffer para ser leído

        // Enviar los bytes serializados al servidor
        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);  // Enviar los bytes al servidor
        }

        buffer.clear();
    }

    public void recibirMensajeDesdeJuego(Mensaje mensaje) {
        mensajeEnviar(mensaje);
    }

    public void manejarMensajeDelServidor(Mensaje mensajeRecibido) throws IOException {
        // Aquí puedes procesar el objeto 'mensajeRecibido' como lo necesites.
        switch (mensajeRecibido.getTipoMensaje().name()) {
            case "CONECCION":
                EventoJugador eventoJugador = new EventoJugador("seteando los uuid y color asignado por el servidor", mensajeRecibido.getUuidJugador());
                eventoJugador.setColor(mensajeRecibido.getColor());
                eventoJugador.setNickJugador(mensajeRecibido.getNickJugador());
                eventoJugador.setTipoMensaje(ETipoMensaje.CONECCION);
                Platform.runLater(() -> {
                    ManejadorEventObservable.notify(eventoJugador); // notifico al resto de interfas q se unio un jugador para q cambie su uuid si no es igual al suyo entonces q habilite el siguiente color
                });
                break;
            case "JUGADA":
                System.out.println("clie la jugada es " + mensajeRecibido.getCasillas());
                break;
            case "DESCONECTADO":
                System.out.println("recibir mensaje y mostrar una interfaz q se desconecto el jugador ${nick} y su color y se inavilita su interfaz");
                EventoJugador evento = new EventoJugador("Alguien jugó", mensajeRecibido.getNickJugador(), mensajeRecibido.getUuidJugador());
                ManejadorEventObservable.notify(evento);
                break;
            case "GO":
                System.out.println("si el servidor retorna el color igual al del cliente entonces es el q inicia la partida "+ mensajeRecibido.getColor());
                // interfaz habilitar la jugada si es igual al color : sino ps mostrar un mensaje q su turno es de tal jugador
                if (mensajeRecibido.getServerMensaje() != null) { // es por q hay mas de dos jugadores
                    System.out.println("mensaje del servidor jugadores menor a 2 " + mensajeRecibido.getServerMensaje()); // se muestra en una interfaz del q lo envio
                } else {
                    EventoJugador eventoJugador1 = new EventoJugador("seteando los uuid y color asignado por el servidor", mensajeRecibido.getUuidJugador());
                    eventoJugador1.setColor(mensajeRecibido.getColor());
                    eventoJugador1.setTipoMensaje(ETipoMensaje.GO);
                    Platform.runLater(() -> {
                        ManejadorEventObservable.notify(eventoJugador1); // notifico al resto de interfas para q cambie su uuid si no es igual al suyo entonces q habilite el siguiente color
                    });
                }
                break;
            case "REINICIAR":
                System.out.println("reiniciar el servidor");
                break;
            case "SERVER": // cuando recibe su uuid y su color del servidor entonces envia el mensaje de coneccion para mandar con el uuid y su color y su nick
                System.out.println("recibo color y uuid del servidor q me asigno color " + mensajeRecibido.getColor());
                Platform.runLater(() -> {
                    ManejadorEventObservable.notify(eventoJugador2); // notifico q se conecto el q envio para q setee su uuid y color
                });
                mensajeConexion(mensajeRecibido.getColor(), nickJugador, mensajeRecibido.getUuidJugador());
                // TODO: EN LA INTERFAZ PONER EL COLOR Q ME TOCO Y SU UUID
                break;
            default:
                System.out.println(" MENSAJE TIPO NO RECONOCIDO ");
        }
    }*/
}
