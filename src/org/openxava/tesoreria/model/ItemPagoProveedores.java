package org.openxava.tesoreria.model;

import java.math.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.negocio.model.*;
import org.openxava.tesoreria.calculators.DiferenciaItemReciboCobranzaCalculator;

@Entity

@Views({
	@View(members=			
			"Origen[empresa, origen];" +
			"tipoValor, pendientePagar, diferencia;" + 		
			"ChequeTercero[referencia];" + 
			"importeOriginal, cotizacion, importe;" + 
			"detalle;" +
			"Datos[" +
				"numero;" + 
				"fechaEmision, fechaVencimiento;" + 
				"banco;" + 
			"];" + 
			"Firmante[" +
				"firmante;" + 
				"cuitFirmante, nroCuentaFirmante];" + 
			"chequera;"	
	)
})

public class ItemPagoProveedores extends ItemEgresoValores{

	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private PagoProveedores pago;

	public PagoProveedores getPago() {
		return pago;
	}

	public void setPago(PagoProveedores pago) {
		this.pago = pago;
	}

	@Override
	public Transaccion transaccion() {
		return pago;
	}

	@Override
	public void asignarOperadorComercial(Valor valor, Transaccion transaccion) {
		valor.setProveedor(((PagoProveedores)transaccion).getProveedor());		
	}
	

	@Override
	public OperadorComercial operadorComercialValores(Transaccion transaccion) {
		return ((PagoProveedores)transaccion).getProveedor();
	}
	
	@Hidden
	@Stereotype("MONEY")
	public BigDecimal getFalta(){
		BigDecimal faltante = BigDecimal.ZERO;
		if (this.getPago() != null){
			if (this.getPago().getaPagar().compareTo(BigDecimal.ZERO) > 0){
				return this.getPago().getFalta();
			}
		}		
		return faltante;
	}
	
	// cuando se crea un nuevo item, se inicializa en ItemPagoProveedoresView
	@Transient
	@ReadOnly @Hidden
	private BigDecimal pendientePagar = BigDecimal.ZERO;
		
	@Transient
	@ReadOnly @Hidden
	@DefaultValueCalculator(
			value=DiferenciaItemReciboCobranzaCalculator.class, 
			properties={@PropertyValue(name="importe", from="importe"),
						@PropertyValue(name="pendiente", from="pendientePagar")
					})
	private String diferencia;
	
	public BigDecimal getPendientePagar() {
		if ((this.getPago() != null) && (this.getPago().getFalta() != null)) {
			
			return this.getPago().getFalta().negate().add(this.getImporte());
		}
		else{
			return pendientePagar;
		}
	}

	public void setPendientePagar(BigDecimal pendientePagar) {
		this.pendientePagar = pendientePagar;		
	}
	
	public String getDiferencia() {
		if (this.getPago() != null){
			return ReciboCobranza.convertirStringPendienteCobrar(this.getImporte().subtract(this.getPendientePagar()));
		}
		else{
			return diferencia;
		}
	}

	public void setDiferencia(String diferencia) {
		this.diferencia = diferencia;
	}
}
