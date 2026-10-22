package com.joel.lectorcodigo.datos;

public class CodigoBarra {

    private Integer id;
    private String codigo;
    private String producto;
    private String tipo;

    public CodigoBarra() {

    }

    public CodigoBarra(String codigo, String producto) {
        this.codigo = codigo;
        this.producto = producto;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
/*private final SimpleStringProperty codigo;
    private final SimpleStringProperty producto;

    public CodigoBarra(String codigo, String producto) {
        this.codigo = new SimpleStringProperty(codigo);
        this.producto = new SimpleStringProperty(producto);
    }

    public String getCodigo() { return codigo.get(); }
    public void setCodigo(String value) { codigo.set(value); }

    public String getProducto() { return producto.get(); }
    public void setProducto(String value) { producto.set(value); }

    public SimpleStringProperty codigoProperty() { return codigo; }
    public SimpleStringProperty productoProperty() { return producto; }*/
}
