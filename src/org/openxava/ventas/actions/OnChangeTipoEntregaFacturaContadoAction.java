package org.openxava.ventas.actions;

import org.openxava.actions.*;
import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.ventas.model.*;

public class OnChangeTipoEntregaFacturaContadoAction extends OnChangePropertyBaseAction{

	@Override
	public void execute() throws Exception {
		if (!Is.emptyString((String)this.getNewValue())){
			String idTipoEntrega = (String)this.getNewValue();
			TipoEntrega tipoEntrega = XPersistence.getManager().find(TipoEntrega.class, idTipoEntrega);
			if (tipoEntrega.getDomicilioObligatorio()){
				String idCliente = this.getView().getValueString("cliente.id");
				if (!Is.emptyString(idCliente)){
					if (((Cliente)XPersistence.getManager().find(Cliente.class, idCliente)).getSinIdentificacion()){
						this.getView().setValue("direccion", null);
						this.getView().getSubview("ciudad").clear();
					}
				}
			}			
		}
	}

}
