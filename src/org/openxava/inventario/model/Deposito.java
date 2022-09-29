package org.openxava.inventario.model;

import java.math.*;
import java.util.*;
import java.util.Map.Entry;

import javax.persistence.*;
import javax.validation.ValidationException;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.calculators.*;
import org.openxava.inventario.validators.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.negocio.validators.*;
import org.openxava.ventas.model.Producto;

@Entity

@Views({
	@View(name="Simple",
		members="codigo, nombre"),
	@View(members=
		"Principal[codigo, nombre, activo;" +
				"principal, participaDisponible, consignacion;" + 
				"sucursal];" + 
		"items;"
		)
})

@EntityValidators({
	@EntityValidator(
		value=PrincipalSucursalValidator.class, 
		properties= {
			@PropertyValue(name="idEntidad", from="id"), 
			@PropertyValue(name="modelo", value="Deposito"),
			@PropertyValue(name="sucursal", from="sucursal"),
			@PropertyValue(name="principal")
		}
	),
	@EntityValidator(
			value=DepositoConsignacionValidator.class, 
			properties= {
				@PropertyValue(name="idEntidad", value="id"), 				
				@PropertyValue(name="consignacion")
			}
		)
})

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)	
})

public class Deposito extends ObjetoEstatico {
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private Sucursal sucursal;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean participaDisponible;
	
	@OneToMany(mappedBy="deposito", cascade=CascadeType.ALL)
	@ListProperties("producto.codigo, producto.nombre, stock, reservado, disponible")
	@ReadOnly
	private Collection<Inventario> items;

	@DefaultValueCalculator(FalseCalculator.class)
	private Boolean principal = false;
	
	@DefaultValueCalculator(FalseCalculator.class)
	private Boolean consignacion = Boolean.FALSE;
	
	public Collection<Inventario> getItems() {
		return items;
	}

	public void setItems(Collection<Inventario> items) {
		this.items = items;
	}

	public Boolean getPrincipal() {
		return principal;
	}

	public void setPrincipal(Boolean principal) {
		this.principal = principal;
	}

	public Boolean getParticipaDisponible() {
		return participaDisponible;
	}

	public void setParticipaDisponible(Boolean participaDisponible) {
		this.participaDisponible = participaDisponible;
	}

	public Boolean getConsignacion() {
		return consignacion;
	}

	public void setConsignacion(Boolean consignacion) {
		this.consignacion = consignacion;
	}
	
	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}
	
	public BigDecimal stockDisponible(IItemMovimientoInventario item){
		Producto producto = item.getProducto();
		if (producto == null){
			throw new ValidationException("Producto no asignado");
		}
		BigDecimal disponible = BigDecimal.ZERO;
		String sql = "select coalesce(sum(i.disponible), 0) from " + Esquema.concatenarEsquema("Inventario") + " i " +
				 "where i.deposito_id = :deposito and producto_id = :producto";
		Map<String, Object> parametros = new HashMap<String, Object>();
		if (producto.getLote() && item.getLote() != null){
			sql += " and lote_id = :lote";
			parametros.put("lote", item.getLote().getId());
		}
		if (producto.getDespacho() && item.getDespacho() != null){
			sql += " and despacho_id = :despacho";
			parametros.put("despacho", item.getDespacho().getId());
		}
		
		Query query = XPersistence.getManager().createNativeQuery(sql);
		query.setParameter("deposito", this.getId());
		query.setParameter("producto", producto.getId());
		
		for(Entry<String, Object> parametro: parametros.entrySet()){
			query.setParameter(parametro.getKey(), parametro.getValue());
		}
		query.setFlushMode(FlushModeType.COMMIT);
		query.setMaxResults(1);
		List<?> result = query.getResultList();
		if (!result.isEmpty()){
			disponible = (BigDecimal)result.get(0);
		}
		return disponible;
	}
}
