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
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

@Embeddable

public class ItemIngresoValoresE implements IItemMovimientoValores{
	
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify	
	@DescriptionsList(descriptionProperties="nombre")
	@OnChange(value=OnChangeEmpresaItemE.class)
	private Empresa empresa;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="codigo, nombre",
					depends="empresa",
					condition="${empresa.id} = ?")
	@OnChange(value=OnChangeTesoreriaDestinoItem.class)
	private Tesoreria destino;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify	
	@DescriptionsList(descriptionProperties="codigo, nombre", 
					  depends="destino.id",
					  condition=Tesoreria.CONDITIONVALORESDESCRIPTIONLIST
					  )
	@OnChange(value=OnChangeTipoValorItemValoresE.class)
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

	@Hidden
	@ReadOnly
	@Column(length=32)
	private String idValor;
	
	public String getIdValor() {
		return idValor;
	}

	public void setIdValor(String idValor) {
		this.idValor = idValor;
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
	
	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
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
	public TipoMovimientoValores tipoMovimientoValores(boolean reversion) {
		if (!reversion){
			return new TipoMovIngresoValores();
		}
		else{
			return new TipoMovAnulacionIngValores();
		}
		
	}

	public BigDecimal getCotizacion() {
		return this.cotizacion == null ? BigDecimal.ZERO : this.cotizacion;
	}

	public void setCotizacion(BigDecimal cotizacion) {
		this.cotizacion = cotizacion;
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
	public Valor referenciaValor() {
		if (!Is.emptyString(this.getIdValor())){
			return (Valor)XPersistence.getManager().find(Valor.class, this.getIdValor());
		}
		else{
			return null;
		}
	}

	@Override
	public void asignarReferenciaValor(Valor valor) {
		this.setIdValor(valor.getId());		
	}

	@Override
	public String getFirmante() {
		return "";
	}

	@Override
	public String getCuitFirmante() {
		return "";
	}

	@Override
	public String getNroCuentaFirmante() {
		return "";
	}

	@Override
	public void asignarOperadorComercial(Valor valor, Transaccion transaccion) {
		Cliente cliente;
		try {
			cliente = (Cliente)transaccion.getClass().getMethod("getCliente").invoke(transaccion);
			valor.setCliente(cliente);
		} catch (Exception e) {
			throw new ValidationException(e.toString());
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
	public OperadorComercial operadorComercialValores(Transaccion transaccion) {
		Cliente cliente;
		try {
			cliente = (Cliente)transaccion.getClass().getMethod("getCliente").invoke(transaccion);
			return cliente;
		} catch (Exception e) {
			throw new ValidationException(e.toString());
		} 		
	}
	
	@Override
	public ObjetoNegocio itemTrValores(){
		return null;
	}
	
	@Override
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
}
