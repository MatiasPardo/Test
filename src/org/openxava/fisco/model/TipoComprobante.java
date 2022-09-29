package org.openxava.fisco.model;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Query;

import org.openxava.afip.model.TipoComprobanteArg;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.Required;
import org.openxava.annotations.View;
import org.openxava.jpa.XPersistence;
import org.openxava.validators.ValidationException;

@Entity

@View(members="id; tipo; regionalidad")

public class TipoComprobante {
	
	public static TipoComprobante buscarPorId(Integer id) {
		TipoComprobante tipo = XPersistence.getManager().find(TipoComprobante.class, id);
		if (tipo == null){
			throw new ValidationException("No se pudo encontrar el tipo de comprobante de id " + id.toString());
		}
		else{
			return tipo;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Collection<TipoComprobante> comprobantesPorRegion(Regionalidad region){
		Query query = XPersistence.getManager().createQuery("from TipoComprobante where regionalidad = :regionalidad");
		query.setParameter("regionalidad", region);
		return query.getResultList();
	}
	
	public static String[] TIPOSTRANSACCIONVENTA = {"FacturaVenta", "FacturaVentaContado", "DebitoVenta", "CreditoVenta"};
	
	public static String[] TIPOSTRANSACCIONCOMPRA = {"FacturaCompra", "DebitoCompra","CreditoCompra"};
	
	@ReadOnly
	@Hidden
	@Id
	private Integer id;
	
	@Required
	@ReadOnly
	@Column(length=8)
	private String tipo;
	
	@Required
	@Hidden
	@ReadOnly
	private Regionalidad regionalidad;
	
	@Override
	public String toString(){
		return this.getTipo();
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public Regionalidad getRegionalidad() {
		return regionalidad;
	}

	public void setRegionalidad(Regionalidad regionalidad) {
		this.regionalidad = regionalidad;
	}
	
	public boolean calculaImpuestos(){
		boolean calcula = true;
		if (this.getRegionalidad().equals(Regionalidad.AR)){
			TipoComprobanteArg tipo = this.tipoComprobanteAfip();
			if (tipo.equals(TipoComprobanteArg.E)){
				calcula = false;			
			}			
		}
		return calcula;
	}
	
	public boolean discriminaIVA(){
		boolean discrimina = true;
		if (this.getRegionalidad().equals(Regionalidad.AR)){
			TipoComprobanteArg tipoComprobante = this.tipoComprobanteAfip();
			discrimina = tipoComprobante.discriminaIVA();
		}
		return discrimina;
	}
	
	public TipoComprobanteArg tipoComprobanteAfip(){
		try{
			return TipoComprobanteArg.values()[this.getId()];
		}
		catch(Exception e){
			throw new ValidationException("No se pudo obtener el tipo de comprobante afip para el tipo " + this.getId().toString());
		}
	}
	
	public Integer codigoFiscal(String tipoTransaccion){
		Integer codigo = null;
		if (this.getRegionalidad().equals(Regionalidad.AR)){
			codigo = TipoComprobanteArg.codigoAfipPorIndice(this.getId(), tipoTransaccion);
		}
		
		if (codigo == null){
			throw new ValidationException("No se pudo obtener el codigo fiscal para el tipo de operación " + tipoTransaccion);
		}
		return codigo;
	}
}
