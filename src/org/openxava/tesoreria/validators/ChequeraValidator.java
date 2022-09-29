package org.openxava.tesoreria.validators;

import javax.persistence.FlushModeType;
import javax.persistence.Query;

import org.openxava.jpa.XPersistence;
import org.openxava.tesoreria.model.CuentaBancaria;
import org.openxava.util.Is;
import org.openxava.util.Messages;
import org.openxava.validators.IValidator;

@SuppressWarnings("serial")
public class ChequeraValidator implements IValidator{

	private String id;
	
	private Long primerNumero;
	
	private Long ultimoNumero; 
	
	private Long proximoNumero;
	
	private CuentaBancaria cuenta;
	
	public Long getPrimerNumero() {
		return primerNumero;
	}

	public void setPrimerNumero(Long primerNumero) {
		this.primerNumero = primerNumero;
	}

	public Long getUltimoNumero() {
		return ultimoNumero;
	}

	public void setUltimoNumero(Long ultimoNumero) {
		this.ultimoNumero = ultimoNumero;
	}

	public Long getProximoNumero() {
		return proximoNumero;
	}

	public void setProximoNumero(Long proximoNumero) {
		this.proximoNumero = proximoNumero;
	}

	public CuentaBancaria getCuenta() {
		return cuenta;
	}

	public void setCuenta(CuentaBancaria cuenta) {
		this.cuenta = cuenta;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void validate(Messages errors) throws Exception {
		if ((this.getPrimerNumero() != null) && 
			(this.getProximoNumero() != null) && 
			(this.getUltimoNumero() != null)){
			
			if (this.getPrimerNumero().compareTo(this.getUltimoNumero()) > 0){
				errors.add("Primer número debe ser mayor a Primer número");
			}
			else{
				if ((this.getProximoNumero().compareTo(this.getPrimerNumero()) < 0) ||
					(this.getProximoNumero().compareTo(this.getUltimoNumero()) > 0)){
					errors.add("Próximo número debe estar en el rango entre " + this.getPrimerNumero().toString() + " y " + this.getUltimoNumero().toString());
				}
			}
						
			if (this.getCuenta() != null){				
				if (this.getCuenta().getMultiplesChequeras()){
				
					String sql = "from Chequera c where c.cuenta.id = :cuenta and " +
					"((primerNumero <= :ultimoNumero and ultimoNumero >= :ultimoNumero) or (ultimoNumero < :ultimoNumero and ultimoNumero >= :primerNumero))";
					if (!Is.emptyString(this.getId())){
						sql += " and c.id != :id";
					}
					Query query = XPersistence.getManager().createQuery(sql);
					query.setParameter("cuenta", this.getCuenta().getId());
					query.setParameter("primerNumero", this.getPrimerNumero());
					query.setParameter("ultimoNumero", this.getUltimoNumero());
					
					if (!Is.emptyString(this.getId())){
						query.setParameter("id", this.getId());
					}
					query.setMaxResults(1);
					query.setFlushMode(FlushModeType.COMMIT);
					
					if (!query.getResultList().isEmpty()){
						errors.add("Chequera repetida");
					}
				}
				else{
					errors.add("La cuenta bancaria debe tener activo la utilización de múltiples chequeras");
				}
			}
		}		
	}
}
