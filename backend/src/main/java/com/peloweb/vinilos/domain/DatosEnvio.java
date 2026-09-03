package com.peloweb.vinilos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Datos de envío de una orden (solo modo_entrega = ENVIO). Spec 4.11. */
@Embeddable
public class DatosEnvio {

    @Column(name = "envio_nombre")
    private String nombre;

    @Column(name = "envio_telefono")
    private String telefono;

    @Column(name = "envio_direccion")
    private String direccion;

    @Column(name = "envio_localidad")
    private String localidad;

    @Column(name = "envio_provincia")
    private String provincia;

    @Column(name = "envio_cp")
    private String cp;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getCp() {
        return cp;
    }

    public void setCp(String cp) {
        this.cp = cp;
    }
}
