package org.openxava.negocio.validators;

import java.util.*;

import javax.persistence.*;

import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.validators.*;

@SuppressWarnings("serial")
public class UnicidadEquivalenciasValidator implements IValidator{
	
	private ObjetoNegocio origen;
	
	private ObjetoNegocio destino;
	
	private String idEntidad;

	@Override
	public void validate(Messages errors) throws Exception {
		if ((this.getOrigen() != null) && (this.getDestino()!= null)){
			if (this.getOrigen().equals(this.getDestino())){
				errors.add("No puede ser las mismas unidades de medida");
			}
			else{
				String sql = "from EquivalenciaUnidadesMedida e where " + 
						"((e.origen.id = :origen and e.destino.id = :destino) or " + 
						"(e.destino.id = :origen and e.origen.id = :destino))";
				if (!Is.emptyString(this.getIdEntidad())){
					sql += " and e.id <> :id";
				}
				Query query = XPersistence.getManager().createQuery(sql);
				query.setParameter("origen", this.getOrigen().getId());
				query.setParameter("destino", this.getDestino().getId());
				if (!Is.emptyString(this.getIdEntidad())){
					query.setParameter("id", this.getIdEntidad());
				}
				query.setMaxResults(1);
				List<?> result = (List<?>)query.getResultList();
				if (!result.isEmpty()){
					errors.add("Equivalencia repetida");
				}
			}
		}
	}

	
	
	public ObjetoNegocio getOrigen() {
		return origen;
	}

	public void setOrigen(ObjetoNegocio origen) {
		this.origen = origen;
	}


	public ObjetoNegocio getDestino() {
		return destino;
	}

	public void setDestino(ObjetoNegocio destino) {
		this.destino = destino;
	}



	public String getIdEntidad() {
		return idEntidad;
	}

	public void setIdEntidad(String idEntidad) {
		this.idEntidad = idEntidad;
	}
}
