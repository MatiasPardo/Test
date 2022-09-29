package org.openxava.afip.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.afip.model.*;
import org.openxava.base.model.*;
import org.openxava.contabilidad.model.*;
import org.openxava.jpa.*;

public class InterfazRegimenInformacionCompraVentaAction extends ViewBaseAction implements IChainAction{

	private List<String> files = null;
	
	
	@Override
	public void execute() throws Exception {
		Date fecha = (Date)this.getView().getValue("fecha");
		Empresa empresa = XPersistence.getManager().find(Empresa.class, this.getView().getValue("empresa.id"));		
		Integer secuencia = this.getView().getValueInt("secuencia");
		
		RegimenInformacionCompraVenta citi = new RegimenInformacionCompraVenta();
		citi.setEmpresa(empresa);
		citi.setFecha(fecha);
		citi.setSecuencia(secuencia);
		this.files = new LinkedList<String>();
		citi.generarArchivos(files);
				
		this.closeDialog();					
	}

	@Override
	public String getNextAction() throws Exception {
		if (!this.files.isEmpty()){
			ArrayList<String> filesID = new ArrayList<String>();
			filesID.addAll(this.files);
			this.getRequest().setAttribute("facearg_filesID", filesID);
			return "Base.descargarArchivos";
		}
		else{
			return null;
		}
	}
}
