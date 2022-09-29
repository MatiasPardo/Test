package org.openxava.ventas.actions;

import org.openxava.base.actions.*;

public class ConfirmarReciboFacturaContadoAction extends ConfirmarTransaccionAction{

	@Override
	public String getNextAction() throws Exception {
		if (this.getErrors().isEmpty()){
			return "ReciboFacturaContado.return";
		}
		else{
			return super.getNextAction();
		}
	}
}
