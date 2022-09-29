package org.openxava.contabilidad.model;

import java.math.BigDecimal;
import java.util.*;

import org.openxava.base.model.*;

public interface ITransaccionContable {

	public Transaccion ContabilidadTransaccion();

	public Date ContabilidadFecha();

	public String ContabilidadDetalle();

	public void generadorPasesContable(Collection<IGeneradorItemContable> items);
	
	public boolean generaContabilidad();
	
	public boolean contabilizaEnCero();
	
	public EmpresaExterna ContabilidadOriginante();
	
	public BigDecimal ContabilidadTotal();
}
