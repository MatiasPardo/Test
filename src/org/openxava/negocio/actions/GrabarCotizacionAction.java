package org.openxava.negocio.actions;

import java.math.*;

import org.openxava.base.actions.*;

public class GrabarCotizacionAction extends GrabarObjetoNegocioAction{
	
	@Override
	public void execute() throws Exception{
		super.execute();
		
		if (this.getErrors().isEmpty()){
			BigDecimal cotizacionNueva = (BigDecimal)this.getView().getValue("cotizacion");
			if (cotizacionNueva.compareTo(BigDecimal.ZERO) != 0){
				BigDecimal cotizacionAnterior = (BigDecimal)this.getView().getValue("cotizacionAnterior");
				if (cotizacionAnterior != null){
					if (cotizacionAnterior.compareTo(BigDecimal.ZERO) != 0){
						BigDecimal porcentaje = cotizacionNueva.multiply(new BigDecimal(100)).divide(cotizacionAnterior, 2, RoundingMode.UP).subtract(new BigDecimal(100));
						porcentaje = porcentaje.setScale(2, RoundingMode.UP);
						String mensaje = "";
						if (porcentaje.compareTo(BigDecimal.ZERO) > 0){
							mensaje = "Aumento % ";
						}
						else if (porcentaje.compareTo(BigDecimal.ZERO) < 0){
							mensaje = "Reducción % ";
						}
						if (mensaje != ""){
							this.addInfo(mensaje + porcentaje.toString());
						}
					}
				}
			}
		}
	}
}
