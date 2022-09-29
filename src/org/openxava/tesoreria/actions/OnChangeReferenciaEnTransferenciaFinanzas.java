package org.openxava.tesoreria.actions;

import java.math.*;

import org.openxava.actions.*;
import org.openxava.jpa.*;
import org.openxava.tesoreria.model.*;
import org.openxava.util.*;

public class OnChangeReferenciaEnTransferenciaFinanzas extends OnChangePropertyBaseAction {

	@Override
	public void execute() throws Exception {
		if (!Is.empty(this.getNewValue())){
			String id = (String)this.getNewValue();
			Valor valor = (Valor)XPersistence.getManager().find(Valor.class, id); 
			getView().setValueNotifying("importeOriginal", valor.getImporte());
		}
		else{
			getView().setValueNotifying("importeOriginal", BigDecimal.ZERO);
		}
	}
}
