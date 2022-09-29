package org.openxava.compras.actions;

import java.math.*;


import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.model.*;
import org.openxava.ventas.model.*;

public class OnChangeCantidadItemCompra extends OnChangePropertyBaseAction{

	@Override
	public void execute() throws Exception {
		if (getNewValue() != null){
			BigDecimal cantidad = BigDecimal.ZERO;
			BigDecimal precioUnitario = BigDecimal.ZERO;
			BigDecimal porcentajeDescuento = (BigDecimal)getView().getValue("porcentajeDescuento");
			String idUnidadMedida = getView().getValueString("unidadMedida.id");
			
			if (this.getChangedProperty().equalsIgnoreCase("cantidad")){
				cantidad = (BigDecimal)this.getNewValue();
				precioUnitario = this.buscarCostoUnitario(getView().getValueString("producto.id"),idUnidadMedida, cantidad);
				if (getView().getValue("precioUnitario") == null){
					getView().setValue("precioUnitario", precioUnitario);
				}
				else if (precioUnitario.compareTo((BigDecimal)getView().getValue("precioUnitario")) != 0){
					getView().setValue("precioUnitario", precioUnitario);
				}
			}
			else if (this.getChangedProperty().equalsIgnoreCase("precioUnitario")){
				cantidad = (BigDecimal)getView().getValue("cantidad");
				precioUnitario = (BigDecimal)this.getNewValue();
			}
			else{
				cantidad = (BigDecimal)getView().getValue("cantidad");
				precioUnitario = (BigDecimal)getView().getValue("precioUnitario");
			}
							
			if (cantidad == null) cantidad = BigDecimal.ZERO;
			if (precioUnitario == null) precioUnitario = BigDecimal.ZERO;
			if (porcentajeDescuento == null) porcentajeDescuento = BigDecimal.ZERO;
			
			BigDecimal sumaSinDescuento = precioUnitario.multiply(cantidad);
			BigDecimal descuento = sumaSinDescuento.multiply(porcentajeDescuento).divide(new BigDecimal(100)).setScale(4, RoundingMode.HALF_EVEN).negate();
			
			getView().setValue("descuento", descuento);
			getView().setValue("suma", sumaSinDescuento.add(descuento));
		}
		
	}
	
	
	private BigDecimal buscarCostoUnitario(String idProducto, String idUnidadMedida, BigDecimal cantidad){
		BigDecimal costo = BigDecimal.ZERO;
		
		ListaPrecio listaCosto = ListaPrecio.buscarListaPrecioPrincipal(Boolean.TRUE);
		if (listaCosto != null){
			Precio precio = listaCosto.buscarObjetoPrecio(idProducto, idUnidadMedida, cantidad);
			if (precio != null){
				costo = precio.getCosto();
			}
			if (costo.compareTo(BigDecimal.ZERO) != 0){
				try {
					Transaccion transaccion = (Transaccion)MapFacade.findEntity(getView().getParent().getModelName(), getView().getParent().getKeyValues());
					costo = transaccion.convertirImporteEnMonedaTr(listaCosto.getMoneda(), costo);
				} catch (Exception e) {				
				}				
			}
		}				
		return costo;
	}

}
