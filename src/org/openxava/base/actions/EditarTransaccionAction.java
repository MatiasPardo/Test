package org.openxava.base.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;


public class EditarTransaccionAction extends EditarObjetoNegocioAction implements IHideActionsAction, IShowActionsAction, IChainAction{

	private String trNextAction = null;
	
	private List<String> showActions = new LinkedList<String>();
	private List<String> hideActions = new LinkedList<String>();
	
	@Override
	public void execute() throws Exception{
		if (this.getObjetoNegocio() instanceof Transaccion){
			Transaccion transaccion = (Transaccion) this.getObjetoNegocio();
			this.showActions.clear();
			this.hideActions.clear();
			transaccion.accionesValidas(showActions, hideActions);
		}
		
		super.execute();		
	}
	
	@Override
	public String[] getActionsToShow() {
		
		String[] actions = new String[showActions.size()];
		int i = 0;
		for (String action: showActions){
			actions[i] = action;
			i++;
		}
		return actions;
	}

	@Override
	public String[] getActionsToHide() {
		String[] actions = new String[hideActions.size()];
		int i = 0;
		for (String action: hideActions){
			actions[i] = action;
			i++;
		}
		return actions;
	}
	
	@Override
	public String getNextAction() throws Exception {
		return this.getTrNextAction();
	}

	public String getTrNextAction() {
		return trNextAction;
	}

	public void setTrNextAction(String trNextAction) {
		this.trNextAction = trNextAction;
	}
}

	
	

