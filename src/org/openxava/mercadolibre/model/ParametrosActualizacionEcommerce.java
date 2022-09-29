package org.openxava.mercadolibre.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.negocio.model.*;
import org.openxava.view.View;

public class ParametrosActualizacionEcommerce implements IParametrosReporte{
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre, codigo")
	@NoCreate @NoModify	
	private ConfiguracionMercadoLibre ecommerce;

	public ConfiguracionMercadoLibre getEcommerce() {
		return ecommerce;
	}

	public void setEcommerce(ConfiguracionMercadoLibre ecommerce) {
		this.ecommerce = ecommerce;
	}

	@Override
	public void asignarValoresIniciales(View view, View previousView, Map<?, ?>[] idsSeleccionados) {		
	}



}
