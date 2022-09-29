package org.openxava.tesoreria.actions;

import java.math.*;

import org.openxava.actions.*;
import org.openxava.util.*;

public class OnChangeTipoValorItemValores extends OnChangePropertyBaseAction{

	@Override
	public void execute() throws Exception {
		if (!Is.empty(this.getNewValue())){
			this.getView().setValue("importeOriginal", null);
			if (this.getView().getMetaProperty("cotizacion") != null){
				this.getView().setValue("cotizacion", BigDecimal.ZERO);
			}
		}		
	}
}
