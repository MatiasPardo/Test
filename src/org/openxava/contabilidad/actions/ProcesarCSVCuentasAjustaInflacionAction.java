package org.openxava.contabilidad.actions;

import java.io.IOException;

import org.openxava.base.actions.ProcesarCSVGenericoAction;
import org.openxava.contabilidad.model.CuentaContable;
import org.openxava.contabilidad.model.TipoCuentaInflacion;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;

import com.csvreader.CsvReader;

public class ProcesarCSVCuentasAjustaInflacionAction extends ProcesarCSVGenericoAction{

	@Override
	protected void preProcesarCSV() throws Exception {
	}

	@Override
	protected void posProcesarCSV() throws Exception {
		this.getPreviousView().refreshCollections();
	}

	@Override
	protected void procesarLineaCSV(CsvReader csvReader) throws IOException {
		String codigo = csvReader.get(0);
		if (!Is.emptyString(codigo)){
			CuentaContable cuenta = (CuentaContable)CuentaContable.buscarPorCodigo(codigo, CuentaContable.class.getSimpleName());
			if (cuenta != null){
				String tipoInflacion = csvReader.get(2);
				if (!Is.emptyString(tipoInflacion)){
					if (Is.equalAsStringIgnoreCase(tipoInflacion, "no Ajustable")){
						cuenta.setInflacion(TipoCuentaInflacion.noAjustable);
					}
					else if (Is.equalAsStringIgnoreCase(tipoInflacion, "Ajustable")){
						cuenta.setInflacion(TipoCuentaInflacion.ajustable);
					}
					else if (Is.equalAsStringIgnoreCase(tipoInflacion, "Cuenta Inflacion")){
						cuenta.setInflacion(TipoCuentaInflacion.cuentaInflacion);
					}
					else{
						throw new ValidationException("Tipo cuenta inflación incorrecta");
					}
				}
				else{
					throw new ValidationException("Tipo cuenta inflación no asignada");
				}
			}
			else{
				throw new ValidationException("No existe la cuenta " + codigo);
			}
		}
		else{
			throw new ValidationException("Código no asignado");
		}	
	}

}
