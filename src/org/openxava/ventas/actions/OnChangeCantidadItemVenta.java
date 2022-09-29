package org.openxava.ventas.actions;

import java.math.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.jpa.XPersistence;
import org.openxava.model.*;
import org.openxava.negocio.model.UnidadMedida;
import org.openxava.util.*;
import org.openxava.ventas.model.*;


public class OnChangeCantidadItemVenta extends OnChangePropertyBaseAction{

	@Override
	public void execute() throws Exception {
		if (getNewValue() != null){
			String idCliente = (String)getView().getParent().getValue("cliente.id");
			if (idCliente != null){
				BigDecimal cantidad = (BigDecimal)getView().getValue("cantidad");
				BigDecimal precioUnitario = (BigDecimal)getView().getValue("precioUnitario");
				BigDecimal porcentajeDescuento = (BigDecimal)(getView().getValue("porcentajeDescuento"));
				if (this.getChangedProperty().equalsIgnoreCase("preciounitario")){					
					precioUnitario = (BigDecimal)getNewValue();
				}
				else if (this.getChangedProperty().equalsIgnoreCase("cantidad")){
					cantidad = (BigDecimal)getNewValue();
					if ((cantidad != null) && (cantidad.compareTo(BigDecimal.ZERO) != 0)){
						String codigoProducto = getView().getValueString("producto.codigo");
						if (!Is.emptyString(codigoProducto)){							
							Cliente cliente = (Cliente)MapFacade.findEntity("Cliente", getView().getParent().getSubview("cliente").getKeyValues());
							Producto producto = (Producto)ObjetoEstatico.buscarPorCodigo(codigoProducto, Producto.class.getSimpleName());
							IVenta transaccion = (IVenta)MapFacade.findEntity(getView().getParent().getModelName(), getView().getParent().getKeyValues());
							ListaPrecio lista = (ListaPrecio)MapFacade.findEntity("ListaPrecio", getView().getParent().getSubview("listaPrecio").getKeyValues());
							String idUnidadMedida = this.getView().getValueString("unidadMedida.id");
							UnidadMedida unidad = null;
							if (!Is.emptyString(idUnidadMedida)){
								unidad = (UnidadMedida)XPersistence.getManager().find(UnidadMedida.class, idUnidadMedida);
							}							
							precioUnitario = cliente.calcularPrecio(lista, producto, unidad, cantidad, transaccion);
							if (precioUnitario != null){
								getView().setEditable("precioUnitario", false);
								getView().setFocus("porcentajeDescuento");
							}
							else{
								getView().setEditable("precioUnitario", true);
								getView().setFocus("precioUnitario");
							}
							getView().setValue("precioUnitario", precioUnitario);
						}
					}
				}
				else if (this.getChangedProperty().equalsIgnoreCase("porcentajeDescuento")){
					porcentajeDescuento = (BigDecimal)getNewValue();
				}
				if (cantidad == null) cantidad = BigDecimal.ZERO;
				if (precioUnitario == null) precioUnitario = BigDecimal.ZERO;
				if (porcentajeDescuento == null) porcentajeDescuento = BigDecimal.ZERO;
				
				BigDecimal suma = precioUnitario.multiply(cantidad).multiply((new BigDecimal(100)).subtract(porcentajeDescuento)).divide(new BigDecimal(100));
				getView().setValueNotifying("suma", suma);
			}
		}		
	}

}
