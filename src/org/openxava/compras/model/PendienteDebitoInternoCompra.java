package org.openxava.compras.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.tesoreria.model.*;

@Entity

@View(members=
"Principal[#" + 
		"fecha, fechaUltimaActualizacion, fechaCreacion;" +
		"cumplido, fechaCumplimiento, empresa];" + 
"rechazoCheque;" 
)

@Tab(
	filter=EmpresaFilter.class,
	properties="fecha, ejecutado, rechazoCheque.numero, rechazoCheque.cheque.numero, rechazoCheque.cheque.importe, rechazoCheque.proveedor.codigo, rechazoCheque.proveedor.nombre, fechaCumplimiento, fechaUltimaActualizacion, fechaCreacion",
	rowStyles={
		@RowStyle(style="pendiente-ejecutado", property="ejecutado", value="true")	
	},
	defaultOrder="${fechaCreacion} desc", 
	baseCondition=EmpresaFilter.BASECONDITION + " and " + Pendiente.BASECONDITION)


public class PendienteDebitoInternoCompra extends Pendiente{
	
	public PendienteDebitoInternoCompra(){
	}

	public PendienteDebitoInternoCompra(Transaccion origen){
		super(origen);
		this.setRechazoCheque((RechazoChequeTercero)origen);		
	}
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)	
	private RechazoChequeTercero rechazoCheque; 
	
	@Hidden
	@ReadOnly
	@Column(length=32)
	private String idDebitoCompra;

	public RechazoChequeTercero getRechazoCheque() {
		return rechazoCheque;
	}

	public void setRechazoCheque(RechazoChequeTercero rechazoCheque) {
		this.rechazoCheque = rechazoCheque;
	}

	public String getIdDebitoCompra() {
		return idDebitoCompra;
	}

	public void setIdDebitoCompra(String idDebitoCompra) {
		this.idDebitoCompra = idDebitoCompra;
	}
	
	@Override
	public Transaccion origen() {
		return this.getRechazoCheque();
	}

	@Override
	public String tipoEntidadDestino(Transaccion origen) {
		return DebitoInternoCompra.class.getSimpleName();
	}

	@Override
	public Transaccion crearTransaccionDestino() {
		return new DebitoInternoCompra();
	}
	
	@Override
	public boolean permiteProcesarJunto(Pendiente pendiente) {
		return false;
	}
}
