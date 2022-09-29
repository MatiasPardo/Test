package org.openxava.ventas.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Query;

import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.ReferenceView;
import org.openxava.base.model.ObjetoNegocio;
import org.openxava.jpa.XPersistence;
import org.openxava.negocio.model.Sucursal;

@Entity

public class SucursalCliente extends ObjetoNegocio{
	
	public static Cliente buscarCliente(Sucursal sucursal) {
		String sql = "from SucursalCliente s where " +
				"s.sucursal.id = :sucursal ";
		Query query = XPersistence.getManager().createQuery(sql);
		query.setParameter("sucursal", sucursal.getId());
		query.setMaxResults(1);
		List<?> resultList = query.getResultList();
		for(Object result: resultList){
			Cliente cliente = ((SucursalCliente)result).getCliente();
			return cliente;
		}
		return null;		
	}
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	@JoinColumn(unique=true)
	private Sucursal sucursal;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@NoCreate @NoModify
	@ReferenceView("Simple")
	private Cliente cliente;

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
}
