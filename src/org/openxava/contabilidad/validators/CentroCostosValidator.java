package org.openxava.contabilidad.validators;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openxava.contabilidad.model.CentroCostos;
import org.openxava.contabilidad.model.DistribucionCentroCosto;
import org.openxava.contabilidad.model.UnidadNegocio;
import org.openxava.jpa.XPersistence;
import org.openxava.util.Is;
import org.openxava.util.Messages;
import org.openxava.validators.IValidator;

@SuppressWarnings("serial")
public class CentroCostosValidator implements IValidator{

	private String idEntidad;
	
	private Boolean distribuye;
	
	private Collection<Object> distribucion;
	
	private UnidadNegocio unidadNegocio;
	
	public String getIdEntidad() {
		return idEntidad;
	}

	public void setIdEntidad(String idEntidad) {
		this.idEntidad = idEntidad;
	}

	public Boolean getDistribuye() {
		return distribuye;
	}

	public void setDistribuye(Boolean distribuye) {
		this.distribuye = distribuye;
	}

	public Collection<Object> getDistribucion() {
		return distribucion;
	}

	public void setDistribucion(Collection<Object> distribucion) {
		this.distribucion = distribucion;
	}

	public UnidadNegocio getUnidadNegocio() {
		return unidadNegocio;
	}

	public void setUnidadNegocio(UnidadNegocio unidadNegocio) {
		this.unidadNegocio = unidadNegocio;
	}

	@Override
	public void validate(Messages errors) throws Exception {
		if (this.getDistribuye()){
			if ((this.getDistribucion() != null) && (!this.getDistribucion().isEmpty())){
				this.validarDistribucionCompleta(errors);
				if (!Is.emptyString(this.getIdEntidad())){
					this.validarReferenciasCirculares(errors, true);
				}				
			}
			else{
				errors.add("Falta distribuir");
			}			
		}
		else{
			if (this.getUnidadNegocio() != null){
				errors.add("La unidad de negocio solo puede asigarse a las distribuciones de centro de costos");
			}			
			if ((this.getDistribucion() != null) && (!this.getDistribucion().isEmpty())){
				errors.add("Debe activar que distribuye o eliminar la distribución de centros de costos");			
			}
		}
		
	}
	
	private void validarDistribucionCompleta(Messages errors){
		BigDecimal porcentaje = BigDecimal.ZERO;
		for(Object distribucion: this.getDistribucion()){
			BigDecimal porcent = BigDecimal.ZERO;
			if (distribucion instanceof DistribucionCentroCosto){
				porcent = ((DistribucionCentroCosto)distribucion).getPorcentaje();
			}
			else{
				porcent = (BigDecimal)((Map<?, ?>)distribucion).get("porcentaje");
			}
			porcentaje = porcentaje.add(porcent);			
		}
		
		if (porcentaje.compareTo(new BigDecimal(100)) != 0){
			errors.add("La suma de los porcentajes debe ser 100");
		}
	}
	
	private void validarReferenciasCirculares(Messages errors, boolean unicoNivel){
		Map<String, Object> ids = new HashMap<String, Object>();
		ids.put(this.getIdEntidad(), null);		
		for(Object distribucion: this.getDistribucion()){
			String id = null;
			if (distribucion instanceof DistribucionCentroCosto){
				id = ((DistribucionCentroCosto) distribucion).getDistribucionCostos().getId();
			}
			else{
				id = ((Map<?, ?>)((Map<?, ?>)distribucion).get("distribucionCostos")).get("id").toString();
			}
			
			if (this.getIdEntidad().equals(id)){
				errors.add("La distribución no puede tener al mismo centro de costos");
			}
			else{
				CentroCostos centroCostos = XPersistence.getManager().find(CentroCostos.class, id);
				if (unicoNivel){
					if (centroCostos.getDistribuye()){
						errors.add(centroCostos.toString() + " es una distribución: solo se puede distribuir en centros de costos que no sean una distribución");
					}
				}
				else{				
					ids.put(id, null);
					this.verificarRefenciasCircularesDistribucionCC(centroCostos, ids, errors);				
					ids.remove(id);
				}
			}	
		}
	}

	private void verificarRefenciasCircularesDistribucionCC(CentroCostos centroCostos, Map<String, Object> ids,
			Messages errors) {		
		if (centroCostos.getDistribuye()){
			for(DistribucionCentroCosto distribucion: centroCostos.getDistribucion()){
				String idDistribucion = distribucion.getDistribucionCostos().getId(); 
				if (ids.containsKey(idDistribucion)){
				 	errors.add("Inconsistencia con el centro de costos " + centroCostos.toString() + ": Referencia Circular " + distribucion.getDistribucionCostos().toString());
				}
				else{
					ids.put(idDistribucion, null);
					this.verificarRefenciasCircularesDistribucionCC(distribucion.getDistribucionCostos(), ids, errors);
					ids.remove(idDistribucion);
				}
			}
		}
	}
	
	

}
