package org.openxava.impuestos.model;

import java.util.*;

import org.openxava.compras.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

import com.allin.percepciones.model.*;

public class CalculadorRetencionesCABA extends CalculadorRetencionIngresosBrutos{

	@Override
	protected EntidadImpuesto determinarEntidadImpuesto(Proveedor proveedor) {
		return proveedor.getRetencionCABA();
	}

	@Override
	protected AlicuotaPadron buscarAlicuotaPadron(Date fecha, Proveedor proveedor) {
		PadronCABA padron = new PadronCABA();
		AlicuotaPadron alicuotaPadron = null;
		try{
			alicuotaPadron = padron.buscarAlicuota(fecha, proveedor.getNumeroDocumento(), TipoAlicuotaPadron.Retencion);
		}
		catch(Exception e){
			String error = e.getMessage();
			if (Is.emptyString(error)){
				error = e.toString();
			}
			throw new ValidationException("Error al buscar alicuota de retención CABA: " + error);
		}
		return alicuotaPadron;
	}

}
