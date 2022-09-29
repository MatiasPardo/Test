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

@MappedSuperclass

public abstract class ItemIngresoValores extends ItemTransaccion implements IItemMovimientoValores{
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@ReadOnly
	private Empresa empresa;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView(value="SimpleConEmpresa")
	@SearchAction("ReferenciasItemIngresoValores.buscarDestino")
	/*@DescriptionsList(descriptionProperties="codigo, nombre",
					depends="this.empresa",
					condition="${empresa.id} = ?")*/
	private Tesoreria destino;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify	
	@DescriptionsList(descriptionProperties=("codigo, nombre"), 
					  depends="this.destino",
					  condition=Tesoreria.CONDITIONVALORESDESCRIPTIONLIST
					  )
	@OnChange(OnChangeTipoValorItemIngresoValores.class)
	private TipoValorConfiguracion tipoValor;

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
	
	@DefaultValueCalculator(value=CurrentDateCalculator.class)
	private Date fechaEmision = new Date();
	
	@DefaultValueCalculator(value=CurrentDateCalculator.class)
	private Date fechaVencimiento = new Date();
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	private Banco banco;
	
	@Column(length=50)
	private String firmante;
	
	@Column(length=20)
	private String cuitFirmante;
	
	@Column(length=50)
	private String nroCuentaFirmante;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly
	private Valor referencia;

	public Empresa getEmpresa() {
		if (this.empresa == null){
			if (this.destino != null){
				return this.destino.getEmpresa();
			}
			else{
				return null;
			}
		}
		else{
			return empresa;
		}
	}

	public void setEmpresa(Empresa empresa) {		
		this.empresa = empresa;
	}

	public Tesoreria getDestino() {
		return destino;
	}

	public void setDestino(Tesoreria destino) {
		this.destino = destino;		
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
		return cotizacion == null ? BigDecimal.ZERO : cotizacion;
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
		int max = 100;
		if ((detalle != null) && (detalle.length() > max)){
			this.detalle = detalle.substring(0, max - 1);
		}
		else{
			this.detalle = detalle;
		}		
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
		if (fechaEmision == null){
			this.fechaEmision = UtilERP.trucarDateTime(new Date());
		}
		else{
			this.fechaEmision = fechaEmision;
		}
	}

	public Date getFechaVencimiento() {
		return fechaVencimiento;
	}

	public void setFechaVencimiento(Date fechaVencimiento) {
		if (fechaVencimiento == null){
			this.fechaVencimiento = UtilERP.trucarDateTime(new Date());;
		}
		else{
			this.fechaVencimiento = fechaVencimiento;
		}
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
	
	public String getFirmante() {
		return firmante;
	}

	public void setFirmante(String firmante) {
		this.firmante = firmante;
	}

	public String getCuitFirmante() {
		return cuitFirmante;
	}

	public void setCuitFirmante(String cuitFirmante) {
		this.cuitFirmante = cuitFirmante;
	}

	public String getNroCuentaFirmante() {
		return nroCuentaFirmante;
	}

	public void setNroCuentaFirmante(String nroCuentaFirmante) {
		this.nroCuentaFirmante = nroCuentaFirmante;
	}

	@Override
	public Tesoreria tesoreriaAfectada() {
		return this.getDestino();
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
			return new TipoMovIngresoValores();
		}
		else{
			return new TipoMovAnulacionIngValores();
		}
	}
	
	@Override
	public ConceptoTesoreria conceptoTesoreria(){
		return null;
	}
	
	@Override
	public void recalcular() {
		if ((this.getEmpresa() == null) && (this.getDestino() != null)){
			this.setEmpresa(this.getDestino().getEmpresa());
		}
		if (this.getTipoValor() != null){
			if (this.getImporteOriginal().compareTo(BigDecimal.ZERO) != 0){
			
				Moneda moneda = this.getTipoValor().getMoneda();
				if (moneda == null){
					// hay un bug: el tipo de valor se instancia solo el Id, pero los demás atributos no están cargados
					moneda = ((TipoValorConfiguracion)XPersistence.getManager().find(TipoValorConfiguracion.class, this.getTipoValor().getId())).getMoneda();
				}					
				if (this.transaccion() != null){
					BigDecimal cotizacionMonedaTr = this.transaccion().buscarCotizacionTrConRespectoA(moneda);
					this.setCotizacion((new BigDecimal(1)).divide(cotizacionMonedaTr, 2, RoundingMode.HALF_EVEN));
					this.setImporte(this.getImporteOriginal().divide(cotizacionMonedaTr, 2, RoundingMode.HALF_EVEN));
				}
							
			}	
		}
	}
	
	@Override
	protected void onPrePersist() {
		super.onPrePersist();
		
		if (this.getDestino() != null){
			this.setEmpresa(this.getDestino().getEmpresa());
		}
	}
	
	@Override
	protected void onPreUpdate() {
		super.onPreUpdate();
		
		if (this.getDestino() != null){
			this.setEmpresa(this.getDestino().getEmpresa());
		}
	}
	
	@Override
	public boolean noGenerarDetalle() {
		return false;
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
	
	@Override
	public IChequera chequera(){
		return null;
	}
	
	@Override
	public void propiedadesOcultas(List<String> ocultar, List<String> visualizar) {
		super.propiedadesOcultas(ocultar, visualizar);
		if (this.getTipoValor() != null && this.getTipoValor().getComportamiento() != null){
			if (this.getTipoValor().getComportamiento().equals(TipoValor.ChequePropio) || 
				this.getTipoValor().getComportamiento().equals(TipoValor.ChequePropioAutomatico) || 
				this.getTipoValor().getComportamiento().equals(TipoValor.ChequeTercero) ){
				visualizar.add("Datos");
				visualizar.add("Firmante");
			}
			else{
				ocultar.add("Datos");
				ocultar.add("Firmante");
			}
		}
		else{
			ocultar.add("Datos");
			ocultar.add("Firmante");
		}
	}
}
