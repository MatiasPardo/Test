package org.openxava.inventario.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.compras.model.*;

@Entity

@Views({
	@View(members=
	"Principal[#" + 
			"fecha, fechaUltimaActualizacion, fechaCreacion;" +
			"cumplido, fechaCumplimiento, empresa];" + 
	"ordenCompra;" 	
	)
})

@Tab(
		filter=EmpresaFilter.class,
		properties="fecha, ejecutado, ordenCompra.numero, proveedor.codigo, proveedor.nombre, fechaCumplimiento, fechaUltimaActualizacion, fechaCreacion",
		rowStyles={
				@RowStyle(style="pendiente-ejecutado", property="ejecutado", value="true")	
		},				
		defaultOrder="${fechaCreacion} desc", 
		baseCondition=EmpresaFilter.BASECONDITION + " and " + Pendiente.BASECONDITION)

public class PendienteRecepcionMercaderia extends Pendiente{
	
	public PendienteRecepcionMercaderia(){
		
	}
	
	public PendienteRecepcionMercaderia(Transaccion origen){
		super(origen);
		this.setOrdenCompra((OrdenCompra)origen);
	}
	
	@Override
	public void inicializar(Transaccion origen){
		super.inicializar(origen);
		for(ItemOrdenCompra item: this.getOrdenCompra().getItems()){
			item.setPendienteRecepcion(item.getCantidad());
		}
	}
	
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	private OrdenCompra ordenCompra;
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private Proveedor proveedor;
	
	public OrdenCompra getOrdenCompra() {
		return ordenCompra;
	}

	public void setOrdenCompra(OrdenCompra ordenCompra) {
		this.ordenCompra = ordenCompra;
		if (ordenCompra != null){
			this.setProveedor(ordenCompra.getProveedor());
		}
	}

	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}

	@Override
	public Transaccion origen() {
		return this.getOrdenCompra();
	}

	@Override
	public String tipoEntidadDestino(Transaccion origen) {		
		return RecepcionMercaderia.class.getSimpleName();
	}

	@Override
	public Transaccion crearTransaccionDestino() {
		return new RecepcionMercaderia();
	}
	
	@Override
	public boolean permiteProcesarJunto(Pendiente pendiente) {
		boolean procesarJuntos = false;
		PendienteRecepcionMercaderia pendienteRecepcion = (PendienteRecepcionMercaderia)pendiente;
		if (pendienteRecepcion.getProveedor().equals(this.getProveedor())){
			procesarJuntos = super.permiteProcesarJunto(pendiente);
		}		
		return procesarJuntos;
	}
	
	@Override
	public void itemsPendientes(List<IItemPendiente> items){
		for(ItemOrdenCompra item: this.getOrdenCompra().getItems()){
			items.add(item.itemPendienteRecepcionMercaderiaProxy());
		}
	}

}
