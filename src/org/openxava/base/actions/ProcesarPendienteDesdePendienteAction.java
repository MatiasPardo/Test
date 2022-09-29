package org.openxava.base.actions;

import java.util.*;

import javax.ejb.*;

import org.openxava.base.model.*;
import org.openxava.model.*;
import org.openxava.validators.*;

public class ProcesarPendienteDesdePendienteAction extends ProcesarPendienteAction{

	@SuppressWarnings("rawtypes")
	@Override
	protected void getPendiente(Map clave, List<Pendiente> pendientes) {
		Pendiente pendiente;
		try {
			pendiente = (Pendiente)MapFacade.findEntity(this.getTab().getModelName(), clave);
			pendientes.add(pendiente);
		} catch (ObjectNotFoundException e) {
			throw new ValidationException("No se encontró el pendiente para la clave " + clave.toString());
		} catch (Exception e) {
			throw new ValidationException("Error al buscar pendiente " + clave.toString());
		}
	}

}
