package org.openxava.impuestos.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.calculators.*;

@Entity

@Views({
	@View(name="Simple", 
		members="calcula;" +  
				"exento, desde, hasta;" + 
				"alicuotaVigente, fechaAlicuotaDesde, fechaAlicuotaHasta;")
})

public class EntidadImpuesto extends ObjetoNegocio{
	
	@DefaultValueCalculator(value=TrueCalculator.class)
	private Boolean calcula = Boolean.TRUE;

	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean exento = Boolean.FALSE;
	
	private Date desde;
	
	private Date hasta;
	
	@ReadOnly
	private BigDecimal alicuotaVigente;
	
	@ReadOnly
	private Date fechaAlicuotaDesde;
	
	@ReadOnly
	private Date fechaAlicuotaHasta;

	@Hidden
	@ReadOnly
	@Column(length=32)
	private String id_externo;
	
	public Boolean getCalcula() {
		return calcula;
	}

	public void setCalcula(Boolean calcula) {
		this.calcula = calcula;
	}

	public Boolean getExento() {
		return exento;
	}

	public void setExento(Boolean exento) {
		this.exento = exento;
	}

	public Date getDesde() {
		return desde;
	}

	public void setDesde(Date desde) {
		this.desde = desde;
	}

	public Date getHasta() {
		return hasta;
	}

	public void setHasta(Date hasta) {
		this.hasta = hasta;
	}

	public BigDecimal getAlicuotaVigente() {
		return alicuotaVigente;
	}

	public void setAlicuotaVigente(BigDecimal alicuotaVigente) {
		this.alicuotaVigente = alicuotaVigente;
	}
	
	public Date getFechaAlicuotaDesde() {
		return fechaAlicuotaDesde;
	}

	public void setFechaAlicuotaDesde(Date fechaAlicuotaDesde) {
		this.fechaAlicuotaDesde = fechaAlicuotaDesde;
	}

	public Date getFechaAlicuotaHasta() {
		return fechaAlicuotaHasta;
	}

	public void setFechaAlicuotaHasta(Date fechaAlicuotaHasta) {
		this.fechaAlicuotaHasta = fechaAlicuotaHasta;
	}

	public boolean debeCalcularImpuesto(Date fecha){
		boolean calcular = false;
		if (this.getCalcula()){
			if (!this.getExento()){
				calcular = true;
			}
			else if ((this.getDesde() != null) && (this.getHasta() != null)){
				if (this.getDesde().compareTo(this.getHasta()) <= 0){
					if ((fecha.compareTo(this.getDesde()) >= 0) && (fecha.compareTo(this.getHasta()) <= 0)){
						calcular = true;
					}
				}
			}
		}
		return calcular;
	}

	public boolean tieneAlicuota(Date fecha) {		
		if ((this.getFechaAlicuotaDesde() != null) && (this.getFechaAlicuotaHasta() != null)){
			if ((this.getFechaAlicuotaDesde().compareTo(fecha) <= 0) && (this.getFechaAlicuotaHasta().compareTo(fecha) >= 0)){
				return true;
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
	}

	public void asignarAlicuota(BigDecimal alicuota, Date vigenciaDesde, Date vigenciaHasta) {
		this.setAlicuotaVigente(alicuota);
		this.setFechaAlicuotaDesde(vigenciaDesde);
		this.setFechaAlicuotaHasta(vigenciaHasta);
	}

	public String getId_externo() {
		return id_externo;
	}

	public void setId_externo(String id_externo) {
		this.id_externo = id_externo;
	}
}
