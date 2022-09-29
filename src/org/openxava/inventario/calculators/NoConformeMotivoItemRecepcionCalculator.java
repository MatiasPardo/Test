package org.openxava.inventario.calculators;

import java.math.*;
import java.rmi.*;

import org.openxava.calculators.*;
import org.openxava.inventario.model.*;


@SuppressWarnings("serial")
public class NoConformeMotivoItemRecepcionCalculator  implements IModelCalculator{

	private ItemRecepcionMercaderia itemRecepcion = null;
	
	private String idProducto;
		
	private BigDecimal cantidad;
	
	private BigDecimal precio;
	
	public String getIdProducto() {
		return idProducto;
	}

	public void setIdProducto(String idProducto) {
		this.idProducto = idProducto;
	}

	public BigDecimal getCantidad() {
		return cantidad == null ? BigDecimal.ZERO : cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
	}

	public BigDecimal getPrecio() {
		return precio == null ? BigDecimal.ZERO : precio;
	}

	public void setPrecio(BigDecimal precio) {
		this.precio = precio;
	}



	@Override
	public Object calculate() throws Exception {
		String motivo = null;;
		if (this.itemRecepcion != null){
			if (this.itemRecepcion.getItemOrdenCompra() != null){
				motivo = this.itemRecepcion.getItemOrdenCompra().conformeOrdenCompra(this.itemRecepcion);
			}
			else{
				motivo = "No tiene orden de compra";
			}
		}
		return motivo;
	}

	@Override
	public void setModel(Object model) throws RemoteException {
		this.itemRecepcion = (ItemRecepcionMercaderia)model;		
	}

}