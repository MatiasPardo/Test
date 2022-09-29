package org.openxava.base.model;

import java.util.*;

public interface IEstrategiaCancelacionPendiente {
	
	public void cancelarPendientes();
	
	public void liberarPendientes();
	
	public void pendientesProcesados(Collection<Pendiente> list);
}
