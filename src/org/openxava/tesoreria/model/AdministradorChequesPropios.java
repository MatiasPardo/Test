package org.openxava.tesoreria.model;

import org.openxava.validators.ValidationException;

public class AdministradorChequesPropios {
	
	private IChequera chequera;
	
	public IChequera getChequera(){
		if (this.chequera == null){
			throw new ValidationException("No esta asignada la chequera en el Administrador de cheques propios");
		}
		return this.chequera;
	}
	
	public AdministradorChequesPropios(IChequera chequera){
		this.chequera = chequera;
	}
	
	public String numerarCheque(TipoValorConfiguracion tipoValor){
		Long numero = this.getChequera().getProximoNumeroChequera();
		Long ultimoNumero = this.getChequera().getUltimoNumeroChequera();
		if ((numero != null) && (ultimoNumero != null)){
			if (numero.compareTo(ultimoNumero) <= 0){
				this.getChequera().setProximoNumeroChequera(this.getChequera().getProximoNumeroChequera() + 1);
				return formatearNumeroCheque(numero);
			}
			else{
				throw new ValidationException("Ya se utilizó el último número de la chequera en " + this.getChequera().toString());
			}
		}
		else{
			throw new ValidationException("No esta configurado la numeración para la chequera en " + this.getChequera().toString());
		}
	}
	
	private String formatearNumeroCheque(Long numero) {		 
		return numero.toString();
	}
	
	public void notificarUsoCheque(String numeroCheque){
		try{
			Long nro = Long.parseLong(numeroCheque);
			if (this.getChequera().getProximoNumeroChequera() < nro){
				this.getChequera().setProximoNumeroChequera(nro + 1);
			}
		}
		catch(Exception e){			
		}
		
	}
	
}
