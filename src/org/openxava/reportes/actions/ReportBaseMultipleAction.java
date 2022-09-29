package org.openxava.reportes.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

public abstract class ReportBaseMultipleAction extends JasperMultipleReportBaseAction{

private String idFiltroEmpresa = null;
	
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
			String fileName = ConfiguracionERP.fullFileNameReporte(nombreReporte);	 		
			JRXMLs[i] = fileName;			
		}
		return JRXMLs;	
	}
	
	protected Map<String, Object> crearParametros(){
		Map<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("ESQUEMA", ConfiguracionERP.esquemaDB());
		if (this.filtraPorEmpresa()){
			agregarFiltroEmpresa(parametros);
		}
		if (this.filtrarPorSucursal()){
			this.agregarFiltroSucursal(parametros);
		}
		return parametros;
	}
	
	protected abstract String[] getNombresReportes();

	protected boolean filtraPorEmpresa(){
		return false;
	}
	
	protected boolean filtrarPorSucursal(){
		return false;
	}
	
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
	
	private void agregarFiltroSucursal(Map<String, Object> parametros){
		if (this.getIdsSucursalesHabilitadas().isEmpty()){			
			throw new ValidationException("No tiene habilitada ninguna sucursal");
		}
		else{
			if (!Is.emptyString(this.idFiltroSucursal)){
				Sucursal sucursal = (Sucursal)XPersistence.getManager().find(Sucursal.class, this.idFiltroSucursal);
				parametros.put("SUCURSAL_NOMBRE", sucursal.getNombre());
				Collection<String> ids = new LinkedList<String>();
				ids.add(this.idFiltroSucursal);
				parametros.put("FILTROSUCURSALES", ids);				
			}
			else{
				String nombres = "";				
				for(String id: this.getIdsSucursalesHabilitadas()){
					Sucursal sucursal = (Sucursal)XPersistence.getManager().find(Sucursal.class, id);
					if (nombres != "") nombres += ", ";
					nombres += sucursal.getNombre();
				}
				parametros.put("SUCURSAL_NOMBRE", nombres);
				parametros.put("FILTROSUCURSALES", this.getIdsSucursalesHabilitadas());				
			}
		}
	}
	
	private String idFiltroSucursal = null;
	
	private List<String> idSucursalesHabilitadas = null;
	
	public List<String> getIdsSucursalesHabilitadas() {
		if (this.idSucursalesHabilitadas == null){
			this.idSucursalesHabilitadas = new LinkedList<String>();
			List<Sucursal> sucursales = new LinkedList<Sucursal>();
			Sucursal.buscarSucursalesHabilitadas(sucursales);
			for(Sucursal s: sucursales){
				this.idSucursalesHabilitadas.add(s.getId());
			}
		}
		return this.idSucursalesHabilitadas;
	}

	public void setIdFiltroSucursal(String idFiltro) {
		this.idFiltroSucursal = idFiltro;
		
		if (!Is.emptyString(this.idFiltroSucursal)){
			if (!this.getIdsSucursalesHabilitadas().contains(this.idFiltroSucursal)){
				throw new ValidationException("Usuario no esta habilitado para la sucursal");
			}
		}
	}
}
