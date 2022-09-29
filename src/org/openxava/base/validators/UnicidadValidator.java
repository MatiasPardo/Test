package org.openxava.base.validators;

import java.util.*;

import javax.persistence.*;

import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.validators.*;

@SuppressWarnings("serial")
public class UnicidadValidator implements IValidator{

	private String modelo = "";
	private String atributo = "";
	private Object valor = "";
	private String id = "";
	private String idMessage = "codigo_repetido";
	
	public String getModelo() {
		return modelo;
	}

	public void setModelo(String modelo) {
		this.modelo = modelo;
	}

	public String getAtributo() {
		return atributo;
	}
	
	public void setAtributo(String atributo) {
		this.atributo = atributo;
	}

	public Object getValor() {
		return valor;
	}

	public void setValor(Object valor) {
		this.valor = valor;
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIdMessage() {
		return idMessage;
	}

	public void setIdMessage(String idMessage) {
		this.idMessage = idMessage;
	}

	@Override
	public void validate(Messages errors) throws Exception {
		if (this.getValor() != null){
			if (!Is.empty(this.getValor())){
				String sql = " from " + this.getModelo() + " where " + 						
						this.getAtributo() + " = :valor";
				if (!Is.emptyString(this.getId())){
					sql += " and id != :id";
				}
				Query query = XPersistence.getManager().createQuery(sql);
				query.setParameter("valor", this.getValor());
				if (!Is.emptyString(this.getId())){
					query.setParameter("id", this.getId());
				}
				query.setMaxResults(1);
				query.setFlushMode(FlushModeType.COMMIT);
				List<?> results = query.getResultList();
				if (!results.isEmpty()){
					errors.add(idMessage, this.getValor().toString()); 
				}
			}
		}
	}

}
