package org.openxava.ventas.actions;

import java.util.*;

import org.openxava.base.actions.*;
import org.openxava.base.model.*;

public class CrearListaPrecioAction extends CrearObjetoEstaticoAction{
	
	@Override
	public void execute() throws Exception {
		super.execute();
		
		this.getView().getSubview("formatoImportacionCSV").getCollectionTab().setCustomizeAllowed(false);
		this.getView().getSubview("formatoImportacionCSV").getCollectionTab().restoreDefaultProperties();
	}
	
	@Override
	protected void propiedadesSoloLectura(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion) {
		super.propiedadesSoloLectura(propiedadesSoloLectura, propiedadesEditables, configuracion);
		
		propiedadesEditables.add("moneda");		
	}
}
