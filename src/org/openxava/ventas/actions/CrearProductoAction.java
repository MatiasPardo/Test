package org.openxava.ventas.actions;

import java.util.*;

import org.openxava.base.actions.*;
import org.openxava.base.model.*;

public class CrearProductoAction extends CrearObjetoEstaticoAction{
	
	@Override
	protected void propiedadesSoloLectura(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion) {
		super.propiedadesSoloLectura(propiedadesSoloLectura, propiedadesEditables, configuracion);
		
		propiedadesEditables.add("unidadMedida");
		propiedadesEditables.add("despacho");
		propiedadesEditables.add("lote");
	}
}
