package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.negocio.model.*;
import org.openxava.validators.*;

@Entity

@Views({
	@View(members=
		"Principal[#" + 
				"descripcion;" +
				"empresa, numero, fecha;" +
				"estado, fechaCreacion, usuario;" +
				"moneda; cotizacion;" +
				"cuentaBancaria];" + 
				"observaciones;" +		
		"valores;" 	
	)
})

@Tabs({
	@Tab(
		properties="fecha, numero, estado, moneda.nombre, cotizacion, observaciones, fechaCreacion",
		filter=EmpresaFilter.class,		
		baseCondition=EmpresaFilter.BASECONDITION,
		defaultOrder="${fechaCreacion} desc")
})

public class AcreditarDebitarValores extends Transaccion implements ITransaccionValores{
		
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoModify @NoCreate
	@DescriptionsList(descriptionProperties="codigo, nombre")
	private CuentaBancaria cuentaBancaria;
	
	@ManyToMany
	@ListProperties("tipoValor.nombre, numero, importe, fechaEmision, fechaVencimiento, detalle, banco.nombre")
	@NewAction(value="ItemAcreditarDebitarValores.add")
	@SaveAction(value="ItemTransaccion.save")
	@HideDetailAction(value="ItemTransaccion.hideDetail")
	@RemoveAction(value="ItemTransaccion.remove")
	@RemoveSelectedAction(value="ItemTransaccion.removeSelected")
	@SearchListCondition("${historico} = 'f' AND ${anulado} = 'f' AND ${tipoValor.consolidaAutomaticamente} = 'f'")
	private Collection<Valor> valores;
	
	public CuentaBancaria getCuentaBancaria() {
		return cuentaBancaria;
	}

	public void setCuentaBancaria(CuentaBancaria cuentaBancaria) {
		this.cuentaBancaria = cuentaBancaria;
	}

	public Collection<Valor> getValores() {
		return valores;
	}

	public void setValores(Collection<Valor> valores) {
		this.valores = valores;
	}

	@Override
	public String descripcionTipoTransaccion() {		
		return "Acreditar/Debitar valores";
	}

	@Override
	public void movimientosValores(List<IItemMovimientoValores> lista) {
		for(Valor valor: this.getValores()){
			if (valor.getTesoreria().equals(this.getCuentaBancaria())){
				ItemTransAcreditarDebitarValor itemMovValores = new ItemTransAcreditarDebitarValor();
				itemMovValores.asignarReferenciaValor(valor);
				lista.add(itemMovValores);
				// El efectivo que sirve para consolidar
				BigDecimal coeficiente = valor.getTipoValor().getComportamiento().coeficienteConsolidacion();
				
				ItemTransEfectivo itemMovEfectivo = new ItemTransEfectivo();
				itemMovEfectivo.asignarReferenciaValor(valor);
				itemMovEfectivo.setTipoValorEfectivo(this.cuentaBancaria.getEfectivo());
				itemMovEfectivo.setImporteEfectivo(valor.getImporte().multiply(coeficiente));
				lista.add(itemMovEfectivo);
			}
			else{
				throw new ValidationException(valor.toString() + " no se encuentra en " + this.getCuentaBancaria().toString());
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
	public OperadorComercial operadorFinanciero(){
		return null;
	}

	@Override
	@Hidden
	public Sucursal getSucursalDestino() {
		return null;
	}

	@Override
	public void despuesPersistirMovimientoFinanciero(MovimientoValores item, boolean revierte) {		
	}
}
