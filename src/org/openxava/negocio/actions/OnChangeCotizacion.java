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
							mensaje = "Aumento de la cotización en un % ";
						}
						else if (porcentaje.compareTo(BigDecimal.ZERO) < 0){
							mensaje = "Reducción de la cotización en un % ";
						}
						
						if (porcentaje.abs().compareTo(new BigDecimal(50)) > 0){
							this.addError("Cuidado, la cotización ha variado considerablemente con respecto a la última registrada");
						}
						else if (porcentaje.abs().compareTo(new BigDecimal(10)) > 0){
							this.addWarning("Advertencia, la cotización difiere bastante de la última registrada");
						}
						this.addInfo(mensaje + porcentaje.toString() + ". Debe grabar para que la cotización impacte en las operaciones");
					}
				}
			}
		}
		
	}

}
