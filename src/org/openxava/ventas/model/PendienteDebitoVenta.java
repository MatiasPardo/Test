package org.openxava.ventas.model;

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
		properties="fecha, ejecutado, rechazoCheque.cheque.numero, rechazoCheque.cheque.importe, rechazoCheque.cliente.codigo, rechazoCheque.cliente.nombre, fechaCumplimiento, fechaUltimaActualizacion, fechaCreacion",
		rowStyles={
			@RowStyle(style="pendiente-ejecutado", property="ejecutado", value="true")	
		},
		defaultOrder="${fechaCreacion} desc", 
		baseCondition=EmpresaFilter.BASECONDITION + " and " + Pendiente.BASECONDITION)


public class PendienteDebitoVenta extends Pendiente{
	
	public PendienteDebitoVenta(){
	}

	public PendienteDebitoVenta(Transaccion origen){
		super(origen);
		this.setRechazoCheque((RechazoChequeTercero)origen);		
	}
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)	
	private RechazoChequeTercero rechazoCheque; 
	
	@Hidden
	@ReadOnly
	@Column(length=32)
	private String idDebitoVenta;
	
	public RechazoChequeTercero getRechazoCheque() {
		return rechazoCheque;
	}

	public void setRechazoCheque(RechazoChequeTercero rechazoCheque) {
		this.rechazoCheque = rechazoCheque;
	}
	
	
	public String getIdDebitoVenta() {
		return idDebitoVenta;
	}

	public void setIdDebitoVenta(String idDebitoVenta) {
		this.idDebitoVenta = idDebitoVenta;
	}

	@Override
	public Transaccion origen() {
		return this.getRechazoCheque();
	}

	@Override
	public String tipoEntidadDestino(Transaccion origen) {
		return DebitoVenta.class.getSimpleName();
	}

	@Override
	public Transaccion crearTransaccionDestino() {
		return new DebitoVenta();
	}
	
	@Override
	public boolean permiteProcesarJunto(Pendiente pendiente) {
		return false;
	}
}
