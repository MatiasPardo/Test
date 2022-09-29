package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import org.openxava.base.model.*;
import org.openxava.negocio.model.*;

public abstract class ItemTransMovimientoValores implements IItemMovimientoValores{

	private Valor valor;
	
	private boolean noDetalle = false;
	
	private Tesoreria tesoreria;

	private IItemMovimientoValores generadoPor;

	public IItemMovimientoValores getGeneradoPor() {
		return generadoPor;
	}

	public void setGeneradoPor(IItemMovimientoValores generadoPor) {
		this.generadoPor = generadoPor;
		if (generadoPor != null){
			this.valor = generadoPor.referenciaValor();
		}
	}

	
	@Override
	public TipoValorConfiguracion getTipoValor() {
		if (this.referenciaValor() != null){
			return this.referenciaValor().getTipoValor();
		}
		else{
			return null;
		}
	}

	@Override
	public Banco getBanco() {
		if (this.referenciaValor() != null){
			return this.referenciaValor().getBanco();
		}
		else{
			return null;
		}
	}

	@Override
	public String getDetalle() {
		if (this.getGeneradoPor() != null){
			return this.getGeneradoPor().getDetalle();			
		}
		else if (this.referenciaValor() != null){
			return this.referenciaValor().getDetalle();
		}
		else{
			return null;
		}
	}

	@Override
	public Date getFechaEmision() {
		if (this.referenciaValor() != null){
			return this.referenciaValor().getFechaEmision();
		}
		else{
			return null;
		}
	}

	@Override
	public Date getFechaVencimiento() {
		if (this.referenciaValor() != null){
			return this.referenciaValor().getFechaVencimiento();
		}
		else{
			return null;
		}
	}

	@Override
	public String getNumeroValor() {
		if (this.referenciaValor() != null){
			return this.referenciaValor().getNumero();
		}
		else{
			return null;
		}
	}

	@Override
	public void setNumeroValor(String numeroValor) {
		if (this.referenciaValor() != null){
			this.referenciaValor().setNumero(numeroValor);
		}
	}

	@Override
	public Empresa getEmpresa() {
		if (this.referenciaValor() != null){
			return this.referenciaValor().getEmpresa();
		}
		if (this.getGeneradoPor() != null){
			return this.getGeneradoPor().getEmpresa();
		}		
		else if (this.getTesoreria() != null){
			return this.getTesoreria().getEmpresa();
		}
		else{
			return null;
		}
	}

	@Override
	public void setEmpresa(Empresa empresa) {
		if (this.referenciaValor() != null){
			this.referenciaValor().setEmpresa(empresa);
		}		
	}

	@Override
	public Tesoreria tesoreriaAfectada(){
		if (this.getTesoreria() != null){
			return this.getTesoreria();
		}
		else if (this.getGeneradoPor() != null){
			return this.getGeneradoPor().tesoreriaAfectada();
		}
		else if (this.referenciaValor() != null){
			return this.referenciaValor().getTesoreria();
		}
		else{
			return null;
		}
	}
	
	@Override
	public IChequera chequera(){
		if (this.getGeneradoPor() != null){
			return this.getGeneradoPor().chequera();
		}
		else{
			return null;
		}
	}
	
	@Override
	public Valor referenciaValor() {
		return this.valor;
	}

	@Override
	public void asignarReferenciaValor(Valor valor) {
		this.valor = valor;		
	}

	@Override
	abstract public BigDecimal importeOriginalValores();

	@Override
	public abstract BigDecimal importeMonedaTrValores(Transaccion transaccion);

	@Override
	public abstract TipoMovimientoValores tipoMovimientoValores(boolean reversion);
	
	@Override
	public String getFirmante(){
		if (this.referenciaValor() != null){
			return this.referenciaValor().getFirmante();
		}
		else{
			return "";
		}
	}
	
	@Override
	public String getCuitFirmante(){
		if (this.referenciaValor() != null){
			return this.referenciaValor().getCuitFirmante();
		}
		else{
			return "";
		}
	}
	
	@Override
	public String getNroCuentaFirmante(){
		if (this.referenciaValor() != null){
			return this.referenciaValor().getNroCuentaFirmante();
		}
		else{
			return "";
		}
	}
	
	@Override
	public boolean noGenerarDetalle() {
		return noDetalle;
	}
	
	public void setNoGenerarDefalle(boolean noGenerar){
		this.noDetalle = noGenerar;
	}
	
	@Override
	public Tesoreria transfiere(){
		return null;
	}
	
	public Tesoreria getTesoreria() {
		return tesoreria;
	}

	public void setTesoreria(Tesoreria tesoreria) {
		this.tesoreria = tesoreria;
	}
	
	@Override
	public OperadorComercial operadorComercialValores(Transaccion transaccion) {
		if (this.getGeneradoPor() != null){
			return this.getGeneradoPor().operadorComercialValores(transaccion);
		}
		else if (this.referenciaValor() != null){
			if (this.referenciaValor().getCliente() != null){
				return this.referenciaValor().getCliente();
			}
			else if (this.referenciaValor().getProveedor() != null){
				return this.referenciaValor().getProveedor();
			}
			else{
				return null;
			}
		}
		else{
			return null;
		}
	}
	
	@Override
	public ConceptoTesoreria conceptoTesoreria(){
		if (this.getGeneradoPor() != null){
			return this.getGeneradoPor().conceptoTesoreria();
		}
		else{
			return null;
		}
	}
	
	@Override
	public ObjetoNegocio itemTrValores(){
		if (this.getGeneradoPor() != null){
			return this.getGeneradoPor().itemTrValores();
		}
		else{
			return null;
		}
	}	
}
