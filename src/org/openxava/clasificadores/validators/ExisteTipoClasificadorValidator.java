package org.openxava.clasificadores.validators;

import org.openxava.util.*;
import org.openxava.validators.*;

@SuppressWarnings("serial")
public class ExisteTipoClasificadorValidator implements IValidator{
	
	private String modelo;
		
	private Integer indice;
	
	public String getModelo() {
		return modelo;
	}

	public void setModelo(String modelo) {
		this.modelo = modelo;
	}

	public Integer getIndice() {
		return indice;
	}

	public void setIndice(Integer indice) {
		this.indice = indice;
	}



	@Override
	public void validate(Messages errors) throws Exception {
		if (!Is.emptyString(this.getModelo())){
			boolean existsClassname = false;
			Class<?> miClase = null;
			final Package[] packages = Package.getPackages();
			for (final Package p : packages) {
				final String pack = p.getName();
			    final String tentative = pack + "." + this.getModelo();
			    try {
			    	miClase = Class.forName(tentative);
			        existsClassname = true;
			        break;
			    }catch (final ClassNotFoundException e) {
			    	continue;
			    }
			}
			
			if (!existsClassname){
				errors.add("No existe " + this.getModelo());
			}
			else{
				String nombreAtributo = this.getModelo().toLowerCase() + "Clasificador";				
				if (this.getIndice() != null){
					nombreAtributo = nombreAtributo.concat(this.getIndice().toString());
				}
				
				try{
					miClase.getDeclaredField(nombreAtributo);
				}
				catch(NoSuchFieldException e){
					errors.add("No existe el campo: " + nombreAtributo + " en la entidad " + this.getModelo());
				}
				catch(Exception e){
					errors.add("No se pudo obtener el campo: " + nombreAtributo + " en la entidad " + this.getModelo());
				}
			}
		}
	}
}
