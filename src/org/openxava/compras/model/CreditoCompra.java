package org.openxava.compras.model;

import java.math.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.EstrategiaCancelacionPendientePorUso;
import org.openxava.base.model.IEstrategiaCancelacionPendiente;
import org.openxava.inventario.model.EgresoPorDevolucion;
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
		"Totales[subtotal, iva, otrosImpuestos, total];"  
	),
	@View(name="Simple", members="numero, estado;")	
})

@Tab(
		filter=EmpresaFilter.class,
		properties="fecha, numero, cae, estado, proveedor.nombre, total, subtotal, iva, fechaCreacion, usuario",
		baseCondition=EmpresaFilter.BASECONDITION,
		defaultOrder="${fechaCreacion} desc")

public class CreditoCompra extends CompraElectronica{
	
	@Override
	public void onPreCreate(){
		super.onPreCreate();
		this.setTipoOperacion("Cr?dito");
	}
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Cr?dito de Compra";		
	}
	
	@Override
	public String CtaCteTipo(){
		return "CREDITO COMPRA";
	}
	
	@Override
	public BigDecimal CtaCteImporte() {
		return this.getTotal().negate();
	}
	
	@Override
	public BigDecimal CtaCteNeto() {
		return this.getSubtotal().negate();
	}
	
	@Override
	protected boolean numeroRepetido(){
		return this.numeroRepetidoCompra();
	}
	
	@Override
	public Integer CtaCteCoeficiente() {
		return -1;
	}
	
	@Override
	protected IEstrategiaCancelacionPendiente establecerEstrategiaCancelacionPendiente(){
		if (Is.equalAsString(this.getTipoEntidadCreadaPor(), EgresoPorDevolucion.class.getSimpleName())){
			EstrategiaCancelacionPendientePorUso estrategia = new EstrategiaCancelacionPendientePorUso();			
			estrategia.getPendientes().add(PendienteCreditoCompra.buscarPendienteCreditoVenta(this));
			return estrategia;
		}
		else{
			return super.establecerEstrategiaCancelacionPendiente();
		}
	}
}
