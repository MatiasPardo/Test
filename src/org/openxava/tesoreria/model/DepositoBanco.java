package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.contabilidad.model.*;
import org.openxava.jpa.*;
import org.openxava.model.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

@Entity

@Views({
	@View(members=
		"Principal{" + 
				"descripcion, fechaCreacion;" +
				"empresa, moneda, cotizacion;" +
				"numero, fecha, estado;" +
				"cuentaBancaria;" + 				
				"observaciones;" +
		"Total[#total];" +		
		"items;}" + 
		"Trazabilidad{trazabilidad}"
	),
	@View(name="Simple", members="numero")
})

@Tabs({
	@Tab(
		properties="empresa.nombre, fecha, numero, estado, cuentaBancaria.nombre, moneda.nombre, cotizacion, observaciones, total",
		filter=EmpresaFilter.class,		
		baseCondition=EmpresaFilter.BASECONDITION,
		defaultOrder="${fechaCreacion} desc")
})

public class DepositoBanco extends Transaccion implements ITransaccionValores, ITransaccionContable{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="codigo, nombre", 
				depends="empresa",
				condition="${empresa.id} = ?")
	private CuentaBancaria cuentaBancaria;
	
	@OneToMany(mappedBy="depositoBanco", cascade=CascadeType.ALL)
	@ListProperties("empresa.nombre, origen.nombre, tipoValor.nombre, referencia.numero, importeOriginal, importe, detalle")
	@SaveAction(value="ItemTransaccion.save")
	@NewAction(value="ItemTransaccion.new")
	@HideDetailAction(value="ItemTransaccion.hideDetail")
	@RemoveAction(value="ItemTransaccion.remove")
	@RemoveSelectedAction(value="ItemTransaccion.removeSelected")
	@EditAction("ItemTransaccion.edit")
	@ListAction("ColeccionItemsDepositoBanco.Cheques")
	private Collection<ItemDepositoBanco> items;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal total;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal total1;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal total2;
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Depósito Bancario";
	}

	public CuentaBancaria getCuentaBancaria() {
		return cuentaBancaria;
	}

	public void setCuentaBancaria(CuentaBancaria cuentaBancaria) {
		this.cuentaBancaria = cuentaBancaria;
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

	public Collection<ItemDepositoBanco> getItems() {
		return items;
	}

	public void setItems(Collection<ItemDepositoBanco> items) {
		this.items = items;
	}

	@Override
	public void movimientosValores(List<IItemMovimientoValores> lista) {
		for(ItemDepositoBanco item: this.getItems()){
			IItemMovimientoValores egreso = item;		
			lista.add(egreso);
			
			TipoValor comportamiento = egreso.getTipoValor().getComportamiento();
			TipoValorConfiguracion tipoValorEfectivo = null;
			boolean generarMovIngEfectivo = false;
			if (comportamiento.equals(TipoValor.Efectivo)){
				if (egreso.tesoreriaAfectada().esCuentaBancaria()){
					throw new ValidationException("No se puede depositar efectivo entre cuentas bancarias. Debe Utilizar una transferencia");
				}				
				generarMovIngEfectivo = true;
				tipoValorEfectivo = item.getTipoValor();
			}
			else if (comportamiento.equals(TipoValor.TransferenciaBancaria)){
				if (!egreso.tesoreriaAfectada().esCuentaBancaria()){
					throw new ValidationException("No se puede hacer una transferencia bancaria desde una caja.");
				}
				generarMovIngEfectivo = true;
				tipoValorEfectivo = item.getTipoValor();
			}
			else if (comportamiento.equals(TipoValor.ChequeTercero)){
				tipoValorEfectivo = item.getTipoValor().consolidaCon(item.transfiere());
				if (tipoValorEfectivo != null){
					generarMovIngEfectivo = true;
				}
				else{
					throw new ValidationException("Transferencia de cheque de tercero: no se encontró el efectivo en la cuenta bancaria");
				}				
			}
			
			if (generarMovIngEfectivo){
				// Si transfiere, hace un ingreso en el destino de la transferencia
				ItemTransEfectivo itemMovEfectivo = new ItemTransEfectivo();
				itemMovEfectivo.setTipoValorEfectivo(tipoValorEfectivo);
				itemMovEfectivo.setImporteEfectivo(item.importeOriginalValores());
				itemMovEfectivo.setTesoreria(item.transfiere());
				itemMovEfectivo.setGeneradoPor(item);
				lista.add(itemMovEfectivo);
			}
		}
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
	}
	
	@Override
	public void despuesPersistirMovimientoFinanciero(MovimientoValores item, boolean revierte) {		
	}
	
	@Override
	public void recalcularTotales(){
		super.recalcularTotales();
	
		if (this.getItems() != null){
			BigDecimal importeTotal = BigDecimal.ZERO;
			Collection<ItemDepositoBanco> items = this.getItems();		
			for(ItemDepositoBanco item: items){
				item.recalcular();
				importeTotal = importeTotal.add(item.getImporte());
			}
			this.setTotal(importeTotal);
		}
	}

	@Override
	public void generadorPasesContable(Collection<IGeneradorItemContable> items) {

		// haber: tipo de valor
		BigDecimal total = BigDecimal.ZERO;
		for(ItemDepositoBanco item: this.getItems()){
			if (item.getTipoValor().getComportamiento().equals(TipoValor.TransferenciaBancaria)){
				CuentaBancaria ctaBan = XPersistence.getManager().find(CuentaBancaria.class, item.tesoreriaAfectada().getId());
				CuentaContable cuenta = TipoCuentaContable.Finanzas.CuentaContablePorTipo(ctaBan);
				GeneradorItemContablePorTr paseHaber = new GeneradorItemContablePorTr(this, cuenta);
				paseHaber.setHaber(item.getImporte());
				items.add(paseHaber);
			}
			else{
				CuentaContable cuenta = TipoCuentaContable.Finanzas.CuentaContablePorTipo(item.getTipoValor());
				GeneradorItemContablePorTr paseHaber = new GeneradorItemContablePorTr(this, cuenta);
				paseHaber.setHaber(item.getImporte());
				items.add(paseHaber);
			}
			total = total.add(item.getImporte());
		}
		
		// debe: cuenta bancaria
		CuentaContable cuenta = TipoCuentaContable.Finanzas.CuentaContablePorTipo(this.getCuentaBancaria());
		GeneradorItemContablePorTr paseDebe = new GeneradorItemContablePorTr(this, cuenta);
		paseDebe.setDebe(total);
		items.add(paseDebe);		
	}
	
	@Override
	protected void atributosConversionesMoneda(List<String> atributos){
		super.atributosConversionesMoneda(atributos);
		atributos.add("Total");	
	}
	
	@Override
	public OperadorComercial operadorFinanciero(){
		return null;
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
		for(ItemDepositoBanco item: this.getItems()){
			if (item.getReferencia().equals(cheque)){
				chequeRepetido = true;
				break;
			}
		}		
		if (!chequeRepetido){
			this.crearItemDepositoBanco(cheque);
			
		}
		return !chequeRepetido;
	}
	
	private ItemDepositoBanco crearItemDepositoBanco(Valor valor){
		ItemDepositoBanco item = new ItemDepositoBanco();
		item.setDepositoBanco(this);
		item.setEmpresa(valor.getEmpresa());
		item.setOrigen(valor.getTesoreria());
		item.setTipoValor(valor.getTipoValor());
		item.setReferencia(valor);
		item.setImporteOriginal(valor.getImporte());
		item.setDetalle(valor.getDetalle());
		
		item.recalcular();
		this.getItems().add(item);
		XPersistence.getManager().persist(item);
		return item;
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
