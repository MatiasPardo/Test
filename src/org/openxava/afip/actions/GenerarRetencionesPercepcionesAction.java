package org.openxava.afip.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.afip.model.InterfazRetPercSufridasAfip;
import org.openxava.base.model.*;
import org.openxava.contabilidad.model.*;
import org.openxava.jpa.*;

public class GenerarRetencionesPercepcionesAction extends ViewBaseAction implements IChainAction{

	private List<String> files = null;
	
	private String tipoInterfaz = null;
	
	public String getTipoInterfaz() {
		return tipoInterfaz;
	}

	public void setTipoInterfaz(String tipoInterfaz) {
		this.tipoInterfaz = tipoInterfaz;
	}
	
	@Override
	public void execute() throws Exception {
		Date fecha = (Date)this.getView().getValue("fecha");
		Empresa empresa = XPersistence.getManager().find(Empresa.class, this.getView().getValue("empresa.id"));		
		
		InterfazRetPercSufridasAfip interfaz = new InterfazRetPercSufridasAfip();
		interfaz.setFecha(fecha);
		interfaz.setEmpresa(empresa);
		interfaz.setTipoInterfaz(this.getTipoInterfaz());
		this.files = new LinkedList<String>();
		interfaz.generarArchivos(files);
		this.closeDialog();
		
		if (!interfaz.getErrores().isEmpty()){
			this.addErrors(interfaz.getErrores());
			this.addWarning("Se han detectado comprobantes con errores");
		}		
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

