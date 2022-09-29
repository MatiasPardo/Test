package org.openxava.ventas.model;

import javax.persistence.*;

import org.openxava.afip.model.TipoComprobanteArg;
import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.calculators.*;
import org.openxava.fisco.model.Regionalidad;

@Entity

@View(members="puntoVenta;" + 
		"tipoComprobante, tipoComprobanteDescripcion;" +
		"proximoNumero, reservado;")

@Tab(properties="puntoVenta.nombre, puntoVenta.tipo, tipoComprobante, tipoComprobanteDescripcion, proximoNumero, reservado")

public class NumeradorPuntoVenta extends ObjetoNegocio{
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Numerador")
	private PuntoVenta puntoVenta;
	
	@ReadOnly
	private Integer tipoComprobante;
	
	@DefaultValueCalculator(value=IntegerCalculator.class,
			properties={@PropertyValue(name="value", value="1")})
	@Required
	@ReadOnly
	private Long proximoNumero;
	
	@ReadOnly
	private Boolean reservado;
	
	@Hidden
	@Column(length=32)
	private String reservadoPor;
	
	@Hidden
	@Column(length=100)
	private String archivoPendiente;
	
	@Version
	int version;
	
	public PuntoVenta getPuntoVenta() {
		return puntoVenta;
	}

	public void setPuntoVenta(PuntoVenta puntoVenta) {
		this.puntoVenta = puntoVenta;
	}

	public Integer getTipoComprobante() {
		return tipoComprobante;
	}

	public void setTipoComprobante(Integer tipoComprobante) {
		this.tipoComprobante = tipoComprobante;
	}

	public Long getProximoNumero() {
		return proximoNumero;
	}

	public void setProximoNumero(Long proximoNumero) {
		this.proximoNumero = proximoNumero;
	}

	public Boolean getReservado() {
		return reservado;
	}

	public void setReservado(Boolean reservado) {
		this.reservado = reservado;
		
	}

	public String getReservadoPor() {
		return reservadoPor;
	}

	public void setReservadoPor(String reservadoPor) {
		this.reservadoPor = reservadoPor;
		this.reservadoPor = reservadoPor;
		if (reservadoPor != null && !reservadoPor.isEmpty()){
			this.reservado = true;
		}
		else{
			this.reservado = false;
		}
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String formatearNumero(Long numero){
		String ptoVta = getPuntoVenta().getNumero().toString();
		int cantidadDigitosPuntoVenta = this.getPuntoVenta().getDigitosPuntoVenta();
		int cantidadDigitosNumero = this.getPuntoVenta().getDigitosNumero();
		while (ptoVta.length() < cantidadDigitosPuntoVenta){
			ptoVta = "0" + ptoVta;
		}
		
		String num = numero.toString();
		while (num.length() < cantidadDigitosNumero){
			num = "0" + num;
		}
	
		return ptoVta + "-" + num;
	}
			
	public String getTipoComprobanteDescripcion(){
		// FALTA REGIONALIZAR
		if (this.region().equals(Regionalidad.AR)){
			return TipoComprobanteArg.descripcionTipoComprobantePorCodigoAfip(this.getTipoComprobante());
		}
		else{
			return "";
		}
	}

	public String getArchivoPendiente() {
		return archivoPendiente;
	}

	public void setArchivoPendiente(String archivoPendiente) {
		this.archivoPendiente = archivoPendiente;
	}
	
	public Regionalidad region(){
		return Regionalidad.AR;
	}
}
