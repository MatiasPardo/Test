package org.openxava.inventario.actions;

import java.util.List;

import org.openxava.base.actions.ProcesarPendienteDesdePendienteAction;
import org.openxava.base.model.Transaccion;
import org.openxava.inventario.model.ControlMercaderia;
import org.openxava.inventario.model.ResultadoControlMercaderia;

public class CancelarPendienteControlMercaderiaAction extends ProcesarPendienteDesdePendienteAction{
	
	@Override
	protected void posGenerarTransacciones(List<Transaccion> trgeneradas){	
		super.posGenerarTransacciones(trgeneradas);
		for(Transaccion tr: trgeneradas){
			ControlMercaderia control = (ControlMercaderia)tr;
			control.setResultado(ResultadoControlMercaderia.MercaderiaNoRecibida);
		}
	}
}

