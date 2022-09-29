package org.openxava.ventas.actions;

import java.math.*;

import org.openxava.actions.*;
import org.openxava.jpa.*;
import org.openxava.model.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

public class OnChangeCantidadItemVentaElectronica extends OnChangePropertyBaseAction{
	

	@Override
	public void execute() throws Exception {
		if (getNewValue() != null){
			if (getView().getParent().getModelName().equals("FacturaVentaContado")){
				// se busca el precio
				BigDecimal cantidad = (BigDecimal)getNewValue();
				String idProducto = this.getView().getValueString("producto.id");
				String idUnidadMedida = this.getView().getValueString("unidadMedida.id");
				if (!Is.emptyString(idProducto) && cantidad.compareTo(BigDecimal.ZERO) > 0 && !Is.emptyString(idUnidadMedida)) {
					ItemVentaElectronica item = new ItemVentaElectronica();
					item.setProducto(XPersistence.getManager().find(Producto.class, idProducto));
					item.setCantidad(cantidad);
					item.setUnidadMedida(XPersistence.getManager().find(UnidadMedida.class, idUnidadMedida));
					item.setVenta((VentaElectronica)MapFacade.findEntity(getView().getParent().getModelName(), getView().getParent().getKeyValues()));
					try{
						item.getVenta().asignarPrecioUnitario(item);
						getView().setValueNotifying("precioUnitario", item.getPrecioUnitario());
					}
					catch(ValidationException e){
						this.addErrors(e.getErrors());
						this.getView().setValue("cantidad", BigDecimal.ZERO);
					}
				}
			}
			else{
				BigDecimal precio = BigDecimal.ZERO;
				// TODO: Buscar el precio de la lista de precio con:
				//getView().getSubview("producto").getKeyValues()
				//getView().getParent().getSubbiew("cliente")
				getView().setValueNotifying("precioUnitario", precio);
			}
		}		
	}

	
}
