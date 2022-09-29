package org.openxava.compras.actions;

import java.io.*;

import org.openxava.base.actions.*;
import org.openxava.base.model.*;
import org.openxava.clasificadores.model.Clasificador;
import org.openxava.compras.model.*;
import org.openxava.contabilidad.model.*;
import org.openxava.impuestos.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.calculators.*;
import org.openxava.negocio.model.*;
import org.openxava.tesoreria.model.Banco;
import org.openxava.tesoreria.model.SucursalBanco;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.TipoCuentaBancaria;

import com.csvreader.*;

public class ProcesarCSVProveedorAction extends ProcesarCSVGenericoAction{

	@Override
	protected void preProcesarCSV() throws Exception {		
	}

	@Override
	protected void procesarLineaCSV(CsvReader csvReader) throws IOException {
		String codigo = csvReader.get(0);
		if (!Is.emptyString(codigo)){
			Proveedor proveedor = (Proveedor)ObjetoEstatico.buscarPorCodigo(codigo, Proveedor.class.getSimpleName());
			if (proveedor == null){
				proveedor = crearProveedor(codigo);
			}
			
			proveedor.setNombre(csvReader.get(1));
			proveedor.setTipoDocumento(TipoDocumento.valueOf(csvReader.get(2)));
			proveedor.setNumeroDocumento(csvReader.get(3));
			proveedor.setContacto(csvReader.get(4));
			proveedor.setTelefono(csvReader.get(5));
			proveedor.setMail1(csvReader.get(6));
			proveedor.setMail2(csvReader.get(7));
			proveedor.setWeb(csvReader.get(8));			
			proveedor.setPosicionIva(PosicionAnteImpuesto.buscarPorCodigo(csvReader.get(9)));
			proveedor.setCuentaContableCompras((CuentaContable)CuentaContable.buscarPorCodigo(csvReader.get(10), CuentaContable.class.getSimpleName()));
			Domicilio domicilio = null;
			if (proveedor.esNuevo()){
				domicilio = new Domicilio();
				proveedor.setDomicilio(domicilio);
				domicilio.setCiudad(Ciudad.buscarPorCodigoPostal(csvReader.get(11)));
			}
			else{
				domicilio = proveedor.getDomicilio();
				if (!Is.equalAsString(domicilio.getCiudad().getCodigoPostal(), csvReader.get(11))){
					domicilio.setCiudad(Ciudad.buscarPorCodigoPostal(csvReader.get(11)));
				}				 
			}
			if (domicilio.getCiudad() == null){
				throw new ValidationException("No se encontró ciudad con el código postal " + csvReader.get(11));
			}
			domicilio.setDireccion(csvReader.get(12));			
			domicilio.setObservaciones(csvReader.get(13));			
			proveedor.setObservaciones(csvReader.get(14));
			
			if (csvReader.getColumnCount() > 14){
				if (!Is.emptyString(csvReader.get(15))){
					proveedor.setBanco((Banco)Banco.buscarPorCodigo(csvReader.get(15), Banco.class.getSimpleName()));
				}
			}
			if (csvReader.getColumnCount() > 15){
				if (!Is.emptyString(csvReader.get(16))){
					proveedor.setCuentaBancaria(csvReader.get(16));
				}
			}
			if (csvReader.getColumnCount() > 16){
				if (!Is.emptyString(csvReader.get(17))){
					proveedor.setClaveBancariaUniforme(csvReader.get(17));
				}
			}
			if (csvReader.getColumnCount() > 17){
				if (!Is.emptyString(csvReader.get(18))){
					proveedor.setProveedorClasificador1(Clasificador.buscar(csvReader.get(18), Proveedor.class.getSimpleName(), 1));
				}
			}
			if (csvReader.getColumnCount() > 18){
				if (!Is.emptyString(csvReader.get(19))){
					proveedor.setProveedorClasificador2(Clasificador.buscar(csvReader.get(19), Proveedor.class.getSimpleName(), 2));
				}
			}
			if (csvReader.getColumnCount() > 19){
				if (!Is.emptyString(csvReader.get(20))){
					proveedor.setProveedorClasificador3(Clasificador.buscar(csvReader.get(20), Proveedor.class.getSimpleName(), 3));
				}
			}
			if (csvReader.getColumnCount() > 20){ 
				if (!Is.emptyString(csvReader.get(21))){
					proveedor.setNumeroCuenta(new Integer(csvReader.get(21)));
				}
			}
			if (csvReader.getColumnCount() > 21){  
				if (!Is.emptyString(csvReader.get(22))){
					proveedor.setSucursalBancaria((SucursalBanco)SucursalBanco.buscarPorCodigo(csvReader.get(22), SucursalBanco.class.getSimpleName()));
				}
			}
			if (csvReader.getColumnCount() > 22){
				if (!Is.emptyString(csvReader.get(23))){
					try{
						proveedor.setTipoCuenta((TipoCuentaBancaria)this.convertirStrEnum(csvReader.get(23), TipoCuentaBancaria.values(), "Tipo cuenta bancaria inválida: "));
					}
					catch(Exception e){
						throw new ValidationException("Tipo cuenta inválida: " + csvReader.get(23));
					}
				}
			}
			
			if (proveedor.esNuevo()){
				XPersistence.getManager().persist(proveedor);
			}
			if (domicilio.esNuevo()){
				XPersistence.getManager().persist(domicilio);
			}
		}
		else{
			throw new ValidationException("Código no asignado");
		}
		
	}
	
	private Proveedor crearProveedor(String codigo){
		Proveedor proveedor = new Proveedor();
		proveedor.setCodigo(codigo);
		ObjetoPrincipalCalculator calculator = new ObjetoPrincipalCalculator();
		calculator.setEntidad(Moneda.class.getSimpleName());
		try{
			Moneda moneda = (Moneda)calculator.calculate();
			proveedor.setMoneda(moneda);
		}
		catch(Exception e){	
		}
		return proveedor;
	}

	@Override
	protected void posProcesarCSV() throws Exception {		
	}	
}
