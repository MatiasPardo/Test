package org.openxava.contratos.calculators;

import java.util.Date;

import org.openxava.calculators.ICalculator;
import org.openxava.contratos.model.CicloFacturacion;
import org.openxava.jpa.XPersistence;
import org.openxava.util.Is;

@SuppressWarnings("serial")
public class FechaFacturacionContratoCalculator implements ICalculator{
	
	private String idCiclo;
	
	private Date fecha;
	
	private String tipoFecha;
	
	public String getIdCiclo() {
		return idCiclo;
	}

	public void setIdCiclo(String idCiclo) {
		this.idCiclo = idCiclo;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public String getTipoFecha() {
		return tipoFecha;
	}

	public void setTipoFecha(String tipoFecha) {
		this.tipoFecha = tipoFecha;
	}

	@Override
	public Object calculate() throws Exception {
		Date resultado = null;
		if (!Is.emptyString(this.getIdCiclo()) && this.getFecha() != null){
			CicloFacturacion ciclo = XPersistence.getManager().find(CicloFacturacion.class, this.getIdCiclo());
			ciclo.simularFechasFacturacion(this.getFecha(), true, false);
			if (Is.equalAsString(this.getTipoFecha(), "proximaEmisionFactura")){
				resultado = ciclo.getFechaEmision1();
			}
			else if (Is.equalAsString(this.getTipoFecha(), "proximoVencimientoFactura")){
				resultado = ciclo.getFechaVencimiento1();
			}
		}
		return resultado;
	}
	
}
