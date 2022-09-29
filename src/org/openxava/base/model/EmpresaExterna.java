package org.openxava.base.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.openxava.annotations.Hidden;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.Required;
import org.openxava.annotations.SearchKey;
import org.openxava.annotations.View;

@Entity

@Table(name="VIEW_EMPRESAEXTERNA")

@View(name="Simple", members="codigo, nombre;")

public class EmpresaExterna {
	
	@Id  	
	@Column(length=32)
	@Hidden @ReadOnly
	private String id = "";
	
	@Column(length=50) @Required
	@SearchKey
	@ReadOnly
    private String codigo;
	
	@Column(length=100) @Required
	@ReadOnly
    private String nombre;
	
	@Column(length=100) 
	@ReadOnly
    private String nombreFantasia;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getNombreFantasia() {
		return nombreFantasia;
	}

	public void setNombreFantasia(String nombreFantasia) {
		this.nombreFantasia = nombreFantasia;
	}	
}
