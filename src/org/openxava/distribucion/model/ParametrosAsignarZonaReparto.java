package org.openxava.distribucion.model;

import java.util.Map;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.openxava.annotations.*;
import org.openxava.negocio.model.IParametrosReporte;
import org.openxava.view.View;

public class ParametrosAsignarZonaReparto implements IParametrosReporte{

	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private ZonaReparto zona;
	
	public ZonaReparto getZona() {
		return zona;
	}

	public void setZona(ZonaReparto zona) {
		this.zona = zona;
	}

	@Override
	public void asignarValoresIniciales(View view, View previousView, Map<?, ?>[] idsSeleccionados) {		
	}
	
}
