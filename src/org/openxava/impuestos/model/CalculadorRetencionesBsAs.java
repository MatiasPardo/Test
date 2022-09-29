package org.openxava.impuestos.model;

import java.util.*;

import org.openxava.base.model.*;
import org.openxava.compras.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

import com.allin.percepciones.model.*;

public class CalculadorRetencionesBsAs extends CalculadorRetencionIngresosBrutos{

	@Override
	protected EntidadImpuesto determinarEntidadImpuesto(Proveedor proveedor) {
		return proveedor.getRetencionARBA();
	}

	@Override
	protected AlicuotaPadron buscarAlicuotaPadron(Date fecha, Proveedor proveedor) {
		PadronARBA padron = new PadronARBA();
		padron.setUsuario(Esquema.getEsquemaApp().getUsuarioARBA());
		padron.setClave(Esquema.getEsquemaApp().getClaveARBA());
		AlicuotaPadron alicuota = null;
		try{
			alicuota = padron.buscarAlicuota(fecha, proveedor.getNumeroDocumento(), TipoAlicuotaPadron.Retencion);			
		}
		catch(Exception e){
			String error = e.getMessage();
			if (Is.emptyString(error)){
				error = e.toString();
			}
			throw new ValidationException("Error al buscar alicuota de retención BsAs: " + error);
		}
		return alicuota;
	}

}
