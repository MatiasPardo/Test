package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.calculators.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.tesoreria.actions.*;
import org.openxava.validators.ValidationException;


@MappedSuperclass

public abstract class ItemEgresoValores extends ItemTransaccion implements IItemMovimientoValores{

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify	
	@DescriptionsList(descriptionProperties="nombre")
	@ReadOnly
	private Empresa empresa;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView("Simple")
	@SearchAction("ReferenciasItemEgresoValores.buscarOrigen")
	/*@DescriptionsList(descriptionProperties=("codigo, nombre"),
			depends="this.empresa",
			condition="${empresa.id} = ?")*/
	@NoFrame
	private Tesoreria origen;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify	
	@DescriptionsList(descriptionProperties=("codigo, nombre"), 
					  depends="this.origen",
					  condition=Tesoreria.CONDITIONVALORESDESCRIPTIONLIST
					  )
	//@OnChange(OnChangeTipoValorItemValores.class)
	private TipoValorConfiguracion tipoValor;	
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView("Simple")
	@SearchAction(value="ReferenciasItemEgresoValores.buscarValor")
	/*@DescriptionsList(descriptionProperties=("numero, importe, fechaVencimiento"), 
	  			depends="this.origen, this.tipoValor",
	  			condition="${historico} = 'f' AND ${anulado} = 'f' AND ${tipoValor.consolidaAutomaticamente} = 'f' AND ${tesoreria.id} = ? AND ${tipoValor.id} = ?",
	  			order="${fechaVencimiento} asc"
	)*/
	@OnChange(value=OnChangeReferenciaItemValores.class)	
	@NoFrame
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
	
	@ManyToOne(fetch=FetchType.LAZY, optional=true)
	@NoModify @NoCreate
	@ReferenceView("Egreso")
	@SearchAction("ReferenciasItemEgresoValores.buscarChequera")	
	private Chequera chequera;
	
	@DefaultValueCalculator(value=CurrentDateCalculator.class)
	private Date fechaEmision = new Date();
	
	private Date fechaVencimiento = new Date();
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
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

	@Override
	public Tesoreria tesoreriaAfectada() {
		return this.getOrigen();
	}

	@Override
	public Tesoreria transfiere(){
		return null;
	}
	
	@Override
	public Valor referenciaValor() {		
		return this.getReferencia();
	}

	@Override
	public void asignarReferenciaValor(Valor valor) {
		this.setReferencia(valor);		
	}

	@Override
	public BigDecimal importeOriginalValores() {
		return this.getImporteOriginal();
	}

	@Override
	public BigDecimal importeMonedaTrValores(Transaccion transaccion) {
		return this.getImporte();
	}

	@Override
	public TipoMovimientoValores tipoMovimientoValores(boolean reversion) {
		if (!reversion){
			return new TipoMovEgresoValores();
		}
		else{
			return new TipoMovAnulacionEgrValores();
		}
	}

	@Override
	public void recalcular() {
		if (this.getReferencia() != null){
			this.setOrigen(this.getReferencia().getTesoreria());
			this.setTipoValor(this.getReferencia().getTipoValor());
		}
		if ((this.getEmpresa() == null) && (this.getOrigen().getEmpresa() != null)){
			this.setEmpresa(this.getOrigen().getEmpresa());
		}
		if (this.getTipoValor() != null){
			if (this.getImporteOriginal().compareTo(BigDecimal.ZERO) != 0){
			
				Moneda moneda = this.getTipoValor().getMoneda();
				if (moneda == null){
					// hay un bug: el tipo de valor se instancia solo el Id, pero los demás atributos no están cargados
					moneda = ((TipoValorConfiguracion)XPersistence.getManager().find(TipoValorConfiguracion.class, this.getTipoValor().getId())).getMoneda();
				}					
				if (this.transaccion() != null){
					this.setCotizacion(this.transaccion().buscarCotizacionTrConRespectoA(moneda));
				}
				this.setImporte(this.getImporteOriginal().divide(this.getCotizacion(), 2, RoundingMode.HALF_EVEN));			
			}	
		}
		
		if (this.getChequera() != null){
			if (!this.getChequera().getCuenta().equals(this.getOrigen())){
				throw new ValidationException("La chequera " + this.getChequera().toString() + " no es de la cuenta bancaria " + this.getOrigen().toString());
			}
		}
	}
	
	@Override
	@Hidden
	public String getFirmante(){
		if (this.getReferencia() != null){
			return this.getReferencia().getFirmante();
		}
		else{
			return "";
		}
	}

	@Override
	@Hidden
	public String getCuitFirmante(){
		if (this.getReferencia() != null){
			return this.getReferencia().getCuitFirmante();
		}
		else{
			return "";
		}
	}
	
	@Override
	@Hidden
	public String getNroCuentaFirmante(){
		if (this.getReferencia() != null){
			return this.getReferencia().getNroCuentaFirmante();
		}
		else{
			return "";
		}
	}
	
	@Override
	public boolean noGenerarDetalle() {
		return false;
	}
	

	@Override
	public ConceptoTesoreria conceptoTesoreria() {
		return null;
	}
	
	@Override
	public ObjetoNegocio itemTrValores(){
		return this;
	}
	
	@Override
	@Hidden
	public String getNumeroValor(){
		return this.getNumero();
	}
	
	@Override
	public void setNumeroValor(String numeroValor){
		this.setNumero(numeroValor);
	}

	public Chequera getChequera() {
		return chequera;
	}

	public void setChequera(Chequera chequera) {
		this.chequera = chequera;
	}
	
	@Override
	public IChequera chequera(){
		return this.getChequera(); 
	}
}
