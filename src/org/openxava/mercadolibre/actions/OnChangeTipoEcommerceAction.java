package org.openxava.mercadolibre.actions;

import org.openxava.actions.OnChangePropertyBaseAction;
import org.openxava.mercadolibre.model.Ecommerce;
import org.openxava.view.View;

public class OnChangeTipoEcommerceAction extends OnChangePropertyBaseAction{

	@Override
	public void execute() throws Exception {
		onChangeEcommerce(getNewValue(), getView());		
	}

	public static void onChangeEcommerce(Object valor, View view) {
		if(valor != null){
			if(valor.equals(Ecommerce.MercadoLibre)){
				view.setEditable("codeToken", false);
				view.setEditable("fechaBusqueda", false);				
			}else if(valor.equals(Ecommerce.TiendaNube)){
				view.setEditable("codeToken", true);
				view.setEditable("fechaBusqueda", true);
			} 
		}
	 }
}
