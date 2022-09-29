package org.openxava.contratos.actions;

import java.util.HashMap;
import java.util.Map;

import org.openxava.actions.OnChangePropertyBaseAction;
import org.openxava.contratos.model.Contrato;
import org.openxava.jpa.XPersistence;
import org.openxava.util.Is;

public class OnChangeContratoAction extends OnChangePropertyBaseAction{

	@Override
	public void execute() throws Exception {
		if (getNewValue() != null){
			String idContrato = (String)this.getNewValue();
			if (!Is.emptyString(idContrato)){
				Contrato contrato = XPersistence.getManager().find(Contrato.class, idContrato);
				Map<String, Object> values = new HashMap<String, Object>();
				values.put("id", contrato.getMoneda().getId());
				this.getView().setValue("moneda", values);
			}
		}
	}

}
