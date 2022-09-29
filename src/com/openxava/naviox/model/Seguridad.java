package com.openxava.naviox.model;

import javax.persistence.*;

import org.openxava.annotations.*;

@Entity

@Table(name="VIEW_SEGURIDAD")

public class Seguridad {
	
	@Id
	@Hidden
	@Column(length=64)
	private String id;
	
	private String usuario;
	
	private String rol;
	
	private String modulo;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getRol() {
		return rol;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}

	public String getModulo() {
		return modulo;
	}

	public void setModulo(String modulo) {
		this.modulo = modulo;
	}


}
