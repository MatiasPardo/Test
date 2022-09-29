package org.openxava.negocio.model;

import javax.persistence.*;

import org.openxava.annotations.*;

@Entity

@Views({
	@View(name="Simple",
	members="provincia")
})

public class Provincia {
	@Id @Hidden
	private long codigo;
	
	@SearchKey
	@Column(length=50)
	private String provincia;
	
	@ReadOnly
	private int codigoAfip;
	
	@ReadOnly
	private int codigoJuridiccion;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	@ReadOnly
	private Pais pais;
	
	public long getCodigo() {
		return codigo;
	}

	public void setCodigo(long codigo) {
		this.codigo = codigo;
	}

	public String getProvincia() {
		return provincia;
	}

	public void setProvincia(String provincia) {
		this.provincia = provincia;
	}
	
	public int getCodigoAfip() {
		return codigoAfip;
	}

	public void setCodigoAfip(int codigoAfip) {
		this.codigoAfip = codigoAfip;
	}

	public Integer AfipCodigo(){
		return this.getCodigoAfip();	
	}

	public int getCodigoJuridiccion() {
		return codigoJuridiccion;
	}

	public void setCodigoJuridiccion(int codigoJuridiccion) {
		this.codigoJuridiccion = codigoJuridiccion;
	}

	public Pais getPais() {
		return pais;
	}

	public void setPais(Pais pais) {
		this.pais = pais;
	}
}
