package com.joel.lectorcodigo.datos;

import java.io.Serializable;

public class Mensaje implements Serializable {
    private String textoCode;
    private int puerto;
    private String token;
    private String tipoMensaje;

    public String getTextoCode() {
        return textoCode;
    }

    public void setTextoCode(String textoCode) {
        this.textoCode = textoCode;
    }

    public int getPuerto() {
        return puerto;
    }

    public void setPuerto(int puerto) {
        this.puerto = puerto;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipoMensaje() {
        return tipoMensaje;
    }

    public void setTipoMensaje(String tipoMensaje) {
        this.tipoMensaje = tipoMensaje;
    }
}
