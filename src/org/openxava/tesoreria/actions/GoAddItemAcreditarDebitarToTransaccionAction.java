package org.openxava.tesoreria.actions;

import org.openxava.base.actions.*;
import org.openxava.util.*;

public class GoAddItemAcreditarDebitarToTransaccionAction extends GoAddItemToTransaccionAction{

	@Override
	public void execute() throws Exception{
		super.execute();
		
		if (getPreviousView() != null){
			String idCuentaBancaria = this.getPreviousView().getValueString("cuentaBancaria.id");
			if (!Is.emptyString(idCuentaBancaria)){
				String condition = this.getTab().getBaseCondition();
				if (!Is.emptyString(condition)){
					condition += " AND ";
				}
				condition += "${tesoreria.id} = '" + idCuentaBancaria + "'";
				this.getTab().setBaseCondition(condition);
			}
		}
		
		
	}
}
