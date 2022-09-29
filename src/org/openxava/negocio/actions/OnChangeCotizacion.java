package org.openxava.negocio.actions;

import java.math.*;

import org.openxava.actions.*;

public class OnChangeCotizacion extends OnChangePropertyBaseAction{

	@Override
	public void execute() throws Exception {
		if (this.getNewValue() != null){
			BigDecimal cotizacionNueva = (BigDecimal)this.getNewValue();
			if (cotizacionNueva.compareTo(BigDecimal.ZERO) != 0){
				BigDecimal cotizacionAnterior = (BigDecimal)this.getView().getValue("cotizacionAnterior");
				if (cotizacionAnterior != null){
					if (cotizacionAnterior.compareTo(BigDecimal.ZERO) != 0){
						BigDecimal porcentaje = cotizacionNueva.multiply(new BigDecimal(100)).divide(cotizacionAnterior, 2, RoundingMode.UP).subtract(new BigDecimal(100));
						porcentaje = porcentaje.setScale(2, RoundingMode.UP);
						String mensaje = "";
						if (porcentaje.compareTo(BigDecimal.ZERO) > 0){
							mensaje = "Aumento de la cotizaci�n en un % ";
						}
						else if (porcentaje.compareTo(BigDecimal.ZERO) < 0){
							mensaje = "Reducci�n de la cotizaci�n en un % ";
						}
						
						if (porcentaje.abs().compareTo(new BigDecimal(50)) > 0){
							this.addError("Cuidado, la cotizaci�n ha variado considerablemente con respecto a la �ltima registrada");
						}
						else if (porcentaje.abs().compareTo(new BigDecimal(10)) > 0){
							this.addWarning("Advertencia, la cotizaci�n difiere bastante de la �ltima registrada");
						}
						this.addInfo(mensaje + porcentaje.toString() + ". Debe grabar para que la cotizaci�n impacte en las operaciones");
					}
				}
			}
		}
		
	}

}
