package org.openxava.ventas.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;

@Entity

@Views({
	@View(members=
	"Principal{ " +
		"descripcion, moneda, cotizacion;" +
		"Principal[#" + 
			"fecha, fechaVencimiento, fechaServicio, fechaCreacion;" +
			"puntoVenta, tipo, estado;" + 
			"empresa, numero;" +
			"Cliente[cliente, razonSocial;" + 
				"cuit, posicionIva, tipoDocumento, condicionVenta];" +
			"domicilioEntrega;" + 
			"observaciones];" +
		"Descuentos[#" +
			"porcentajeDescuento];" +	
		"items;" +
		"Totales[subtotalSinDescuento, descuento;" +
				"total;]" +
	"}" +
	"CuentaCorriente{ctacte}"		
	),
	@View(name="Simple",
			members="numero, estado")
})

@Tab(filter=EmpresaFilter.class,
	baseCondition=EmpresaFilter.BASECONDITION,
	properties="fecha, numero, tipo.tipo, estado, tipoOperacion, cae, fechaVencimientoCAE, cliente.codigo, cliente.nombre, total, subtotal, iva, descuento, subtotalSinDescuento",
	defaultOrder="${fechaCreacion} desc")


public class DebitoInternoVenta extends VentaElectronica{
	
	@ReadOnly
	private Boolean diferenciaCambio = Boolean.FALSE;
	
	public Boolean getDiferenciaCambio() {
		return diferenciaCambio;
	}

	public void setDiferenciaCambio(Boolean diferenciaCambio) {
		this.diferenciaCambio = diferenciaCambio;
	}
	
	@Override
	public void onPreCreate(){
		super.onPreCreate();
		this.setTipoOperacion("Debito");
	}
	
	@Override
	public String CtaCteTipo(){
		return "DEBITO INTERNO";
	}
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Débito de Venta Interno";
	}
	
	@Override
	public Boolean numeraSistema(){
		return true;
	}
	
	@Override
	protected IEstrategiaCancelacionPendiente establecerEstrategiaCancelacionPendiente(){
		if (this.getDiferenciaCambio()){
			EstrategiaCancelacionPendientePorUso estrategia = new EstrategiaCancelacionPendientePorUso();
			for(ItemVentaElectronica item: this.getItems()){
				estrategia.getPendientes().add(item.generadoPorDiferenciaCambio());
			}
			return estrategia;
		}
		else{
			return super.establecerEstrategiaCancelacionPendiente();
		}
	}
	
	@Override
	public boolean generadaPorDiferenciaCambio(){
		return this.getDiferenciaCambio();
	}
	
	@Override
	public Boolean calculaImpuestos(){
		return Boolean.FALSE;
	}

	@Override
	protected Class<?> tipoTransaccionRevierte() {
		return CreditoInternoVenta.class;
	}
}
