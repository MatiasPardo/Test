package org.openxava.ventas.actions;

import org.openxava.base.actions.*;
import org.openxava.base.model.*;
import org.openxava.ventas.model.*;

public class CrearVentaCAEAction  extends CrearTransaccionAction{
	@Override
	public void execute() throws Exception {
		super.execute();
		this.getAccionesVisibles().remove(Transaccion.ACCIONCONFIRMAR);
		this.getAccionesOcultas().add(Transaccion.ACCIONCONFIRMAR);
		
		String idSubestado = null;
		try{
			idSubestado = this.getView().getValueString("subestado.id");
		}
		catch(Exception e){			
		}		
		if (TransicionEstado.tieneTransiciones(this.getView().getModelName(), idSubestado)){
			this.getAccionesOcultas().add(VentaElectronica.ACCIONSOLICITARCAE);			
		}
		else{
			this.getAccionesVisibles().add(VentaElectronica.ACCIONSOLICITARCAE);
		}
	}
}
