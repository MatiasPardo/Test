package org.openxava.tesoreria.model;

import java.math.BigDecimal;
import java.util.Map;

import org.openxava.base.model.ItemTransaccionView;
import org.openxava.base.model.ObjetoNegocio;
import org.openxava.view.View;

public class ItemReciboCobranzaView extends ItemTransaccionView{
	
	private Map<?, ?> destinoValues;
	
	@Override
	public void copiarValoresItemsPrevioGrabar(View item){
		super.copiarValoresItemsPrevioGrabar(item);
		
		try{
			View subviewDestino = item.getSubview("destino");
			if (subviewDestino != null){
				destinoValues = subviewDestino.getValues();
			}			
		}
		catch(Exception e){
		}
	}
	
	@Override
	public void copiarValoresCabecera(View cabecera, View items, ObjetoNegocio cabeceraPosCommit) {
		super.copiarValoresCabecera(cabecera, items, cabeceraPosCommit);
		
		try{
			if ((destinoValues != null) && (!destinoValues.isEmpty())){
				items.setValue("destino", destinoValues);
			}
			BigDecimal diferencia = (BigDecimal)cabecera.getValue("diferencia");
			if (cabeceraPosCommit != null){
				// se utiliza el objeto porque esta recalculado
				diferencia = ((ReciboCobranza)cabeceraPosCommit).getDiferencia();
			}
			if (diferencia == null) diferencia = BigDecimal.ZERO;
			
			items.setValue("pendienteCobrar", diferencia.negate());
			items.setValue("diferencia", ReciboCobranza.convertirStringPendienteCobrar(diferencia));
		}
		catch(Exception e){
		}
	}
	
}
