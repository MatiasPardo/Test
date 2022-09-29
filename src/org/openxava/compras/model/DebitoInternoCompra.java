package org.openxava.compras.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;

@Entity

@Views({
	@View(members=
		"Auditoria[descripcion, fechaCreacion, usuario];" +
		"Principal[#" + 
				"empresa;" + 
				"fecha, fechaReal, estado;" +
				"tipo, numero, fechaVencimiento, fechaServicio;" + 				
				"proveedor;" + 
				"condicionCompra, moneda, cotizacion;" + 
				"observaciones];" +		
		"items;" +
		"Totales[subtotal, descuento, total];"  
	),
	@View(name="Cerrado", members=
		"Auditoria[descripcion, fechaCreacion, usuario];" +
		"Principal[#" + 
				"empresa;" + 
				"fecha, fechaReal, estado;" +
				"tipo, numero, fechaVencimiento, fechaServicio;" + 				
				"proveedor;" + 
				"condicionCompra, moneda, cotizacion;" + 
				"observaciones];" +		
		"items;" +
		"Totales[subtotal, descuento, total];"	
	),
	@View(name="Simple", members="numero, estado;")
})


@Tab(
		filter=EmpresaFilter.class,
		properties="fecha, numero, cae, estado, proveedor.nombre, total, fechaCreacion, usuario",
		baseCondition=EmpresaFilter.BASECONDITION,
		defaultOrder="${fechaCreacion} desc")

public class DebitoInternoCompra extends CompraElectronica{
	
	@Override
	public void onPreCreate(){
		super.onPreCreate();
		this.setTipoOperacion("Débito Interno");
	}
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Débito Interno de Compra";		
	}
	
	@Override
	public String CtaCteTipo(){
		return "DEBITO INTERNO COMPRA";
	}
	
	@Override
	protected boolean calcularImpuestos(){
		return false;
	}
	
	@Override
	public String viewName(){
		if (this.cerrado()){
			return "Cerrado";
		}
		else{
			return null;
		}
	}
	
	@Override
	protected IEstrategiaCancelacionPendiente establecerEstrategiaCancelacionPendiente(){
		PendienteDebitoInternoCompra pendiente = buscarPendienteDebitoInternoCompra();
		if (pendiente != null){
			EstrategiaCancelacionPendientePorUso estrategia = new EstrategiaCancelacionPendientePorUso();
			estrategia.getPendientes().add(pendiente);
			return estrategia;
		}
		else{
			return super.establecerEstrategiaCancelacionPendiente();
		}		
	}
	
	private PendienteDebitoInternoCompra buscarPendienteDebitoInternoCompra(){
		Query query = XPersistence.getManager().createQuery("from PendienteDebitoInternoCompra where idDebitoCompra = :id");
		query.setParameter("id", this.getId());
		query.setMaxResults(1);
		List<?> results = query.getResultList();
		if (!results.isEmpty()){
			return (PendienteDebitoInternoCompra)results.get(0);
		}
		else{
			return null;
		}
	}
}
