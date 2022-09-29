package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.contabilidad.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.filter.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

@Entity

@Views({
	@View(members=
		"Principal[#" + 
				"descripcion;" +
				"empresa, moneda, cotizacion;" +
				"numero, fecha, fechaCreacion;" +
				"estado;" + 				
				"observaciones];" +
		"Total[#total];" +		
		"items;" 
	),
	@View(name="Simple", members="numero")
})

@Tabs({
	@Tab(
		properties="empresa.nombre, fecha, numero, estado, moneda.nombre, cotizacion, observaciones, total",
		filter=SucursalEmpresaFilter.class,		
		baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL,
		defaultOrder="${fechaCreacion} desc")
})


public class IngresoFinanzas extends Transaccion implements ITransaccionValores, ITransaccionContable{

	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal total;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal total1;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal total2;
	
	@OneToMany(mappedBy="ingresoFinanzas", cascade=CascadeType.ALL)
	@ListProperties("empresa.nombre, destino.nombre, tipoValor.nombre, importeOriginal, cotizacion, importe, detalle, numero, fechaEmision, fechaVencimiento, banco.nombre")
	@SaveAction(value="ItemTransaccion.save")
	@NewAction(value="ItemTransaccion.new")
	@HideDetailAction(value="ItemTransaccion.hideDetail")
	@RemoveAction(value="ItemTransaccion.remove")
	@RemoveSelectedAction(value="ItemTransaccion.removeSelected")
	@EditAction("ItemTransaccion.edit")
	private Collection<ItemIngresoFinanzas> items;
	
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

	public Collection<ItemIngresoFinanzas> getItems() {
		return items;
	}

	public void setItems(Collection<ItemIngresoFinanzas> items) {
		this.items = items;
	}

	@Override
	public String descripcionTipoTransaccion() {
		return "Ingreso Financiero";
	}
	
	@Override
	protected void atributosConversionesMoneda(List<String> atributos){
		super.atributosConversionesMoneda(atributos);
		atributos.add("Total");	
	}
	
	@Override
	public void recalcularTotales(){
		super.recalcularTotales();
		Sucursal sucursal = this.getSucursal();	
		if (this.getItems() != null){
			BigDecimal importeTotal = BigDecimal.ZERO;
			Collection<ItemIngresoFinanzas> items = this.getItems();		
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
	}

	@Override
	public void movimientosValores(List<IItemMovimientoValores> lista) {
		lista.addAll(this.getItems());		
	}

	@Override
	public boolean revierteFinanzasAlAnular() {		
		return true;
	}

	@Override
	public boolean actualizarFinanzasAlConfirmar() {
		return true;
	}

	@Override
	public void antesPersistirMovimientoFinanciero(MovimientoValores item) {
		if (this.movParaConciliar != null){
			this.movParaConciliar.add(item);
		}
	}
	
	@Override
	public void despuesPersistirMovimientoFinanciero(MovimientoValores item, boolean revierte) {		
	}
	
	@Transient
	private Collection<MovimientoValores> movParaConciliar; 
	
	public void setMovimientosParaConciliar(Collection<MovimientoValores> movimientos) {
		this.movParaConciliar = movimientos;
	}
	
	@Override
	public void agregarParametrosImpresion(Map<String, Object> parameters) {
		super.agregarParametrosImpresion(parameters);
		
		parameters.put("TOTAL", this.getTotal());
	}

	@Override
	public void generadorPasesContable(Collection<IGeneradorItemContable> items) {
 
		for(ItemIngresoFinanzas item: this.getItems()){
			// Debe: tipo de valor
			Tesoreria tesoreria = item.getDestino();
			Query query = XPersistence.getManager().createQuery("from CuentaBancaria where id = :id");
			query.setParameter("id", tesoreria.getId());
			query.setMaxResults(1);
			List<?> result = query.getResultList();
			CuentaContable cuenta = null;
			if (!result.isEmpty()){
				CuentaBancaria cuentaBancaria = (CuentaBancaria)result.get(0);
				cuenta = TipoCuentaContable.Finanzas.CuentaContablePorTipo(cuentaBancaria);
			}
			else{
				cuenta = TipoCuentaContable.Finanzas.CuentaContablePorTipo(item.getTipoValor());
			}
			GeneradorItemContablePorTr paseDebe = new GeneradorItemContablePorTr(this, cuenta);
			paseDebe.setDebe(item.getImporte());
			items.add(paseDebe);
			
			// haber: concepto
			cuenta = TipoCuentaContable.Finanzas.CuentaContablePorTipo(item.getConcepto());
			GeneradorItemContablePorTr paseHaber = new GeneradorItemContablePorTr(this, cuenta);
			paseHaber.setHaber(item.getImporte());
			items.add(paseHaber);
		}				
	}
	
	@Override
	public OperadorComercial operadorFinanciero(){
		return null;
	}
	
	@Override
	protected void asignarSucursal(){
		if (this.getItems() != null){
			// la sucursal se busca en el primer item. Después se valida que todos los items coincidan con las sucursales
			for(ItemIngresoValores item: this.getItems()){
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
	
	@Override
	public EmpresaExterna ContabilidadOriginante() {
		return null;		
	}
	
	@Override
	public BigDecimal ContabilidadTotal() {
		return this.getTotal1();
	}
}
