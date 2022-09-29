package org.openxava.ventas.actions;

import org.openxava.actions.*;

public class ImportarProductoCSVAction extends TabBaseAction implements ILoadFileAction{

	@Override
	public void execute() throws Exception {
		showDialog();		
	}
	
	@Override
	public String[] getNextControllers() throws Exception {
		return new String [] { "ProcesarCSVProducto" };
	}

	@Override
	public String getCustomView() throws Exception {
		return "xava/editors/chooseFile";
	}

	@Override
	public boolean isLoadFile() {
		return true;
	}

	
	

}
