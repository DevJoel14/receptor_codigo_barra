package com.joel.lectorcodigo.datos;

import com.joel.lectorcodigo.datos.Mensaje;

public interface MensajeEvento {
    void onMensaje(Mensaje event);
}
