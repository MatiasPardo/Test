package org.openxava.base.actions;

import java.util.*;

import org.openxava.base.model.*;

public class CrearEmpresaAction extends CrearObjetoEstaticoAction{
	
	@Override
	protected void propiedadesSoloLectura(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion) {
		super.propiedadesSoloLectura(propiedadesSoloLectura, propiedadesEditables, configuracion);
		
		propiedadesEditables.add("moneda1");
		propiedadesEditables.add("moneda2");
	}

}
