package org.openxava.ventas.actions;

import java.util.HashMap;
import java.util.Map;

import org.openxava.actions.OnChangePropertyBaseAction;
import org.openxava.afip.calculators.TipoComprobanteCalculator;
import org.openxava.fisco.model.TipoComprobante;
import org.openxava.util.Is;

public class OnChangePosicionIva extends OnChangePropertyBaseAction{

	@Override
	public void execute() throws Exception {
		if (!Is.emptyString((String)getNewValue())){	
			String idPuntoVenta = this.getView().getValueString("puntoVenta.id");
			if (!Is.emptyString(idPuntoVenta)){
				String codigoPosicionIva = this.getNewValue().toString();
				
				TipoComprobanteCalculator calculator = new TipoComprobanteCalculator();
				calculator.setCodigoPosicionIVA(codigoPosicionIva);
				calculator.setIdPuntoVenta(idPuntoVenta);
				TipoComprobante tipo = (TipoComprobante)calculator.calculate();
				
				Map<String, Object> values = new HashMap<String, Object>();
				values.put("id", tipo.getId());
				values.put("tipo", tipo.getTipo());
				this.getView().setValue("tipo", values);
			}
		}
	}
}
