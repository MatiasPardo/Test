package org.openxava.impuestos.model;

import java.math.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;

@Embeddable

public class EscalaImpuesto {
	
	@Stereotype("MONEY")
	private BigDecimal masDe;
	
	@Stereotype("MONEY")
	private BigDecimal hasta;
	
	@Stereotype("MONEY")
	private BigDecimal importeFijo;
	
	@Min(value=0, message="No puede menor a 0")
	@Max(value=100, message="No puede ser mayor a 100")
	private BigDecimal masPorcentaje;

	public BigDecimal getMasDe() {
		return masDe == null ? BigDecimal.ZERO : this.masDe;
	}

	public void setMasDe(BigDecimal masDe) {
		this.masDe = masDe;
	}

	public BigDecimal getHasta() {
		return hasta == null ? BigDecimal.ZERO : this.hasta;
	}

	public void setHasta(BigDecimal hasta) {
		this.hasta = hasta;
	}

	public BigDecimal getImporteFijo() {
		return importeFijo == null ? BigDecimal.ZERO : this.importeFijo;
	}

	public void setImporteFijo(BigDecimal importeFijo) {
		this.importeFijo = importeFijo;
	}

	public BigDecimal getMasPorcentaje() {
		return masPorcentaje == null ? BigDecimal.ZERO : this.masPorcentaje;
	}

	public void setMasPorcentaje(BigDecimal masPorcentaje) {
		this.masPorcentaje = masPorcentaje;
	}
}
