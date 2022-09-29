package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.tesoreria.actions.*;

@Entity

@Views({
	@View(members=
		"empresa;" + 
		"origen;" +
		"tipoValor;" + 
		"referencia;" +
		"importeOriginal, importe;" + 
		"detalle;")
})

public class ItemDepositoBanco extends ItemTransaccion implements IItemMovimientoValores{

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	private DepositoBanco depositoBanco;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify	
	@DescriptionsList(descriptionProperties="nombre")
	@ReadOnly
	private Empresa empresa;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView("Simple")
	@SearchAction("ReferenciasItemEgresoValores.buscarOrigen")	
	@NoFrame
	private Tesoreria origen;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify	
	@DescriptionsList(descriptionProperties=("codigo, nombre"), 
					  depends="this.origen",
					  condition=Tesoreria.CONDITIONVALORESDESCRIPTIONLIST + " and ${comportamiento} in (0, 1, 3)"
					  )
	private TipoValorConfiguracion tipoValor;	
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView("SimpleCheque")
	@SearchAction(value="ReferenciasItemEgresoValores.buscarValor")
	@OnChange(value=OnChangeReferenciaItemValores.class)	
	@NoFrame
	private Valor referencia;
	
	@Stereotype("MONEY")
	@OnChange(value=OnChangeImporteOriginalItemValores.class)
	private BigDecimal importeOriginal;
	
	@Stereotype("MONEY")
	@ReadOnly
	private BigDecimal importe;
	
	@Column(length=100)
	private String detalle;
	
	public Tesoreria getOrigen() {
		return origen;
	}

	public void setOrigen(Tesoreria origen) {
		this.origen = origen;
	}

	public Valor getReferencia() {
		return referencia;
	}

	public void setReferencia(Valor referencia) {
		this.referencia = referencia;
	}

	public DepositoBanco getDepositoBanco() {
		return depositoBanco;
	}

	public void setDepositoBanco(DepositoBanco depositoBanco) {
		this.depositoBanco = depositoBanco;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public TipoValorConfiguracion getTipoValor() {
		return tipoValor;
	}

	public void setTipoValor(TipoValorConfiguracion tipoValor) {
		this.tipoValor = tipoValor;
	}

	
	public BigDecimal getImporteOriginal() {
		return importeOriginal == null ? BigDecimal.ZERO : this.importeOriginal;
	}

	public void setImporteOriginal(BigDecimal importeOriginal) {
		this.importeOriginal = importeOriginal;
	}

	public BigDecimal getImporte() {
		return importe  == null ? BigDecimal.ZERO : this.importe;
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

	@Override
	public Transaccion transaccion() {
		return this.getDepositoBanco();
	}

	@Override
	public void recalcular() {
		if (this.getReferencia() != null){
			this.setOrigen(this.getReferencia().getTesoreria());
			this.setTipoValor(this.getReferencia().getTipoValor());
			this.setImporteOriginal(this.getReferencia().getImporte());
		}
		if (this.getTipoValor() != null){
			if (this.getImporteOriginal().compareTo(BigDecimal.ZERO) != 0){
			
				Moneda moneda = this.getTipoValor().getMoneda();
				if (moneda == null){
					// a veces el tipo de valor se instancia solo el Id, pero los demás atributos no están cargados
					moneda = ((TipoValorConfiguracion)XPersistence.getManager().find(TipoValorConfiguracion.class, this.getTipoValor().getId())).getMoneda();
				}	
				BigDecimal cotizacion = BigDecimal.ZERO;
				if (this.transaccion() != null){
					cotizacion = this.transaccion().buscarCotizacionTrConRespectoA(moneda);
					this.setImporte(this.getImporteOriginal().divide(cotizacion, 2, RoundingMode.HALF_EVEN));
				}
			}	
		}
	}

	@Override
	public Banco getBanco() {
		if (this.getReferencia() != null){
			return this.getReferencia().getBanco();
		}
		else{
			return null;
		}
	}

	@Override
	public void setBanco(Banco banco) {	
	}

	@Override
	public Date getFechaEmision() {
		if (this.getReferencia() != null){
			return this.getReferencia().getFechaEmision();
		}
		else{
			if (this.getDepositoBanco() != null){
				return this.getDepositoBanco().getFecha();
			}
			else{
				return new Date();
			}
		}		
	}

	@Override
	public void setFechaEmision(Date fechaEmision) {	
	}

	@Override
	public Date getFechaVencimiento() {
		if (this.getReferencia() != null){
			return this.getReferencia().getFechaVencimiento();
		}
		else{
			return null;
		}
	}

	@Override
	public void setFechaVencimiento(Date fechaVencimiento) {		
	}

	@Override
	@Hidden
	public String getNumeroValor() {
		if (this.getReferencia() != null){
			return this.getReferencia().getNumero();
		}
		else{
			return "";
		}
	}

	@Override
	public void setNumeroValor(String numeroValor) {	
	}

	@Override
	public String getFirmante() {
		if (this.getReferencia() != null){
			return this.getReferencia().getFirmante();
		}
		else{
			return "";
		}		
	}

	@Override
	public String getCuitFirmante() {
		if (this.getReferencia() != null){
			return this.getReferencia().getCuitFirmante();
		}
		else{
			return "";
		}		
	}

	@Override
	public String getNroCuentaFirmante() {
		if (this.getReferencia() != null){
			return this.getReferencia().getNroCuentaFirmante();
		}
		else{
			return "";
		}
	}

	@Override
	public Tesoreria tesoreriaAfectada() {
		return this.getOrigen();
	}

	@Override
	public Tesoreria transfiere(){
		if (this.getDepositoBanco() != null){
			return this.getDepositoBanco().getCuentaBancaria();
		}
		else{
			return null;
		}
	}
	
	@Override
	public Valor referenciaValor() {
		return this.getReferencia();
	}

	@Override
	public void asignarReferenciaValor(Valor valor) {
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
	public void asignarOperadorComercial(Valor valor, Transaccion transaccion) {		
	}

	@Override
	public boolean noGenerarDetalle() {
		return false;
	}

	@Override
	public OperadorComercial operadorComercialValores(Transaccion transaccion) {
		OperadorComercial operador = null;
		Valor valor = this.referenciaValor();
		if (valor != null){
			if (valor.getCliente() != null){
				operador = valor.getCliente();
			}
			else if (valor.getProveedor() != null){
				operador = valor.getProveedor();
			}
		}		
		return operador;
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
	public IChequera chequera(){
		return null;
	}
}
