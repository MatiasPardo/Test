package org.openxava.cuentacorriente.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.ventas.model.*;

@Entity

@Views({
	@View(members=
			"empresa;" + 
			"cliente;" + 
			"imputacion;" + 
			"DiferenciaCambio[importe, moneda]" )
})

@Tab(
		filter=EmpresaFilter.class,
		properties="fecha, empresa.nombre, imputacion.cliente.codigo, imputacion.cliente.nombre, imputacion.importe, imputacion.origen.fecha, imputacion.origen.tipo, imputacion.origen.numero, imputacion.destino.fecha, imputacion.destino.tipo, imputacion.destino.numero",
		defaultOrder="${fecha} desc", 
		baseCondition=EmpresaFilter.BASECONDITION + " and " + Pendiente.BASECONDITION)


public class DiferenciaCambioDebitoVenta extends DiferenciaCambioVenta{
	
	public DiferenciaCambioDebitoVenta(){
		
	}
	
	public DiferenciaCambioDebitoVenta(Transaccion transaccion){		
		super(transaccion);
	}
	
	@Override
	public String tipoEntidadDestino(Transaccion origen) {
		String tipoEntidad = DebitoVenta.class.getSimpleName();
		if (origen != null){
			if (origen.getEmpresa().getNumero() == 2){
				tipoEntidad = DebitoInternoVenta.class.getSimpleName();
			}
		}
		return tipoEntidad;
	}

	@Override
	public Transaccion crearTransaccionDestino() {
		if (getTipoTrDestino().equals(DebitoVenta.class.getSimpleName())){
			return new DebitoVenta();
		}
		else if (getTipoTrDestino().equals(DebitoInternoVenta.class.getSimpleName())){
			return new DebitoInternoVenta();
		}
		else{
			return null;
		}
	}
}
