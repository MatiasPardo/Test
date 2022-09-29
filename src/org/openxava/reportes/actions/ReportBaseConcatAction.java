package org.openxava.reportes.actions;

import java.io.*;
import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.tab.*;
import org.openxava.util.*;
import org.openxava.validators.*;

public abstract class ReportBaseConcatAction extends JasperConcatReportBaseAction{

	
	protected abstract String[] getNombresReportes();

	@Override
	protected String[] getJRXMLs() throws Exception {
		
		String[] reportes = this.getNombresReportes(); 
		if (reportes == null){
			throw new ValidationException("No esta definido los reportes");
		}
		else if (reportes.length == 0){
			throw new ValidationException("No esta definido los reportes");
		}
		
		String[] JRXMLs = new String[reportes.length];
		for (int i = 0; i < reportes.length; i++) {
			String nombreReporte = reportes[i];
			String fileName = ConfiguracionERP.pathConfig().concat(nombreReporte); 		
			try{
				FileInputStream file = new FileInputStream(fileName);
				JRXMLs[i] = fileName;
				file.close();
			}
			catch(FileNotFoundException e){
				String fileNameGenerico = ConfiguracionERP.pathConfigAplicacionDefault().concat(nombreReporte);
				try{
					FileInputStream file = new FileInputStream(fileNameGenerico);
					JRXMLs[i] = fileNameGenerico;
					file.close();
				}
				catch(FileNotFoundException e2){
					throw new ValidationException("No se encontró " + fileName + " ni tampoco " + fileNameGenerico);
				}
			}
		}
		return JRXMLs;	
	}
	
	protected Map<String, Object> crearParametros(){
		Map<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("ESQUEMA", ConfiguracionERP.esquemaDB());
		parametros.put("LOGO", ConfiguracionERP.pathConfig().concat("logo.png"));
		if (this.filtraPorEmpresa()){
			this.agregarFiltroEmpresa(parametros);
		}
		return parametros;
	}
	
	protected boolean filtraPorEmpresa(){
		return false;
	}
	
	private void agregarFiltroEmpresa(Map<String, Object> parametros){
		if (this.getIdsEmpresasHabilitadas().isEmpty()){			
			throw new ValidationException("No tiene habilitada ninguna empresa");
		}
		else{
			if (!Is.emptyString(this.idFiltroEmpresa)){
				Empresa empresa = (Empresa)XPersistence.getManager().find(Empresa.class, this.idFiltroEmpresa);
				parametros.put("EMPRESA_NOMBRE", empresa.getNombre());
				Collection<String> ids = new LinkedList<String>();
				ids.add(this.idFiltroEmpresa);
				parametros.put("FILTROEMPRESAS", ids);				
			}
			else{
				String nombres = "";				
				for(String id: this.getIdsEmpresasHabilitadas()){
					Empresa empresa = (Empresa)XPersistence.getManager().find(Empresa.class, id);
					if (nombres != "") nombres += ", ";
					nombres += empresa.getNombre();
				}
				parametros.put("EMPRESA_NOMBRE", nombres);
				parametros.put("FILTROEMPRESAS", this.getIdsEmpresasHabilitadas());				
			}
		}
	}
	
	private String idFiltroEmpresa = null;
	
	private List<String> idsEmpresasHabilitadas = null;
	
	public List<String> getIdsEmpresasHabilitadas() {
		if (idsEmpresasHabilitadas == null){
			this.idsEmpresasHabilitadas = new LinkedList<String>(); 
			Empresa.buscarEmpresasHabilitadas(this.idsEmpresasHabilitadas);
		}
		return idsEmpresasHabilitadas;
	}

	public void setIdFiltroEmpresa(String idFiltroEmpresa) {
		this.idFiltroEmpresa = idFiltroEmpresa;
		
		if (!Is.emptyString(idFiltroEmpresa)){
			if (!this.getIdsEmpresasHabilitadas().contains(idFiltroEmpresa)){
				throw new ValidationException("Usuario no esta habilitado para la empresa");
			}
		}
	}
	
	protected void deseleccionarElementos(boolean closeDialog){
		Tab tab = (Tab)this.getContext().get(getRequest(), "xava_tab");
		if (tab != null){
			tab.deselectAll();
		}
		
		if (closeDialog){
			this.closeDialog();
		}
	} 
}
