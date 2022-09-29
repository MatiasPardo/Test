package org.openxava.reportes.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.Sucursal;
import org.openxava.util.*;
import org.openxava.validators.*;


public abstract class ReportBaseAction extends JasperReportBaseAction{

	private String idFiltroEmpresa = null;
	
	private String idFiltroSucursal = null;
	
	private List<String> idsEmpresasHabilitadas = null;
	
	private List<Sucursal> sucursalesHabilitadas = null;
	
	public List<String> getIdsEmpresasHabilitadas() {
		if (idsEmpresasHabilitadas == null){
			this.idsEmpresasHabilitadas = new LinkedList<String>(); 
			Empresa.buscarEmpresasHabilitadas(this.idsEmpresasHabilitadas);
		}
		return idsEmpresasHabilitadas;
	}

	public List<Sucursal> getSucursalesHabilitadas() {
		if (this.sucursalesHabilitadas == null){			
			this.sucursalesHabilitadas = new LinkedList<Sucursal>();
			Sucursal.buscarSucursalesHabilitadas(this.sucursalesHabilitadas);
		}
		return this.sucursalesHabilitadas;
	}
	
	public void setIdFiltroEmpresa(String idFiltroEmpresa) {
		this.idFiltroEmpresa = idFiltroEmpresa;
		
		if (!Is.emptyString(idFiltroEmpresa)){
			if (!this.getIdsEmpresasHabilitadas().contains(idFiltroEmpresa)){
				throw new ValidationException("Usuario no esta habilitado para la empresa");
			}
		}
	}

	public void setIdFiltroSucursal(String idFiltroSucursal) {
		this.idFiltroSucursal = idFiltroSucursal;
		
		if (!Is.emptyString(idFiltroSucursal)){
			boolean habilitada = false;
			for(Sucursal sucursal: this.getSucursalesHabilitadas()){
				if (sucursal.getId().equals(idFiltroSucursal)){
					habilitada = true;
					break;
				}				
			}
			if (!habilitada){
				throw new ValidationException("Usuario no esta habilitado para la sucursal");
			}
		}
	}
	
	@Override
	protected String getJRXML() throws Exception {		
		String nombreReporte = this.getNombreReporte();
		return ConfiguracionERP.fullFileNameReporte(nombreReporte);		
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Map getParameters() throws Exception {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("ESQUEMA", ConfiguracionERP.esquemaDB());
		parameters.put("LOGO", ConfiguracionERP.pathConfig().concat("logo.png"));
		parameters.put("USUARIO", Users.getCurrent());
		parameters.put("FECHAEJECUCION_DATE", new Date());
		this.agregarParametros(parameters);
		if (this.filtraPorEmpresa()){
			agregarFiltroEmpresa(parameters);
		}
		if (this.filtraPorSucursales()){
			agregarFiltroSucursales(parameters);
		}
		return parameters;
	}
	
	protected abstract String getNombreReporte();
	
	protected abstract void agregarParametros(Map<String, Object> parametros);
	
	
	public String archivoJRXML() throws Exception{
		return this.getJRXML();		
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> parametrosReporte() throws Exception{
		return this.getParameters();
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
	
	protected boolean filtraPorSucursales(){
		return false;
	}
	
	private void agregarFiltroSucursales(Map<String, Object> parametros){
		List<Sucursal> sucursales = this.getSucursalesHabilitadas();		
		if (!sucursales.isEmpty()){
			if (!Is.emptyString(this.idFiltroSucursal)){
				Sucursal sucursal = (Sucursal)XPersistence.getManager().find(Sucursal.class, this.idFiltroSucursal);
				parametros.put("SUCURSAL_NOMBRE", sucursal.getNombre());
				Collection<String> ids = new LinkedList<String>();
				ids.add(this.idFiltroSucursal);
				parametros.put("FILTROSUCURSALES", ids);		
			}
			else{
				String nombres = "";
				Collection<String> idsSucursales = new LinkedList<String>();
				for(Sucursal sucursal: sucursales){
					nombres += sucursal.getNombre();
					idsSucursales.add(sucursal.getId());
				}
				parametros.put("SUCURSAL_NOMBRE", nombres);
				parametros.put("FILTROSUCURSALES", idsSucursales);
			}							
		}
		else{
			throw new ValidationException("No tiene habilitada ninguna sucursal");
		}
	}
	
	protected TipoFormatoImpresion formatoImpresion(){
		return null;
	}
	
	@Override
	public void execute() throws Exception {
		
		TipoFormatoImpresion formatoImpresion = this.formatoImpresion();
		if (formatoImpresion != null){
			if (formatoImpresion.equals(TipoFormatoImpresion.Excel)){
				this.setFormat(ReportBaseAction.EXCEL);
			}
			else if (formatoImpresion.equals(TipoFormatoImpresion.PDF)){
				this.setFormat(ReportBaseAction.PDF);
			}
		}
		
		super.execute();
	}
}
