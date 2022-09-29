package org.openxava.conciliacionbancaria.validators;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.FlushModeType;
import javax.persistence.Query;

import org.openxava.jpa.XPersistence;
import org.openxava.tesoreria.model.CuentaBancaria;
import org.openxava.util.Is;
import org.openxava.util.Messages;
import org.openxava.validators.IValidator;

@SuppressWarnings("serial")
public class ResumenExtractoBancarioValidator implements IValidator{
	
	private String idEntidad; 
	
	private Date desde;
	
	private Date hasta;
	
	private CuentaBancaria cuenta;
	
	@Override
	public void validate(Messages errors) throws Exception {
		if ((this.getDesde() != null) && (this.getHasta() != null) && (this.getCuenta() != null)){
			if (this.getDesde().compareTo(this.getHasta()) > 0){
				errors.add("Desde no puede ser posterior a Hasta");
			}
			else{
				String sql = "from ResumenExtractoBancario where ( (desde < :hasta and hasta >= :hasta) or (hasta < :hasta and hasta > :desde) )" 
						+ " and cuenta.id = :cuenta";
				if (!Is.emptyString(this.getIdEntidad())){
					sql += " and id != :id";
				}
				
				Query query = XPersistence.getManager().createQuery(sql);
				query.setParameter("desde", this.getDesde());
				query.setParameter("hasta", this.getHasta());
				query.setParameter("cuenta", this.getCuenta().getId());
				if (!Is.emptyString(this.getIdEntidad())){ 
					query.setParameter("id", this.getIdEntidad());
				}
				query.setFlushMode(FlushModeType.COMMIT);
				query.setMaxResults(1);
				if (!query.getResultList().isEmpty()){
					SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
					errors.add("Ya existe un resumen de extracto bancario entre las fechas " + format.format(this.getDesde()) + " y " + format.format(this.getHasta()) );
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

	public Date getDesde() {
		return desde;
	}

	public void setDesde(Date desde) {
		this.desde = desde;
	}

	public Date getHasta() {
		return hasta;
	}

	public void setHasta(Date hasta) {
		this.hasta = hasta;
	}

	public CuentaBancaria getCuenta() {
		return cuenta;
	}

	public void setCuenta(CuentaBancaria cuenta) {
		this.cuenta = cuenta;
	}
}
