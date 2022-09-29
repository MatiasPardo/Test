package org.openxava.ventas.validators;

import javax.persistence.FlushModeType;
import javax.persistence.Query;

import org.openxava.jpa.XPersistence;
import org.openxava.util.Is;
import org.openxava.util.Messages;
import org.openxava.validators.IValidator;

@SuppressWarnings("serial")
public class TipoOperacionVentaValidator implements IValidator{

	private Boolean transferenciaSucursales = Boolean.FALSE;
	
	private Boolean principal = Boolean.FALSE;
	
	private String idEntidad;
	
	public Boolean getTransferenciaSucursales() {
		return transferenciaSucursales;
	}

	public void setTransferenciaSucursales(Boolean transferenciaSucursales) {
		if (transferenciaSucursales != null){
			this.transferenciaSucursales = transferenciaSucursales;
		}
	}

	public Boolean getPrincipal() {
		return principal;
	}

	public void setPrincipal(Boolean principal) {
		if (principal != null){
			this.principal = principal;
		}
	}

	public String getIdEntidad() {
		return idEntidad;
	}

	public void setIdEntidad(String idEntidad) {
		this.idEntidad = idEntidad;
	}

	@Override
	public void validate(Messages errors) throws Exception {
		if (this.getPrincipal() && this.getTransferenciaSucursales()){
			errors.add("No puede ser principal y transferencia por sucursales");
		}
		else if (this.getTransferenciaSucursales()){
			String sql = "from TipoOperacionVenta where transferenciaSucursales = :transferencia";
			if (!Is.emptyString(this.getIdEntidad())){
				sql += " and id != :id";
			}
			Query query = XPersistence.getManager().createQuery(sql);
			query.setParameter("transferencia", true);
			if (!Is.emptyString(this.getIdEntidad())){
				query.setParameter("id", this.getIdEntidad());
			}
			query.setMaxResults(1);
			query.setFlushMode(FlushModeType.COMMIT);
			if (!query.getResultList().isEmpty()){
				errors.add("Ya existe un tipo de operación que es para transferencia entre sucursales");
			}
		}
	}
}

