package org.openxava.ventas.model;

import java.math.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.clasificadores.model.*;
import org.openxava.negocio.calculators.*;
import org.openxava.negocio.model.*;

@Entity

@Views({
	@View(members=
			"cliente, marca;" + 
			"porcentajeComercial, tipoPorcentajeComercial;")
})

public class DescuentoVenta extends ObjetoNegocio{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@Required
	@ReferenceView("Simple")
	private Cliente cliente;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@Required
	@DescriptionsList(descriptionProperties="codigo, nombre")
	private Marca marca;
	
	@Min(value=0, message="No puede menor a 0")
	@Max(value=100, message="No puede ser mayor a 100")
	private BigDecimal porcentajeComercial = BigDecimal.ZERO;
	
	@DefaultValueCalculator(TipoPorcentajeDescuentoDefaultCalculator.class)
	@Required
	private TipoPorcentaje tipoPorcentajeComercial = TipoPorcentaje.Descuento;
	
	

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Marca getMarca() {
		return marca;
	}

	public void setMarca(Marca marca) {
		this.marca = marca;
	}
	
	public BigDecimal getPorcentajeComercial() {
		return porcentajeComercial == null ? BigDecimal.ZERO : this.porcentajeComercial;
	}

	public void setPorcentajeComercial(BigDecimal porcentajeComercial) {
		this.porcentajeComercial = porcentajeComercial;
	}

	@Override
	protected void onPrePersist(){
		super.onPrePersist();
		
		this.inicializarDescuentos();
	}
	
	protected void onPreUpdate(){
		super.onPreUpdate();
		
		this.inicializarDescuentos();
	}

	public TipoPorcentaje getTipoPorcentajeComercial() {
		return tipoPorcentajeComercial;
	}

	public void setTipoPorcentajeComercial(TipoPorcentaje tipoPorcentajeComercial) {
		this.tipoPorcentajeComercial = tipoPorcentajeComercial;
	}

	private void inicializarDescuentos() {
		if (this.porcentajeComercial == null){
			this.porcentajeComercial = BigDecimal.ZERO;
		}		
	}
	
	public BigDecimal porcentajeComercialCalculo(){
		if (this.getTipoPorcentajeComercial().equals(TipoPorcentaje.Descuento)){
			return this.getPorcentajeComercial().negate();
		}
		else{
			return this.getPorcentajeComercial();
		}
	}
}
