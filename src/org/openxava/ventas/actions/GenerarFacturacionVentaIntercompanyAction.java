package org.openxava.ventas.actions;

import java.math.*;

import org.openxava.actions.*;
import org.openxava.model.*;
import org.openxava.ventas.model.*;

public class GenerarFacturacionVentaIntercompanyAction extends ViewBaseAction implements IChainAction{

	@Override
	public void execute() throws Exception {
		BigDecimal porcentajeFacturacion = (BigDecimal)getView().getValue("porcentaje");
		if (porcentajeFacturacion == null){
			porcentajeFacturacion = BigDecimal.ZERO;
		}
		
		VentaElectronica venta = (VentaElectronica)MapFacade.findEntity(this.getPreviousView().getModelName(), this.getPreviousView().getKeyValues());
		venta.generarTransaccionIntercompany(porcentajeFacturacion);
		this.commit();
		addMessage("Intercompany generado");
	}

	@Override
	public String getNextAction() throws Exception {
		return "FacturacionIntercompany.cancel";
	}

}
