package com.joel.lectorcodigo.datos;

import com.joel.lectorcodigo.datos.Mensaje;
import com.joel.lectorcodigo.datos.MensajeEvento;

import java.util.ArrayList;
import java.util.List;

public class MensajeManejador {
    private static List<MensajeEvento> observable = new ArrayList<>();

    public static void registerListener(MensajeEvento listener) {
        observable.add(listener);
    }

    public static void unregisterListener(MensajeEvento listener) {
        observable.remove(listener);
    }

    public static void notify(Mensaje event) {
        for (MensajeEvento listener : observable) {
            listener.onMensaje(event);
        }
    }
}
