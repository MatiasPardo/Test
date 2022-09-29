package org.openxava.base.actions;

import org.openxava.actions.*;
import org.openxava.util.*;
import org.openxava.validators.*;

public class ImportarCSVGenericoAction extends TabBaseAction implements ILoadFileAction{
	
	private String nombreControlador;
	
	public String getNombreControlador() {
		return nombreControlador;
	}

	public void setNombreControlador(String nombreControlador) {
		this.nombreControlador = nombreControlador;
	}

	@Override
	public void execute() throws Exception {
		if (Is.emptyString(this.getNombreControlador())){
			throw new ValidationException("Cuando define la acción en el XML, falta poner el valor de NombreControlador");
		}
		showDialog();		
	}
	
	@Override
	public String[] getNextControllers() throws Exception {
		return new String [] { this.getNombreControlador() };
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
