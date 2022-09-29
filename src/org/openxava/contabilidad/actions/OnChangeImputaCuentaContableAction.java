package org.openxava.contabilidad.actions;

import org.openxava.actions.*;

public class OnChangeImputaCuentaContableAction extends OnChangePropertyBaseAction{

	@Override
	public void execute() throws Exception {
		if ((getNewValue() != null) && (this.getView().isKeyEditable())){
			String id = (String)getNewValue();
			if (!id.isEmpty()){
				String codigoImputa = (String)getView().getValue("imputa.codigo");
				String codigoCuenta = (String)getView().getValue("codigo");
				
				if ((codigoImputa != null) && (!codigoImputa.isEmpty())){
					boolean cambiarCodigo = true;
					if ((codigoCuenta != null) && (!codigoCuenta.isEmpty())){
						if (codigoCuenta.indexOf(codigoImputa) >= 0){
							cambiarCodigo = false;
						}
					}
					if (cambiarCodigo){
						//getView().setValue("codigo", codigoImputa.concat("."));
						getView().setValue("codigo", codigoImputa);
					}
				}
			}
		}
	}

}
