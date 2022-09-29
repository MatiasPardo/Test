package org.openxava.codigobarras.actions;

import org.openxava.actions.ViewBaseAction;
import org.openxava.base.model.UtilERP;
import org.openxava.codigobarras.model.CodigoBarrasProducto;
import org.openxava.util.Is;
import org.openxava.ventas.model.Producto;

public class TestLectorCodigoBarrasAction extends ViewBaseAction{

	@Override
	public void execute() throws Exception {
		String lectura = this.getView().getValueString("lecturaCodigoBarras");
		if (!Is.emptyString(lectura)){
			CodigoBarrasProducto configurador = new CodigoBarrasProducto();
			UtilERP.copyValuesViewToObject(this.getView(), configurador);			
			configurador.escanear(lectura);
			
			Producto producto = configurador.getProductoEscaneado();
			if (producto != null){
				this.addMessage("Producto: " + configurador.getProductoEscaneado());
			}
			else{
				this.addInfo("Sin producto");
			}

			if (!Is.emptyString(configurador.getLoteEscaneado())){
				this.addMessage("Lote: " + configurador.getLoteEscaneado());
			}
			else{
				this.addInfo("Sin Lote");
			}
			
			if (configurador.getVencimientoEscaneado() != null){
				this.addMessage("Vencimiento: " + UtilERP.convertirString(configurador.getVencimientoEscaneado()));
			}
			else{
				this.addInfo("Sin vencimiento");
			}
			
			if (!Is.emptyString(configurador.getSerieEscaneado())){
				this.addMessage("Serie: " + configurador.getSerieEscaneado());
			}
			else{
				this.addInfo("Sin serie");
			}
		}
		else{
			this.addError("Debe asignar una lectura de código de barras");
		}
	}
}
