package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.contabilidad.model.*;
import org.openxava.jpa.*;
import org.openxava.model.*;
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
		"conceptoDefault;" + 
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

public class EgresoFinanzas extends Transaccion implements ITransaccionValores, ITransaccionContable{

	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView(value="Simple")
	@NoCreate @NoModify
	private ConceptoTesoreria conceptoDefault;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal total;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal total1;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal total2;
	
	@OneToMany(mappedBy="egresoFinanzas", cascade=CascadeType.ALL)
	@ListProperties("empresa.nombre, origen.nombre, tipoValor.nombre, importeOriginal, cotizacion, importe, detalle, numero, fechaEmision, fechaVencimiento, banco.nombre")
	@SaveAction(value="ItemTransaccion.save")
	@NewAction(value="ItemTransaccion.new")
	@HideDetailAction(value="ItemTransaccion.hideDetail")
	@RemoveAction(value="ItemTransaccion.remove")
	@RemoveSelectedAction(value="ItemTransaccion.removeSelected")
	@EditAction("ItemTransaccion.edit")
	@ListAction("ColeccionItemsEgresoFinanciero.Cheques")
	private Collection<ItemEgresoFinanzas> items;
	
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
	
	public Collection<ItemEgresoFinanzas> getItems() {
		return items;
	}

	public void setItems(Collection<ItemEgresoFinanzas> items) {
		this.items = items;
	}

	@Override
	protected void atributosConversionesMoneda(List<String> atributos){
		super.atributosConversionesMoneda(atributos);
		atributos.add("Total");	
	}
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Egreso Financiero";
	}
	
	@Override
	public void recalcularTotales(){
		super.recalcularTotales();
		Sucursal sucursal = this.getSucursal();		
		if (this.getItems() != null){
			BigDecimal importeTotal = BigDecimal.ZERO;
			Collection<ItemEgresoFinanzas> items = this.getItems();		
			for(ItemEgresoValores item: items){
				item.recalcular();
				if (!Is.equal(item.getOrigen().getSucursal(), sucursal)){
					Messages error = new Messages();
					error.add("sucursales_items", item.getOrigen().getSucursal().toString(), sucursal.toString());
					throw new ValidationException(error);
				}
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
		parameters.put("TOTAL1", this.getTotal1());
		parameters.put("TOTAL2", this.getTotal2());
		parameters.put("MONEDA", this.getMoneda().getNombre());
	}

	@Override
	public void generadorPasesContable(Collection<IGeneradorItemContable> items) {
 
		for(ItemEgresoFinanzas item: this.getItems()){
			// Haber: tipo de valor
			Tesoreria tesoreria = item.getOrigen();
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
			GeneradorItemContablePorTr paseHaber = new GeneradorItemContablePorTr(this, cuenta);
			paseHaber.setHaber(item.getImporte());
			paseHaber.setCentroCostos(item.getCentroCostos());
			items.add(paseHaber);
			
			// Debe: concepto
			cuenta = TipoCuentaContable.Finanzas.CuentaContablePorTipo(item.getConcepto());
			GeneradorItemContablePorTr paseDebe = new GeneradorItemContablePorTr(this, cuenta);
			paseDebe.setDebe(item.getImporte());
			paseDebe.setCentroCostos(item.getCentroCostos());
			items.add(paseDebe);
		}				
	}
	
	@Override
	public OperadorComercial operadorFinanciero(){
		return null;
	}
	
	public ConceptoTesoreria getConceptoDefault() {
		return conceptoDefault;
	}

	public void setConceptoDefault(ConceptoTesoreria conceptoDefault) {
		this.conceptoDefault = conceptoDefault;
	}

	@Override
	protected boolean agregarItemDesdeMultiseleccion(Map<?, ?> key, Map<String, Object> itemsMultiseleccion){
		try {
			Valor cheque = (Valor)MapFacade.findEntity("Valor", key);
			this.agregarCheque(cheque);
			return true;
		} catch (Exception e) {
			String error = e.getMessage();
			if (Is.emptyString(error)) error = e.toString();
			throw new ValidationException("Error al agregar cheque: " + error);
		}
		
	}
	
	private boolean agregarCheque(Valor cheque){
		boolean chequeRepetido = false;
		for(ItemEgresoFinanzas item: this.getItems()){
			if (item.getReferencia().equals(cheque)){
				chequeRepetido = true;
				break;
			}
		}		
		if (!chequeRepetido){
			this.crearItemEgresoFinanzas(cheque);
			
		}
		return !chequeRepetido;
	}
	
	private ItemEgresoFinanzas crearItemEgresoFinanzas(Valor valor){
		ItemEgresoFinanzas item = new ItemEgresoFinanzas();
		item.setEgresoFinanzas(this);
		item.setConcepto(this.getConceptoDefault());
		item.setEmpresa(valor.getEmpresa());
		item.setOrigen(valor.getTesoreria());
		item.setTipoValor(valor.getTipoValor());
		item.setReferencia(valor);
		item.setImporteOriginal(valor.getImporte());
		item.setDetalle(valor.getDetalle());
		valor.copiarAtributosValoresEnItem(item);
		
		item.recalcular();
		this.getItems().add(item);
		XPersistence.getManager().persist(item);
		return item;
	}
	
	@Override
	protected void asignarSucursal(){
		if (this.getItems() != null){
			// la sucursal se busca en el primer item. Después se valida que todos los items coincidan con las sucursales
			for(ItemEgresoFinanzas item: this.getItems()){
				this.setSucursal(item.getOrigen().getSucursal());
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
