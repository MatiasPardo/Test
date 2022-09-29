package org.openxava.ventas.validators;

import java.util.List;

import javax.persistence.FlushModeType;
import javax.persistence.Query;

import org.openxava.jpa.XPersistence;
import org.openxava.util.Is;
import org.openxava.util.Messages;
import org.openxava.validators.IValidator;

@SuppressWarnings("serial")
public class CondicionVentaValidator implements IValidator{
	
	private String idEntidad = "";
	
	private Boolean principal = Boolean.FALSE;
	
	private Boolean ventas = Boolean.FALSE;
	
	private Boolean compras = Boolean.FALSE;
	
	@Override
	public void validate(Messages errors) throws Exception {
		if (this.getPrincipal()){
			if (!this.getVentas() && !this.getCompras()){
				errors.add("Debe estar activado para compras y/o ventas");
			}
			else{
				String sql = " from CondicionVenta where principal = :principal";
				if (!Is.emptyString(this.getIdEntidad())){
					sql += " and id <> :id";
				}
				boolean parametroVentasCompras = false;
				if (this.getVentas() && !this.getCompras()){
					sql += " and ventas = :valor";
					parametroVentasCompras = true;
				}
				else if (!this.getVentas() && this.getCompras()){
					sql += " and compras = :valor";
					parametroVentasCompras = true;
				}
				
				Query query = XPersistence.getManager().createQuery(sql);
				query.setParameter("principal", Boolean.TRUE);
				if (parametroVentasCompras){
					query.setParameter("valor", Boolean.TRUE);
				}
				if (!Is.emptyString(this.getIdEntidad())){
					query.setParameter("id", this.getIdEntidad());
				}		
				query.setMaxResults(1);
				query.setFlushMode(FlushModeType.COMMIT);
				List<?> results = query.getResultList();
				if (!results.isEmpty()){
					errors.add("Solo puede existir un principal para ventas/compras"); 
				}
			}
		}
	}

	public String getIdEntidad() {
		return idEntidad;
	}

	public void setIdEntidad(String idEntidad) {
		this.idEntidad = idEntidad;
	}

	public Boolean getPrincipal() {
		return principal;
	}

	public void setPrincipal(Boolean principal) {
		if (principal != null){
			this.principal = principal;
		}
	}

	public Boolean getVentas() {
		return ventas;
	}

	public void setVentas(Boolean ventas) {
		if (ventas != null){
			this.ventas = ventas;
		}
	}

	public Boolean getCompras() {
		return compras;
	}

	public void setCompras(Boolean compras) {
		if (compras != null){
			this.compras = compras;
		}
	}
}

