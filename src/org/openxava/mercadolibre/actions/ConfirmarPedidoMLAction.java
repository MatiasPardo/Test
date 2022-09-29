package org.openxava.mercadolibre.actions;

import org.openxava.actions.IChainAction;
import org.openxava.actions.ViewBaseAction;
import org.openxava.mercadolibre.model.ItemPedidoML;
import org.openxava.model.MapFacade;

public class ConfirmarPedidoMLAction extends ViewBaseAction implements IChainAction{

	@Override
	public void execute() throws Exception {
		ItemPedidoML item = (ItemPedidoML)MapFacade.findEntity(this.getView().getModelName(), this.getView().getKeyValues());
		item.getPedido().confirmarTransaccion();
		
		this.addMessage("ejecucion_OK");
	}

	@Override
	public String getNextAction() throws Exception {
		return "CRUD.searchReadOnly";
	}

}
