package org.openxava.codigobarras.actions;

import java.math.BigDecimal;

import org.openxava.actions.CollectionElementViewBaseAction;
import org.openxava.base.model.Transaccion;
import org.openxava.codigobarras.model.CodigoBarrasProducto;
import org.openxava.codigobarras.model.IControlCodigoBarra;
import org.openxava.jpa.XPersistence;
import org.openxava.model.MapFacade;
import org.openxava.util.Is;

public class ConfirmarLecturaCodigoBarraAction extends CollectionElementViewBaseAction{

	private boolean crearItem = false;
	
	public boolean isCrearItem() {
		return crearItem;
	}
	
	public void setCrearItem(boolean crearItem) {
		this.crearItem = crearItem;
	}
	
	@Override
	public void execute() throws Exception {
		
		String tipoCodigoBarras = this.getView().getValueString("tipoCodigoBarras.id");
		String codigoBarras = this.getView().getValueString("codigoBarras");
		BigDecimal cantidad = (BigDecimal)this.getView().getValue("cantidad");
		if (Is.emptyString(tipoCodigoBarras)){
			this.addError("Falta asignar tipo de código de barras");
		}
		if (Is.emptyString(codigoBarras)){
			this.addError("Falta asignar código de barras");			
		}
		if (cantidad == null){
			this.addError("Falta asignar cantidad");
		}
		else if (cantidad.compareTo(BigDecimal.ZERO) == 0){
			this.addError("Cantidad en cero");
		}
		
		if (this.getErrors().isEmpty()){
			IControlCodigoBarra transaccion = (IControlCodigoBarra)MapFacade.findEntity(this.getPreviousView().getModelName(), this.getPreviousView().getKeyValues());
			CodigoBarrasProducto configuracion = XPersistence.getManager().find(CodigoBarrasProducto.class, tipoCodigoBarras);
			configuracion.controlarItems(transaccion, codigoBarras, cantidad, this.isCrearItem());
			
			((Transaccion)transaccion).grabarTransaccion();
			this.commit();
			
			this.addMessage("Lectura correcta: " + codigoBarras);
			
			if (((BigDecimal)this.getView().getValue("cantidad")).compareTo(BigDecimal.ZERO) >= 0){
				this.getView().setValue("cantidad", new BigDecimal(1));
			}
			else{
				this.getView().setValue("cantidad", new BigDecimal(-1));
			}
			this.getView().setValue("codigoBarras", null);
			this.getView().setFocus("codigoBarras");
			
			BigDecimal total = transaccion.mostrarTotalLectorCodigoBarras();
			if (total != null){
				this.getView().setHidden("total", false);
				this.getView().setValue("total", total);
			}
		}
	}

}

