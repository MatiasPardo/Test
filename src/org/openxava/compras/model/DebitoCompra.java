package org.openxava.compras.model;

import java.util.Collection;
import java.util.LinkedList;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.Estado;
import org.openxava.base.model.Trazabilidad;
import org.openxava.tesoreria.model.LiquidacionTarjetaCredito;
import org.openxava.util.Is;

@Entity

@Views({
	@View(members=
		"Auditoria[descripcion, fechaCreacion, usuario];" +
		"Principal[#" + 
				"fecha, fechaReal, estado;" +
				"tipo, numero, fechaVencimiento, fechaServicio;" + 
				"cae, fechaVencimientoCAE;" +
				"proveedor;" + 
				"condicionCompra, moneda, cotizacion;" + 
				"observaciones];" +
		"impuestos;" +
		"items;" +
		"Totales[subtotal, iva, otrosImpuestos, total];" + 		
		"Descuentos[subtotalSinDescuento, descuento];" 
	),
	@View(name="Cerrado", members=
		"Auditoria[descripcion, fechaCreacion, usuario];" +
		"Principal[#" + 
				"fecha, fechaReal, estado;" +
				"tipo, numero, fechaVencimiento, fechaServicio;" + 
				"cae, fechaVencimientoCAE;" +
				"proveedor;" + 
				"condicionCompra, moneda, cotizacion;" + 
				"observaciones];" + 	
		"items;" +
		"impuestos;" + 
		"Totales[subtotal, iva, otrosImpuestos, total];" +
		"Descuentos[subtotalSinDescuento, descuento];"
	),
	@View(name="Simple", members="numero, estado;")
})


@Tab(
		filter=EmpresaFilter.class,
		properties="fecha, numero, cae, estado, proveedor.nombre, total, subtotal, iva, fechaCreacion, usuario",
		baseCondition=EmpresaFilter.BASECONDITION,
		defaultOrder="${fechaCreacion} desc")


public class DebitoCompra extends CompraElectronica{
	
	@Override
	public void onPreCreate(){
		super.onPreCreate();
		this.setTipoOperacion("Débito");
	}
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Débito de Compra";		
	}
	
	@Override
	public String CtaCteTipo(){
		return "DEBITO COMPRA";
	}
	
	@Override
	protected boolean numeroRepetido(){
		return this.numeroRepetidoCompra();
	}
	
	@Transient
	private LiquidacionTarjetaCredito liquidacionTarjeta;
	
	public void notificarLiquidacionTarjeta(LiquidacionTarjetaCredito liquidacionTarjeta){
		this.liquidacionTarjeta = liquidacionTarjeta;
	}
	
	@Override
	public boolean generaCtaCte(){
		boolean generadaPorLiquidacion = false;
		if (this.liquidacionTarjeta != null){
			generadaPorLiquidacion = true;
		}
		else if (Is.equal(this.getEstado(), Estado.Anulada)){
			// Se busca en la trazabilidad origen, si se generó por una liquidación
			Collection<Trazabilidad> trazabilidad = new LinkedList<Trazabilidad>();
			Trazabilidad.buscarTrazabilidadPorTipo(trazabilidad, this, false, LiquidacionTarjetaCredito.class.getSimpleName());
			generadaPorLiquidacion = !trazabilidad.isEmpty();
		}
		
		if (generadaPorLiquidacion){
			return false;
		}
		else{
			return super.generaCtaCte();
		}
	}
}
