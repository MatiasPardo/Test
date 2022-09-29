package org.openxava.base.actions;

import org.openxava.actions.*;

public class CerrarItemTransaccionAction extends HideDetailElementInCollectionAction implements IChainAction{

	@Override
	public String getNextAction() throws Exception {
		return "Transaccion.editar";
	}

}
