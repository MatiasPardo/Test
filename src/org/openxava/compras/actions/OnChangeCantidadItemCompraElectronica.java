package org.openxava.compras.actions;

import java.math.*;

import org.openxava.actions.*;

public class OnChangeCantidadItemCompraElectronica extends OnChangePropertyBaseAction{

	@Override
	public void execute() throws Exception {
		if (getNewValue() != null){
			
			BigDecimal cantidad = (BigDecimal)getView().getValue("cantidad");
			BigDecimal precioUnitario = (BigDecimal)getView().getValue("precioUnitario");
			BigDecimal porcentajeDescuento = (BigDecimal)getView().getValue("porcentajeDescuento");
			
			if (cantidad == null) cantidad = BigDecimal.ZERO;
			if (precioUnitario == null) precioUnitario = BigDecimal.ZERO;
			if (porcentajeDescuento == null) porcentajeDescuento = BigDecimal.ZERO;
			
			BigDecimal sumaSinDescuento = precioUnitario.multiply(cantidad);
			BigDecimal descuento = sumaSinDescuento.multiply(porcentajeDescuento).divide(new BigDecimal(100)).setScale(4, RoundingMode.HALF_EVEN).negate();
			
			getView().setValue("descuento", descuento);
			getView().setValue("suma", sumaSinDescuento.add(descuento));
		}
		
	}

}
