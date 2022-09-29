package org.openxava.tesoreria.model;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.base.filter.EmpresaFilter;
import org.openxava.negocio.filter.SucursalEmpresaFilter;
import org.openxava.negocio.model.*;
import org.openxava.tesoreria.calculators.DiferenciaItemReciboCobranzaCalculator;

@Entity

@Views({
	@View(members=
			"destino;" + 
			"tipoValor, pendienteCobrar, diferencia;" +
			"importeOriginal, cotizacion, importe;" + 
			"detalle;" +
			"Datos[" +
				"numero;" + 
				"fechaEmision, fechaVencimiento;" + 
				"banco;" + 
			"];" + 
			"Firmante[" +
				"firmante;" + 
				"cuitFirmante, nroCuentaFirmante" +
			"]" + 
			"Tarjeta[" +
				"lote, cupon;" +
			"]"),
	@View(name="CuponTarjetaCredito", 
		members=
			"reciboCobranza;" +
			"destino;" + 
			"tipoValor, importe;" +
			"detalle;" +
			"Tarjeta[" +
				"lote, cupon;" +
			"]")
})

@Tab(name="CuponesTarjetaCredito",
	properties="reciboCobranza.fecha, reciboCobranza.numero, tipoValor.nombre, lote, cupon, destino.nombre, importe, empresa.nombre, detalle, fechaCreacion, usuario, reciboCobranza.cliente.nombre",
	filter=SucursalEmpresaFilter.class,		
	baseCondition=SucursalEmpresaFilter.SUCURSAL_CONDITION_ITEMS1 + "reciboCobranza" + SucursalEmpresaFilter.SUCURSAL_CONDITION_ITEMS2 
		+ " and " + EmpresaFilter.BASECONDITION  + " and e.reciboCobranza.estado = 1 and e.tipoValor.comportamiento = 5",
defaultOrder="${fechaCreacion} asc")

public class ItemReciboCobranza extends ItemIngresoValores implements ICuponTarjeta{
	

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	@ReferenceView(value="ItemReciboCobranza")
	private ReciboCobranza reciboCobranza;
		
	@Hidden
	@Column(length=32)	
	@ReadOnly
	private String idItemCobranza;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly
	@ReferenceView("Simple")
	private LiquidacionTarjetaCredito liquidacionTarjeta;
	
	public String getIdItemCobranza() {
		return idItemCobranza;
	}

	public void setIdItemCobranza(String idItemCobranza) {
		this.idItemCobranza = idItemCobranza;
	}

	public ReciboCobranza getReciboCobranza() {
		return reciboCobranza;
	}

	public void setReciboCobranza(ReciboCobranza reciboCobranza) {
		this.reciboCobranza = reciboCobranza;
	}

	@Override
	public Transaccion transaccion() {
		return this.getReciboCobranza();
	}

	@Override
	public void asignarOperadorComercial(Valor valor, Transaccion transaccion) {
		valor.setCliente(((ReciboCobranza)transaccion).getCliente());		
	}
	
	@Override
	public OperadorComercial operadorComercialValores(Transaccion transaccion) {
		return ((ReciboCobranza)transaccion).getCliente();	
	}
	
	// cuando se crea un nuevo item, se inicializa en ItemReciboCobranzaView
	@Transient
	@ReadOnly @Hidden
	private BigDecimal pendienteCobrar = BigDecimal.ZERO;
	
	@Transient
	@ReadOnly @Hidden
	@DefaultValueCalculator(
			value=DiferenciaItemReciboCobranzaCalculator.class, 
			properties={@PropertyValue(name="importe", from="importe"),
						@PropertyValue(name="pendiente", from="pendienteCobrar")
					})
	private String diferencia;

	public BigDecimal getPendienteCobrar() {
		if ((this.getReciboCobranza() != null) && (this.getReciboCobranza().getDiferencia() != null)) {
			
			return this.getReciboCobranza().getDiferencia().negate().add(this.getImporte());
		}
		else{
			return pendienteCobrar;
		}
	}

	public void setPendienteCobrar(BigDecimal pendienteCobrar) {
		this.pendienteCobrar = pendienteCobrar;		
	}

	public String getDiferencia() {
		if (this.getReciboCobranza() != null){
			return ReciboCobranza.convertirStringPendienteCobrar(this.getImporte().subtract(this.getPendienteCobrar()));
		}
		else{
			return diferencia;
		}
	}

	public void setDiferencia(String diferencia) {
		this.diferencia = diferencia;
	}
	
	private Integer cupon;
	
	private Integer lote;
	
	public Integer getCupon() {
		return cupon;
	}

	public void setCupon(Integer cupon) {
		this.cupon = cupon;
	}

	public Integer getLote() {
		return lote;
	}

	public void setLote(Integer lote) {
		this.lote = lote;
	}
	
	@Override
	public void propiedadesOcultas(List<String> ocultar, List<String> visualizar) {
		super.propiedadesOcultas(ocultar, visualizar);
		
		// Se compara si el comportamiento esta asignado, porque aunque es un valor obligatorio, 
		// cuando se crea un item desde nuevo, viene asignado el tipo de valor sin comportamiento 
		if (this.getTipoValor() != null && this.getTipoValor().getComportamiento() != null){
			if (this.getTipoValor().getComportamiento().equals(TipoValor.TarjetaCreditoCobranza)){
				visualizar.add("Tarjeta");
			}
			else{
				ocultar.add("Tarjeta");
			}
		}
		else{
			ocultar.add("Tarjeta");			
		}
	}
	
	@Override
	public String viewName(){
		if (this.getTipoValor() != null){
			if (this.getTipoValor().getComportamiento().equals(TipoValor.TarjetaCreditoCobranza)){
				return "CuponTarjetaCredito"; 
			}
			else{
				return super.viewName();
			}
		}
		else{
			return super.viewName();
		}
	}

	@Override
	public LiquidacionTarjetaCredito getLiquidacionTarjeta() {
		return liquidacionTarjeta;
	}

	@Override
	public void setLiquidacionTarjeta(LiquidacionTarjetaCredito liquidacionTarjeta) {
		this.liquidacionTarjeta = liquidacionTarjeta;		
	}

	@Override
	public BigDecimal importeCupon() {
		return this.getImporteOriginal();
	}	
}
