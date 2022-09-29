package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

import com.allin.interfacesafip.util.NumberToLetterConverter;

@MappedSuperclass

public abstract class IngresoValores extends Transaccion implements ITransaccionValores{

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Cobranza")
	@NoCreate 
    @NoModify
	private Cliente cliente;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal total;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal total1;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal total2;
	
	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public BigDecimal getTotal() {
		return total == null ? BigDecimal.ZERO : total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}
	
	public BigDecimal getTotal1() {
		return total1 == null ? BigDecimal.ZERO : total1;
	}

	public void setTotal1(BigDecimal total1) {
		this.total1 = total1;
	}

	public BigDecimal getTotal2() {
		return total2 == null ? BigDecimal.ZERO : total2;
	}

	public void setTotal2(BigDecimal total2) {
		this.total2 = total2;
	}

	protected abstract Collection<ItemIngresoValores> items();
	
	@Override
	public void recalcularTotales(){
		super.recalcularTotales();
		
		BigDecimal importeTotal = BigDecimal.ZERO;
		Collection<ItemIngresoValores> items = this.items();
		Sucursal sucursal = this.getSucursal();		
		for(ItemIngresoValores item: items){			
			if (!Is.equal(item.getDestino().getSucursal(), sucursal)){
				Messages error = new Messages();
				error.add("sucursales_items", item.getDestino().getSucursal().toString(), sucursal.toString());
				throw new ValidationException(error);
			}			
			item.recalcular();
			importeTotal = importeTotal.add(item.getImporte());
		}
		this.setTotal(importeTotal);
	}
	
	public void movimientosValores(List<IItemMovimientoValores> lista){
		lista.addAll(this.items());
	}
	
	@Override
	public boolean revierteFinanzasAlAnular() {
		return true;
	}
	
	@Override
	public void agregarParametrosImpresion(Map<String, Object> parameters) {
		super.agregarParametrosImpresion(parameters);
		
		parameters.put("RAZONSOCIAL_CLIENTE", this.getCliente().getNombre());
		parameters.put("CODIGO_CLIENTE", this.getCliente().getCodigo());
		parameters.put("CUIT_CLIENTE", this.getCliente().getNumeroDocumento());
		parameters.put("TIPODOCUMENTO_CLIENTE", this.getCliente().getTipoDocumento().toString());
		parameters.put("POSICIONIVA_CLIENTE", this.getCliente().getPosicionIva().getDescripcion());
		parameters.put("DIRECCION_CLIENTE", this.getCliente().getDomicilio().getDireccion());
		parameters.put("CODIGOPOSTAL_CLIENTE", this.getCliente().getDomicilio().getCiudad().getCodigoPostal().toString());
		parameters.put("CIUDAD_CLIENTE", this.getCliente().getDomicilio().getCiudad().getCiudad());
		parameters.put("PROVINCIA_CLIENTE", this.getCliente().getDomicilio().getCiudad().getProvincia().getProvincia());
		parameters.put("MONEDA", this.getMoneda().getNombre());
		parameters.put("TOTAL", this.getTotal());
		parameters.put("TOTAL1", this.getTotal1());
		parameters.put("TOTAL2", this.getTotal2());
		
		String totalLetras = "";
		try{
			totalLetras = NumberToLetterConverter.convertNumberToLetter(this.getTotal());
		}
		catch(Exception e){
		}
		parameters.put("TOTALLETRAS", totalLetras);
		
	}
	
	@Override
	protected void atributosConversionesMoneda(List<String> atributos){
		super.atributosConversionesMoneda(atributos);
		atributos.add("Total");
	}
	
	@Override
	public OperadorComercial operadorFinanciero(){
		return this.getCliente();
	}

	@Override
	protected void asignarSucursal(){
		Collection<ItemIngresoValores> items = this.items();
		if (items != null){
			// la sucursal se busca en el primer item. Después se valida que todos los items coincidan con las sucursales
			for(ItemIngresoValores item: items){
				this.setSucursal(item.getDestino().getSucursal());
				break;
			}
		}
		if (this.getSucursal() == null){
			super.asignarSucursal();
		}		
	}
	
	@Override
	@Hidden
	public Sucursal getSucursalDestino() {
		return null;
	}

	public void recalcularSaldoACobrar() {		
	}
	
	@Override
	public void despuesPersistirMovimientoFinanciero(MovimientoValores item, boolean revierte) {		
	}
}
