package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.tesoreria.actions.*;

@Embeddable

public class ItemEgresoValoresE {
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify	
	@DescriptionsList(descriptionProperties="nombre")
	@OnChange(value=OnChangeEmpresaItemE.class)
	private Empresa empresa;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@OnChange(value=OnChangeTesoreriaOrigenItem.class)
	@DescriptionsList(descriptionProperties=("codigo, nombre"),
			depends="empresa",
			condition="${empresa.id} = ?")
	private Tesoreria origen;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify	
	@DescriptionsList(descriptionProperties=("codigo, nombre"), 
					  depends="origen",
					  condition=Tesoreria.CONDITIONVALORESDESCRIPTIONLIST
					  )
	@OnChange(value=OnChangeTipoValorItemValoresE.class)
	private TipoValorConfiguracion tipoValor;	
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties=("numero, importe, fechaVencimiento"), 
	  			depends="origen, tipoValor",
	  			condition="${historico} = 'f' AND ${anulado} = 'f' AND ${tipoValor.consolidaAutomaticamente} = 'f' AND ${tesoreria.id} = ? AND ${tipoValor.id} = ?",
	  			order="${fechaVencimiento} asc"
	)
	@OnChange(value=OnChangeReferenciaItemValoresE.class)
	private Valor referencia;
	
	@Stereotype("MONEY")
	@OnChange(value=OnChangeImporteOriginalItemValores.class)
	private BigDecimal importeOriginal;
	
	@ReadOnly
	private BigDecimal cotizacion;
	
	@Stereotype("MONEY")
	@ReadOnly
	private BigDecimal importe;
	
	@Column(length=100)
	private String detalle;
	
	@Column(length=30)
	private String numero;
	
	private Date fechaEmision = new Date();
	
	private Date fechaVencimiento = new Date();
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	private Banco banco;

	public Tesoreria getOrigen() {
		return origen;
	}

	public void setOrigen(Tesoreria origen) {
		this.origen = origen;
	}

	public TipoValorConfiguracion getTipoValor() {
		return tipoValor;
	}

	public void setTipoValor(TipoValorConfiguracion tipoValor) {
		this.tipoValor = tipoValor;
	}

	public BigDecimal getImporteOriginal() {
		return importeOriginal == null ? BigDecimal.ZERO : importeOriginal;
	}

	public void setImporteOriginal(BigDecimal importeOriginal) {
		this.importeOriginal = importeOriginal;
	}

	public BigDecimal getCotizacion() {
		return cotizacion;
	}

	public void setCotizacion(BigDecimal cotizacion) {
		this.cotizacion = cotizacion;
	}

	public BigDecimal getImporte() {
		return importe == null ? BigDecimal.ZERO : importe;
	}

	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}

	public String getDetalle() {
		return detalle;
	}

	public void setDetalle(String detalle) {
		this.detalle = detalle;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public Date getFechaEmision() {
		return fechaEmision;
	}

	public void setFechaEmision(Date fechaEmision) {
		this.fechaEmision = fechaEmision;
	}

	public Date getFechaVencimiento() {
		return fechaVencimiento;
	}

	public void setFechaVencimiento(Date fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
	}

	public Banco getBanco() {
		return banco;
	}

	public void setBanco(Banco banco) {
		this.banco = banco;
	}

	public Valor getReferencia() {
		return referencia;
	}

	public void setReferencia(Valor referencia) {
		this.referencia = referencia;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}
}
