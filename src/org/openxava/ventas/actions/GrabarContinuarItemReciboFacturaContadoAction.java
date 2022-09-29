package org.openxava.ventas.actions;

import java.math.BigDecimal;

import org.openxava.actions.SaveAndStayElementInCollectionAction;

public class GrabarContinuarItemReciboFacturaContadoAction extends SaveAndStayElementInCollectionAction{

	public String getNextAction() throws Exception {
		return "ItemTransaccion.save";
	}
	
	public String getNextActionArgv() throws Exception {
		try{
			BigDecimal pendiente = (BigDecimal)getCollectionElementView().getValue("pendienteCobrar");
			BigDecimal importe = (BigDecimal)getCollectionElementView().getValue("importe");
			if ((pendiente != null) && (importe != null)){
				BigDecimal diferencia = importe.subtract(pendiente);
				if (diferencia.compareTo(BigDecimal.ZERO) >= 0){
					// ya no queda pendiente por cobrar, se cierra la ventana de item 
					return "closeDialogDisallowed=false";
				}
			}			
		}
		catch(Exception e){
			
		}
		return super.getNextActionArgv();
	}
	
}
