package org.openxava.afip.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.FlushModeType;
import javax.persistence.Id;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Query;

import org.openxava.annotations.Hidden;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.Required;
import org.openxava.annotations.View;
import org.openxava.jpa.XPersistence;
import org.openxava.ventas.model.Cliente;

import com.allin.interfacesafip.model.AfipIdioma;
import com.allin.interfacesafip.model.AfipIncoterm;

@Entity

@View(name="Cliente", members="idioma; incoterms; incotermsDescripcion")

public class ClienteFacturaExportacion {
	
	public static ClienteFacturaExportacion buscar(Cliente cliente) {
		Query query = XPersistence.getManager().createQuery("from ClienteFacturaExportacion where cliente = :cliente");
		query.setMaxResults(1);
		query.setParameter("cliente", cliente);
		query.setFlushMode(FlushModeType.COMMIT);
		List<?> results = query.getResultList();
		if (!results.isEmpty()){
			return (ClienteFacturaExportacion)results.get(0);
		}
		else{
			return null;
		}
	}
	
	@Id @Hidden 
	@Column(length=32)
	private String id = "";
	
	@OneToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView("Transaccion")
	@MapsId
	private Cliente cliente;
	
	@Required
	private AfipIdioma idioma = AfipIdioma.Castellano;

	@Required
	private AfipIncoterm incoterms; 
	
	@Column(length=100)
	private String incotermsDescripcion;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public AfipIdioma getIdioma() {
		return idioma;
	}

	public void setIdioma(AfipIdioma idioma) {
		if (idioma != null){
			this.idioma = idioma;
		}
		else{
			this.idioma = AfipIdioma.Castellano;
		}
	}

	public AfipIncoterm getIncoterms() {
		return incoterms;
	}

	public void setIncoterms(AfipIncoterm incoterms) {
		this.incoterms = incoterms;
	}

	public String getIncotermsDescripcion() {
		return incotermsDescripcion;
	}

	public void setIncotermsDescripcion(String incotermsDescripcion) {
		this.incotermsDescripcion = incotermsDescripcion;
	}	
}
