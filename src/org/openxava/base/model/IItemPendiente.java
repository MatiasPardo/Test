package org.openxava.base.model;

import org.openxava.validators.*;

public interface IItemPendiente {
	
	public Pendiente getPendiente();
	
	public Boolean cumplido();
	
	public void cancelarPendiente() throws ValidationException;
	
	public void liberar();
	
	public ObjetoNegocio getItem();
}
