package org.openxava.tesoreria.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.negocio.model.*;
import org.openxava.tesoreria.model.*;


public class TransferirValoresAction extends TabBaseAction{

	@Override
	public void execute() throws Exception {
		if (this.getSelectedKeys().length > 0){
			this.showDialog();
			getView().setTitle("Transferencia");
			getView().setModelName(ParametrosTransferenciaValores.class.getSimpleName());
			
			if (Esquemas.getEsquemaApp().getSucursalUnica()){
				Map<String, Object> valores = new HashMap<String, Object>();
				Sucursal sucursal = Sucursal.sucursalDefault();
				if (sucursal != null){
					valores.put("id", sucursal.getId());
					getView().setValue("sucursal", valores);
					getView().setFocus("tesoreria.id");
				}
			}			
			this.addActions("AccionTransferirValores.confirmar","Dialog.cancel");	
		}
		else{
			this.addError("sin_seleccionar_items");
		}
	}

}
