package org.openxava.negocio.actions;

import javax.inject.*;

import org.openxava.actions.*;
import org.openxava.tab.*;

public class VerElementoMultiseleccionAction extends CollectionElementViewBaseAction{
	private int row = -1;
	
	@Inject
	private Tab tab;

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public Tab getTab() {
		return tab;
	}

	public void setTab(Tab tab) {
		this.tab = tab;
	}

	@Override
	public void execute() throws Exception {
		// por ahora no hace nada, se agregó para que si tipean en una fila del listado no haga nada
	}
}

