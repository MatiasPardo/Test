package org.openxava.ventas.actions;

import java.io.IOException;
import java.math.BigDecimal;

import org.openxava.afip.model.TipoPersonaAfip;
import org.openxava.base.actions.ProcesarCSVGenericoAction;
import org.openxava.base.model.ObjetoEstatico;
import org.openxava.clasificadores.model.Clasificador;
import org.openxava.contabilidad.calculators.CuentaContableVentasDefaultCalculator;
import org.openxava.contabilidad.model.CuentaContable;
import org.openxava.fisco.calculators.TipoFacturacionCalculator;
import org.openxava.fisco.model.TipoFacturacion;
import org.openxava.impuestos.model.PosicionAnteImpuesto;
import org.openxava.jpa.XPersistence;
import org.openxava.negocio.calculators.ObjetoPrincipalCalculator;
import org.openxava.negocio.model.Ciudad;
import org.openxava.negocio.model.Domicilio;

import org.openxava.negocio.model.TipoDocumento;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.calculators.LimiteCreditoDefaultCalcultor;
import org.openxava.ventas.model.Cliente;
import org.openxava.ventas.model.FrecuenciaEntrega;
import org.openxava.ventas.model.ListaPrecio;
import org.openxava.ventas.model.LugarEntregaMercaderia;
import org.openxava.ventas.model.MedioTransporte;
import org.openxava.ventas.model.Vendedor;
import org.openxava.ventas.model.Zona;

import com.csvreader.CsvReader;

public class ProcesarCSVClienteAction extends ProcesarCSVGenericoAction{

	@Override
	protected void preProcesarCSV() throws Exception {
	}

	@Override
	protected void posProcesarCSV() throws Exception {
				
	}

	@Override
	protected void procesarLineaCSV(CsvReader csvReader) throws IOException {
		String codigo = csvReader.get(0);
		if (!Is.emptyString(codigo)){
			Cliente cliente = (Cliente)ObjetoEstatico.buscarPorCodigo(codigo, Cliente.class.getSimpleName());
			if (cliente == null){
				cliente = crearCliente(codigo);
			}			
			cliente.setNombre(csvReader.get(1));
			cliente.setNombreFantasia(csvReader.get(2));
			if (Is.emptyString(cliente.getNombreFantasia())){
				cliente.setNombreFantasia(cliente.getNombre());
			}
			if (Is.emptyString(cliente.getNombre())){
				throw new ValidationException("Nombre no asignado");
			}
			cliente.setTipoDocumento(TipoDocumento.valueOf(csvReader.get(3)));
			cliente.setNumeroDocumento(csvReader.get(4));
			cliente.setPosicionIva(PosicionAnteImpuesto.buscarPorCodigo(csvReader.get(5)));
			cliente.setTipo(TipoPersonaAfip.valueOf(csvReader.get(6)));
			cliente.setContacto(csvReader.get(7));
			cliente.setTelefono(csvReader.get(8));
			cliente.setMail1(csvReader.get(9));
			cliente.setMail2(csvReader.get(10));
			cliente.setWeb(csvReader.get(11));
			cliente.setVendedor((Vendedor)Vendedor.buscarPorCodigo(csvReader.get(12), Vendedor.class.getSimpleName()));
						
			Domicilio domicilio = null;
			LugarEntregaMercaderia domicilioEntrega = null;
			if (cliente.esNuevo()){
				domicilio = new Domicilio();
				domicilio.setCiudad(Ciudad.buscarPorCodigoPostal(csvReader.get(13)));
				
				domicilioEntrega = new LugarEntregaMercaderia();
				domicilioEntrega.setCodigo(cliente.getCodigo());
				domicilioEntrega.setNombre(cliente.getNombre());
				domicilioEntrega.setPrincipal(Boolean.TRUE);
				domicilioEntrega.setDomicilio(domicilio);
				
				domicilioEntrega.setCliente(cliente);
				cliente.setDomicilio(domicilio);
				cliente.setDomicilioLegal(domicilioEntrega);
			}
			else{
				domicilio = cliente.getDomicilio();
				domicilioEntrega = cliente.getDomicilioLegal();
				if (!Is.equalAsString(domicilio.getCiudad().getCodigoPostal(), csvReader.get(13))){
					domicilio.setCiudad(Ciudad.buscarPorCodigoPostal(csvReader.get(13)));
				}				 
			}
			if (domicilio.getCiudad() == null){
				throw new ValidationException("No se encontró ciudad con el código postal " + csvReader.get(13));
			}
			domicilio.setDireccion(csvReader.get(14));			
			domicilio.setObservaciones(csvReader.get(15));			
			cliente.setObservaciones(csvReader.get(16));
			
			if (!Is.emptyString(csvReader.get(17))){
				cliente.setCuentaContableVentas((CuentaContable)CuentaContable.buscarPorCodigo(csvReader.get(17), CuentaContable.class.getSimpleName()));
			}
			if (cliente.getCuentaContableVentas() == null){
				throw new ValidationException("Falta asignar la cuenta contable");
			}
			
			cliente.setClienteClasificador1(Clasificador.buscar(csvReader.get(18), Cliente.class.getSimpleName(), 1));
			cliente.setClienteClasificador2(Clasificador.buscar(csvReader.get(19), Cliente.class.getSimpleName(), 2));
			cliente.setClienteClasificador3(Clasificador.buscar(csvReader.get(20), Cliente.class.getSimpleName(), 3));
			
			domicilioEntrega.setHorario(csvReader.get(21));
			domicilioEntrega.setZona((Zona)Zona.buscarPorCodigo(csvReader.get(22), Zona.class.getSimpleName()));
			domicilioEntrega.setMedioTransporte((MedioTransporte)MedioTransporte.buscarPorCodigo(csvReader.get(23), MedioTransporte.class.getSimpleName()));
			domicilioEntrega.setFrecuencia((FrecuenciaEntrega)FrecuenciaEntrega.buscarPorCodigo(csvReader.get(24), FrecuenciaEntrega.class.getSimpleName()));
			if (!Is.emptyString(csvReader.get(25)) && !Is.emptyString(csvReader.get(26))){
				domicilio.setLatitud(this.convertirStrDouble(csvReader.get(25)));
				domicilio.setLongitud(this.convertirStrDouble(csvReader.get(26)));
			}
			
			if (cliente.esNuevo()){
				XPersistence.getManager().persist(cliente);
			}
			if (domicilio.esNuevo()){
				XPersistence.getManager().persist(domicilio);
			}
			if (domicilioEntrega.esNuevo()){
				XPersistence.getManager().persist(domicilioEntrega);
			}
		}
		else{
			throw new ValidationException("Código no asignado");
		}			
	}
	
	private Cliente crearCliente(String codigo){
		Cliente cliente = new Cliente();
		cliente.setCodigo(codigo);
		
		try{
			ObjetoPrincipalCalculator calculator = new ObjetoPrincipalCalculator();
			calculator.setEntidad(ListaPrecio.class.getSimpleName());
			cliente.setListaPrecio((ListaPrecio)calculator.calculate());
			
			LimiteCreditoDefaultCalcultor limite = new LimiteCreditoDefaultCalcultor();
			cliente.setLimiteCredito((BigDecimal)limite.calculate());
			
			CuentaContableVentasDefaultCalculator cuenta = new CuentaContableVentasDefaultCalculator();
			cliente.setCuentaContableVentas((CuentaContable)cuenta.calculate());
			
			TipoFacturacionCalculator tipoFacturacion = new TipoFacturacionCalculator();
			tipoFacturacion.setEntidad(TipoFacturacion.class.getSimpleName());
			cliente.setRegimenFacturacion((TipoFacturacion)tipoFacturacion.calculate());
		}
		catch(Exception e){
			
		}
				
		return cliente;
	}
}
