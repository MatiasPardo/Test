package org.openxava.negocio.actions;

import java.util.*;

import org.openxava.base.actions.*;
import org.openxava.base.model.*;

public class CrearObjetoCodificadoAction extends CrearObjetoNegocioAction{

	@Override
	protected void propiedadesSoloLectura(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion) {
		super.propiedadesSoloLectura(propiedadesSoloLectura, propiedadesEditables, configuracion);
		
		propiedadesEditables.add("codigo");
	}
}
