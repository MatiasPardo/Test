package org.openxava.base.actions;

import java.util.*;

import org.openxava.base.model.*;

public class CrearObjetoEstaticoAction extends CrearObjetoNegocioAction{
	
	private Boolean codigoSoloLectura = Boolean.FALSE; 
	
	@Override
	public void execute() throws Exception {
		super.execute();
		
		if (this.codigoSoloLectura){
			getView().setValue("codigo", "Codificación Automática");
		}
	}

	@Override
	protected void propiedadesSoloLectura(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion) {
		super.propiedadesSoloLectura(propiedadesSoloLectura, propiedadesEditables, configuracion);
		
		this.codigoSoloLectura = Boolean.FALSE;
		if (configuracion != null){
			if (configuracion.getNumerador() != null){
				this.codigoSoloLectura = Boolean.TRUE;
			}
		}
		if (this.codigoSoloLectura){
			propiedadesSoloLectura.add("codigo");
		}
		else{
			propiedadesEditables.add("codigo");
		}
	}
}
