package org.openxava.inventario.calculators;

import java.math.*;
import java.rmi.*;

import org.openxava.calculators.*;
import org.openxava.inventario.model.*;

@SuppressWarnings("serial")
public class NoConformeCantidadExcedeOCCalculator implements IModelCalculator{

	private ItemRecepcionMercaderia item;
	
	private BigDecimal cantidad = BigDecimal.ZERO;
	
	public BigDecimal getCantidad() {
		return cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		if (cantidad != null){
			this.cantidad = cantidad;
		}
	}

	@Override
	public Object calculate() throws Exception {
		BigDecimal unidadesExcedeOC = BigDecimal.ZERO;
		if ((this.item != null) && (this.getCantidad().compareTo(BigDecimal.ZERO) > 0)){
			if (this.item.getItemOrdenCompra() != null){				
				BigDecimal dif = this.getCantidad().subtract(this.item.getItemOrdenCompra().getPendienteRecepcion());
				if (dif.compareTo(BigDecimal.ZERO) > 0){
					unidadesExcedeOC = dif;
				}
			}
		}
		return unidadesExcedeOC;
	}

	@Override
	public void setModel(Object model) throws RemoteException {
		this.item = (ItemRecepcionMercaderia)model;		
	}

}
