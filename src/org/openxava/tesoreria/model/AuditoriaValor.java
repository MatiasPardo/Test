package org.openxava.tesoreria.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;

@Entity

@Views({
	@View(members=
		"Auditoria[#modificacion, valorAnterior, valorNuero;" + 
				"fechaCreacion, usuario];" + 
				"referencia;")
})

public class AuditoriaValor extends ObjetoNegocio{
	
	@Required
	private TipoModificacionValor modificacion;
	
	@Column(length=50)
	private String valorAnterior;
	
	@Column(length=50)
	private String valorNuevo;
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	private Valor referencia;

	public TipoModificacionValor getModificacion() {
		return modificacion;
	}

	public void setModificacion(TipoModificacionValor modificacion) {
		this.modificacion = modificacion;
	}

	public String getValorAnterior() {
		return valorAnterior;
	}

	public void setValorAnterior(String valorAnterior) {
		this.valorAnterior = valorAnterior;
	}

	public String getValorNuevo() {
		return valorNuevo;
	}

	public void setValorNuevo(String valorNuevo) {
		this.valorNuevo = valorNuevo;
	}

	public Valor getReferencia() {
		return referencia;
	}

	public void setReferencia(Valor referencia) {
		this.referencia = referencia;
	}	
}
