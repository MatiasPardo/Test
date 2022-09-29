package org.openxava.tesoreria.actions;

import org.openxava.negocio.actions.VerListaParaMultiseleccionAction;
import org.openxava.tab.Tab;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;

public class VerCuponesTarjetaAction extends VerListaParaMultiseleccionAction{

	@Override
	protected void armarListadoMultilseleccion(Tab tab) {
		tab.setModelName("ItemReciboCobranza");
		tab.setTabName("CuponesTarjetaCredito");
		
		String idSucursalOrigen = this.getView().getValueString("sucursal.id");
		String codigoProveedor = this.getView().getValueString("proveedor.codigo");
		String idEmpresa = this.getView().getValueString("empresa.id");
		if (!Is.emptyString(idSucursalOrigen) && !Is.emptyString(codigoProveedor)){
			tab.setBaseCondition("(${reciboCobranza.empresa.id} = '" + idEmpresa + "' and ${reciboCobranza.sucursal.id} = '" + idSucursalOrigen + "' and ${tipoValor.proveedorTarjeta} = '" + codigoProveedor + "'  and ${liquidacionTarjeta} is null)");
		}
		else{
			throw new ValidationException("Debe asignar la sucursal y el proveedor");
		}	
		
	}	
}
