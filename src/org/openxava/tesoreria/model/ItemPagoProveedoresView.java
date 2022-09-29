package org.openxava.tesoreria.model;

import java.math.BigDecimal;

import org.openxava.base.model.ItemTransaccionView;
import org.openxava.base.model.ObjetoNegocio;
import org.openxava.view.View;

public class ItemPagoProveedoresView extends ItemTransaccionView{
	
	@Override
	public void copiarValoresCabecera(View cabecera, View items, ObjetoNegocio cabeceraPosCommit) {
		super.copiarValoresCabecera(cabecera, items, cabeceraPosCommit);
		
		try{
			BigDecimal diferencia = (BigDecimal)cabecera.getValue("falta");
			if (cabeceraPosCommit != null){
				// se utiliza el objeto porque esta recalculado
				diferencia = ((PagoProveedores)cabeceraPosCommit).getFalta();
			}
			if (diferencia == null) diferencia = BigDecimal.ZERO;
			
			items.setValue("pendientePagar", diferencia);
			items.setValue("diferencia", ReciboCobranza.convertirStringPendienteCobrar(diferencia.negate()));
		}
		catch(Exception e){
		}
	}
}
