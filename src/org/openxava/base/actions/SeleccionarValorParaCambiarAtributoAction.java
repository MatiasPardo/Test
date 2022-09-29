package org.openxava.base.actions;

import org.openxava.actions.*;

public class SeleccionarValorParaCambiarAtributoAction extends SeleccionarValorParaCambiarAction implements IPropertyAction{

	@Override
	public void setProperty(String propertyName) {
		this.setNombreAtributo(propertyName);		
	}	
}

