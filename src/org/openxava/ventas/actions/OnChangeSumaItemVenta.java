package org.openxava.ventas.actions;

import java.math.*;

import org.openxava.actions.*;

public class OnChangeSumaItemVenta extends OnChangePropertyBaseAction{

	@Override
	public void execute() throws Exception {
		if (getNewValue() != null){
			BigDecimal suma = (BigDecimal)getNewValue();
			BigDecimal subtotalItem = BigDecimal.ZERO;
			BigDecimal descuentoGlobal = BigDecimal.ZERO;
			BigDecimal descuentoFinanciero = BigDecimal.ZERO;
			if (suma.compareTo(BigDecimal.ZERO) != 0){
				BigDecimal porcentaje = (BigDecimal)getView().getParent().getValue("porcentajeDescuento");
				if (porcentaje != null){
					descuentoGlobal = suma.multiply(porcentaje).divide(new BigDecimal(100)).negate().setScale(4, RoundingMode.HALF_EVEN); 
				}
				porcentaje = (BigDecimal)getView().getParent().getValue("porcentajeFinanciero");
				if (porcentaje != null){
					descuentoFinanciero = suma.add(descuentoGlobal).multiply(porcentaje).divide(new BigDecimal(100)).negate().setScale(4,  RoundingMode.HALF_EVEN);
				}
				subtotalItem = suma.add(descuentoGlobal).add(descuentoFinanciero);
			}
			getView().trySetValue("descuentoGlobal", descuentoGlobal);
			getView().trySetValue("descuentoFinanciero", descuentoFinanciero);
			getView().setValueNotifying("subtotal", subtotalItem);
		}
		
	}

}
