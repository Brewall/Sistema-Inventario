package com.edu.sistema_inventario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UsuarioUpdateRequest {
    @Size(min = 2, message = "El nombre debe tener al menos 2 caracteres")
    private String nombre;

    @Size(min = 2, message = "El apellido debe tener al menos 2 caracteres")
    private String apellido;

    @Email(message = "El email no es valido")
    private String email;

    @Size(min = 6, message = "La password debe tener al menos 6 caracteres")
    private String password;

    private String rol;

    private Boolean activo;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
