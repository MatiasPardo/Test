package org.openxava.ventas.model;

import java.util.Map;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.clasificadores.model.*;
import org.openxava.negocio.model.*;
import org.openxava.view.View;

public class ParametrosReporteProductoDisponibles implements IParametrosReporte{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	@NoCreate @NoModify
	private Marca marca;
	
	private Boolean incluyeCompras = Boolean.FALSE;
	
	public Boolean getIncluyeCompras() {
		return incluyeCompras;
	}

	public void setIncluyeCompras(Boolean incluyeCompras) {
		this.incluyeCompras = incluyeCompras;
	}

	public Marca getMarca() {
		return marca;
	}

	public void setMarca(Marca marca) {
		this.marca = marca;
	}

	@Override
	public void asignarValoresIniciales(View view, View previousView, Map<?, ?>[] idsSeleccionados) {
		// TODO Auto-generated method stub	
	}
}
