package org.openxava.base.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.model.*;

public class GenerarCircuitoDesdeEntidadAction extends TabBaseAction{

	@Override
	public void execute() throws Exception {
		@SuppressWarnings("rawtypes")
		Map [] selectedOnes = getSelectedKeys(); 
		if (selectedOnes != null) {
			for (int i = 0; i < selectedOnes.length; i++) {
				@SuppressWarnings("rawtypes")
				Map key = selectedOnes[i];
				ConfiguracionEntidad entidad = (ConfiguracionEntidad)MapFacade.findEntity(this.getTab().getModelName(), key);
				Map<String, Object> procesados = new HashMap<String, Object>();
				Collection<ConfiguracionCircuito> circuitos = new LinkedList<ConfiguracionCircuito>();
				entidad.generarCircuito(procesados, circuitos);
				
				for(ConfiguracionCircuito circuito: circuitos){
					addMessage(circuito.toString());
				}
				this.commit();
			}
			addMessage("Generación de Circuitos finalizada");
		}
		
	}

}
