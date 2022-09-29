package org.openxava.ventas.validators;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.jpa.*;
import org.openxava.negocio.model.UnidadMedida;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

@SuppressWarnings("serial")
public class PrecioValidator implements IValidator{

	private String id;
	
	private Producto producto;
	
	private ListaPrecio listaPrecio;

	private Boolean porCantidad;
	
	private BigDecimal desde;
	
	private BigDecimal hasta;
	
	private UnidadMedida unidadMedida;
	
	@Override
	public void validate(Messages errors) throws Exception {
		if ((this.getProducto() != null) && (this.getListaPrecio() != null) && (this.getUnidadMedida() != null)){
			if (!this.getProducto().unidadMedidaPermitida(this.getUnidadMedida())){
				errors.add("unidadMedida_no_permitida", this.getProducto().getCodigo(), this.getUnidadMedida().getNombre());
			}
			else{
				String sql = " from Precio where producto = :producto and listaPrecio = :listaPrecio and unidadMedida = :unidadMedida";
				if (!Is.emptyString(this.getId())){
					sql += " and id != :id";
				}	
				
				if (this.getPorCantidad()){
					
					if (this.getDesde().compareTo(this.getHasta()) >= 0){
						errors.add("Desde debe ser menor a Hasta");
					}
					else{				
						sql += " and (porCantidad = :sinCantidad or (" + 
							   " (desde < :hasta and hasta >= :hasta) or (hasta < :hasta and hasta > :desde) ) )";
						
						Query query = XPersistence.getManager().createQuery(sql);
						// Si hace flush, en una importación puede generar recursividad infinita por ejecucion del validate
						query.setFlushMode(FlushModeType.COMMIT);
						query.setParameter("producto", this.getProducto());
						query.setParameter("listaPrecio", this.getListaPrecio());
						query.setParameter("sinCantidad", Boolean.FALSE);
						query.setParameter("desde", this.getDesde());
						query.setParameter("hasta", this.getHasta());
						query.setParameter("unidadMedida", this.getUnidadMedida());
						if (!Is.emptyString(this.getId())){
							query.setParameter("id", this.getId());
						}
						query.setMaxResults(1);
						List<?> results = query.getResultList();
						if (!results.isEmpty()){
							errors.add("Precio repetido"); 
						}
					}
				}
				else{
					Query query = XPersistence.getManager().createQuery(sql);
					// Si hace flush, en una importación puede generar recursividad infinita por ejecucion del validate
					query.setFlushMode(FlushModeType.COMMIT);
					query.setParameter("producto", this.getProducto());
					query.setParameter("listaPrecio", this.getListaPrecio());
					query.setParameter("unidadMedida", this.getUnidadMedida());
					if (!Is.emptyString(this.getId())){
						query.setParameter("id", this.getId());
					}	
					query.setMaxResults(1);
					List<?> results = query.getResultList();
					if (!results.isEmpty()){
						errors.add("Precio repetido"); 
					}
				}
			}	
		}
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public ListaPrecio getListaPrecio() {
		return listaPrecio;
	}

	public void setListaPrecio(ListaPrecio listaPrecio) {
		this.listaPrecio = listaPrecio;
	}

	public Boolean getPorCantidad() {
		return porCantidad == null ? Boolean.FALSE : porCantidad;
	}

	public void setPorCantidad(Boolean porCantidad) {
		this.porCantidad = porCantidad;
	}

	public BigDecimal getDesde() {
		return desde;
	}

	public void setDesde(BigDecimal desde) {
		this.desde = desde;
	}

	public BigDecimal getHasta() {
		return hasta;
	}

	public void setHasta(BigDecimal hasta) {
		this.hasta = hasta;
	}

	public UnidadMedida getUnidadMedida() {
		return unidadMedida;
	}

	public void setUnidadMedida(UnidadMedida unidadMedida) {
		this.unidadMedida = unidadMedida;
	}	
}
