package org.openxava.base.actions;

import java.util.*;

import org.openxava.base.model.*;

public class CrearTransicionEstadoAction extends CrearObjetoNegocioAction{
	
	@Override
	protected void propiedadesSoloLectura(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion) {
		super.propiedadesSoloLectura(propiedadesSoloLectura, propiedadesEditables, configuracion);
		
		propiedadesEditables.add("entidad");
	}

}
