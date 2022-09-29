package org.openxava.negocio.actions;

import javax.inject.*;

import org.openxava.actions.*;
import org.openxava.base.actions.*;
import org.openxava.tab.*;

public abstract class VerListaParaMultiseleccionAction extends PrimeroGrabarTrDespuesEjecutarItemAction implements INavigationAction{
	
	@Inject 		
	private Tab tab;
	
	public Tab getTab() {
		return tab;
	}

	public void setTab(Tab tab) {
		this.tab = tab;
	}
	
	@Override
	protected void ejecutarAccionItem() throws Exception {
		Tab tab = new Tab();
		tab.setRequest(this.getTab().getRequest());
		
		this.armarListadoMultilseleccion(tab);
		setTab(tab);		
		this.showDialog(getCollectionElementView());
	}

	@Override
	public String[] getNextControllers() throws Exception {
		return new String[]{ getNextController() }; 
	}
	
	public String getNextController() {
		return "Multiseleccion";
	}
	
	public String getCustomView() {		
		return "xava/addToCollection.jsp?rowAction=" + getNextController() + ".view";
	}
	
	protected abstract void armarListadoMultilseleccion(Tab tab);
}