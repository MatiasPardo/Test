package org.openxava.negocio.model;

import java.util.Map;

import org.openxava.view.*;

public interface IParametrosReporte {
	
	public void asignarValoresIniciales(View view, View previousView, Map<?, ?>[] idsSeleccionados);
	
}
