package org.openxava.inventario.model;

import java.util.Map;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.clasificadores.model.*;
import org.openxava.negocio.model.*;
import org.openxava.view.View;

public class ParametrosConsignacionesPendientes implements IParametrosReporte{
	
	@ManyToOne(fetch=FetchType.LAZY, optional=true)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	private Marca marca;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=true)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	private Rubro rubro;
	
	@Override
	public void asignarValoresIniciales(View view, View previousView, Map<?, ?>[] idsSeleccionados) {		
	}

	public Marca getMarca() {
		return marca;
	}

	public void setMarca(Marca marca) {
		this.marca = marca;
	}

	public Rubro getRubro() {
		return rubro;
	}

	public void setRubro(Rubro rubro) {
		this.rubro = rubro;
	}
}
