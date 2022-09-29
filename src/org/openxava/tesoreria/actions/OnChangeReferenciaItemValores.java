package org.openxava.tesoreria.actions;

import java.math.*;

import org.openxava.actions.*;
import org.openxava.jpa.*;
import org.openxava.tesoreria.model.*;
import org.openxava.util.*;

public class OnChangeReferenciaItemValores  extends OnChangePropertyBaseAction{
	
	@Override
	public void execute() throws Exception {
		if (!Is.empty(this.getNewValue())){
			String id = (String)this.getNewValue();
			Valor valor = (Valor)XPersistence.getManager().find(Valor.class, id); 
			getView().setValue("detalle", valor.getDetalle());
			
			try{
				if (getView().getMetaProperty("numero") != null){
					getView().setValue("fechaEmision", valor.getFechaEmision());
					getView().setValue("fechaVencimiento", valor.getFechaVencimiento());
					getView().setValue("numero", valor.getNumero());
				}				
			}
			catch(ElementNotFoundException e){
			}
			getView().setValueNotifying("importeOriginal", valor.getImporte());							
		}
		else{
			getView().setValue("detalle", "");
			try{
				if (getView().getMetaProperty("numero") != null){
					getView().setValue("numero", "");
				}
			}
			catch(ElementNotFoundException e){
			}
			getView().setValueNotifying("importeOriginal", BigDecimal.ZERO);
		}
	}

}
