package org.openxava.tesoreria.actions;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openxava.actions.IChainAction;
import org.openxava.actions.TabBaseAction;
import org.openxava.base.actions.DescargarArchivosAction;
import org.openxava.base.model.Estado;
import org.openxava.base.model.EstadoEntidad;
import org.openxava.jpa.XPersistence;
import org.openxava.model.MapFacade;
import org.openxava.negocio.model.Moneda;
import org.openxava.tesoreria.model.*;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.TipoCuentaBancaria;

import com.allin.interfacesafip.model.AfipMonedas;
import com.csvreader.CsvWriter;

public class IntefazPagosBancoAction extends TabBaseAction implements IChainAction{
	
	private List<String> files = null;
	
	private CuentaBancaria miBanco;
	
	private int contador = 0;
	
	private Collection<PagoProveedores> pagoProveedoresList;
	
	private List<PagoProveedores> filesProcesadosOk = new LinkedList<PagoProveedores>();

	private List<PagoProveedores> pagosConErrores = new LinkedList<PagoProveedores>();


	@Override
	public void execute() throws Exception {
		pagoProveedoresList = new LinkedList<PagoProveedores>();
			
		for (Map<?, ?> key: getTab().getSelectedKeys()) { 
			PagoProveedores pagoProveedores= (PagoProveedores)MapFacade.findEntity("PagoProveedores", key); 
			if(pagoProveedores.getEstado().equals(Estado.Confirmada)) {
				pagoProveedoresList.add(pagoProveedores);
			}else {
				this.addError("falta confirmar la transaccion");
				this.pagosConErrores.add(pagoProveedores);
				getTab().deselect(key);
			}
		}
		if (pagoProveedoresList.isEmpty()){
			this.addError("No se generó ningún comprobantes");
		}
		this.files = new LinkedList<String>();
		this.generarArchivos(files);
		if(pagosConErrores.isEmpty()){
			addMessage("Operacion Realizada");
		}else addError("algunos pagos continenen errores, estos fueron deseleccionados.");
		this.closeDialog();		
	}

	private void generarArchivos(List<String> fileIDs) throws IOException {
		if(Is.emptyString(getView().getValueString("cuentaOrigen.id"))){
			throw new ValidationException("cuenta bancaria no asignada"); 
		}
		this.miBanco = (CuentaBancaria)XPersistence.getManager().find(CuentaBancaria.class, getView().getValueString("cuentaOrigen.id")); 
		if(miBanco.getSucursalBancaria() == null || miBanco.getNumeroCuenta().isEmpty() || miBanco.getSucursalBancaria().getNumeroSucursal() == null){
			 throw new ValidationException("sucursal no asignada en la cuenta bancaria seleccionado, por favor asigne una sucursal y un numero de cuenta"); 
		}
		EstadoEntidad enviado = EstadoEntidad.buscarPorCodigo(PagoProveedores.ENVIADO, PagoProveedores.class.getSimpleName());
		EstadoEntidad pendienteEnvio = EstadoEntidad.buscarPorCodigo(PagoProveedores.PENDIENTE_ENVIO, PagoProveedores.class.getSimpleName());
		
		StringWriter datos = new StringWriter();
		CsvWriter writer = new CsvWriter(datos, ';');
		BigDecimal importe= new BigDecimal(0);
		
		try{
			for(PagoProveedores pago: pagoProveedoresList){
				if(Is.equal(pago.getSubestado(), pendienteEnvio)){
					for(ItemPagoProveedores item: pago.getValores()){
						if(item.getOrigen().equals(miBanco) && this.esTransferencia(item)){
							importe = importe.add(item.getImporteOriginal());
							contador ++;
						}
					}					
				}	
			}
			
			this.cargarDatosPagosHeader(writer, importe);			
			Map<String, String> deseleccionar = new HashMap<String, String>();
			for(PagoProveedores pago: pagoProveedoresList){		
				//cargar body 
				if(Is.equal(pago.getSubestado(), pendienteEnvio)){
					if(!this.alMenosUnoConMismoOrigen(pago,writer)){
						this.deseleccionarYAgregarError(pago, deseleccionar, pago.toString() +  " contiene errores");
					}
					else this.filesProcesadosOk.add(pago);
				}
				else if(!pagosConErrores.contains(pago)){					
					this.deseleccionarYAgregarError(pago, deseleccionar, pago.toString() + " no esta pendiente de envio al banco");
				}
			}
			
			this.cargarDatosPagosTrailer(contador, writer);
			if(pagosConErrores.isEmpty()){
				fileIDs.add(DescargarArchivosAction.grabarArchivo("Santander Rio", datos));
				for(PagoProveedores pago: pagoProveedoresList){
					pago.setSubestado(enviado);
				}
			}
		}
		finally{
			writer.flush();
			writer.close();
		}
	}
	
	private boolean alMenosUnoConMismoOrigen(PagoProveedores pago, CsvWriter writer) throws IOException {
		List<ItemPagoProveedores> itemProcesados = new LinkedList<ItemPagoProveedores>();
		for(ItemPagoProveedores item: pago.getValores()){
			if(item.getOrigen().equals(miBanco) && this.esTransferencia(item)){
				try{
					this.cargarDatosPagosBody(item, writer, pago);
					itemProcesados.add(item);
				}catch(ValidationException v){
					addError(v.getMessage());
					pagosConErrores.add(pago);
				}
			}	
		}
		if(itemProcesados.size() > 0){
			return true;
		}else {
			this.pagosConErrores.add(pago);
			return false;	
		}
	}
	
	public void deseleccionarYAgregarError(PagoProveedores pago, Map<String, String> deseleccionar, String error){
		deseleccionar.put("id", pago.getId());
		getTab().deselect(deseleccionar);
		pagosConErrores.add(pago);
		addError(error);		
	}

	private void cargarDatosPagosHeader(CsvWriter writer,BigDecimal importe) throws IOException {
		writer.write("H");
		
		writer.write("0072");
		
		this.validarNumeroSucursal(miBanco.getSucursalBancaria().getNumeroSucursal(),miBanco.getBanco());
		writer.write(String.format("%03d", miBanco.getSucursalBancaria().getNumeroSucursal()));
		
		writer.write(this.tipoCuenta(miBanco.getEfectivo().getMoneda(),miBanco.getTipo())); 
		
		if (miBanco.getTipo() == null){
			throw new ValidationException("La cuenta bancaria origen no tiene asignado el tipo");
		}
		if(miBanco.getNumeroCuenta().length() > 7){
			throw new ValidationException("El numero de cuenta origen no puede tener mas de 7 caracteres");
		}
		writer.write(String.valueOf(miBanco.getNumeroCuenta())); //numero cuenta 
		
		String importeConComaOPunto = importe.toString();
		writer.write(this.armarImporte(importeConComaOPunto));
		
		writer.write("0000001");	//Numero de envio
		
		writer.endRecord();
	}


	private void cargarDatosPagosBody(ItemPagoProveedores item, CsvWriter writer,PagoProveedores pago) throws IOException {
		if(this.esTransferencia(item) ){
			writer.write("I");
			if(pago.getProveedor().getBanco() != null && miBanco.getBanco().equals(pago.getProveedor().getBanco())){
				this.bodyOtrasCuentas(writer, pago, item);
			}else this.bodyOtrosBancos(writer, pago, item);
			
			
			if(getErrors().isEmpty()){
				writer.endRecord();
			}else{
				throw new ValidationException(" ");
			}
		}
	}
	
	private void bodyOtrasCuentas(CsvWriter writer, PagoProveedores pago, ItemPagoProveedores item) throws IOException {
		writer.write("2"); //Código Tipo de Transferencia - 2 otros cuentas
		if(!Is.empty(pago.getProveedor().getSucursalBancaria())){
			this.validarNumeroSucursal(pago.getProveedor().getSucursalBancaria().getNumeroSucursal(),pago.getProveedor().getBanco());
			writer.write( String.format("%03d",pago.getProveedor().getSucursalBancaria().getNumeroSucursal()) ); //numero sucursal destino 					
		}else addError("El proveedor " + pago.getProveedor().getCodigo() + " no tiene sucursal bancaria");
		
		if(!(Is.empty(pago.getProveedor().getTipoCuenta()) && Is.empty(pago.getMoneda()))){
			writer.write(this.tipoCuenta(pago.getMoneda(),pago.getProveedor().getTipoCuenta())); // tipo de cuenta destino
		}else addError("El proveedor " + pago.getProveedor().getCodigo() + " no tiene tipo de cuenta");
		
		if(!Is.empty(pago.getProveedor().getNumeroCuenta())){
			String numeroCuenta = String.valueOf(pago.getProveedor().getNumeroCuenta());
			if(numeroCuenta.length() < 7){
				int cerosRestantes = 7 - numeroCuenta.length();
				writer.write(this.cargarCeros(cerosRestantes,numeroCuenta)); // Numero de cuenta
			}else if(numeroCuenta.length() == 7){
				writer.write(numeroCuenta);			
			}else{
				addError("El numero de cuenta del proveedor: " +
												pago.getProveedor().getCodigo()+", debe tener una longitud igual a 7 ");
			}
		}else addError("El proveedor " + pago.getProveedor().getCodigo() + " no tiene numero de cuenta");
		
		writer.write(this.armarImporte(item.getImporte().toString()));
		if(pago.getProveedor().getCuentaBancaria() != null){
			writer.write(this.descripcionDe20Caracteres(pago.getProveedor().getCuentaBancaria()));	//Descripción de la cuenta destino//proveedor-banco-cuentabancaria
		}else{
			addError("El proveedor "+pago.getProveedor().getCodigo()+" no tiene cuenta bancaria");
		}		writer.write("FAC"); //Código Concepto Transferencia //siempre fac
		writer.write(""); // Información adicional		
	}

	private void bodyOtrosBancos(CsvWriter writer,PagoProveedores pago,ItemPagoProveedores item) throws IOException {
		writer.write("3"); //Código Tipo de Transferencia - 3 otros bancos
		writer.write(this.armarImporte(item.getImporte().toString()));
		if(pago.getProveedor().getCuentaBancaria() != null){
			writer.write(this.descripcionDe20Caracteres(pago.getProveedor().getCuentaBancaria()));	//Descripción de la cuenta destino//proveedor-banco-cuentabancaria
		}else{
			writer.write(this.descripcionDe20Caracteres(""));	//Descripción de la cuenta destino//proveedor-banco-cuentabancaria
		}
		String cbu = pago.getProveedor().getClaveBancariaUniforme();
		if(cbu != null && !cbu.isEmpty() && cbu.length() == 22){
			writer.write(cbu); //proveedor-banco-cbu
		}else{
			addError("El proveedor "+pago.getProveedor().getCodigo()+" no tiene numero de cbu valido");
		}
		writer.write("FAC"); //Código Concepto Transferencia //siempre fac
		if(item.getDetalle() != null && !item.getDetalle().isEmpty()){
			writer.write(this.descripcionDe20Caracteres(item.getDetalle()));
		}else{
			writer.write("");
		}
		writer.write(this.normalizarCuit(pago.getProveedor().getNumeroDocumento()));//CUIT proveedores
		writer.write("4");//Código Tipo Cuenta Destino //otra cuenta de terceros		
	}

	private String cargarCeros(int cantidadDeCeros, String concatenarCeros){
		StringBuilder ceros = new StringBuilder();
		for(int i =1;i<=cantidadDeCeros;i++){
			ceros.append("0");
		}
		return ceros.append(concatenarCeros).toString();	
	}
	
	private String armarImporte(String importeConComaOPunto) {
		String importesinDecimal = new String();
		if(importeConComaOPunto.contains(".")){ 
			String[] importeSeparado = importeConComaOPunto.split("[.]");
			importesinDecimal= importeSeparado[0]+importeSeparado[1];
		}else if(importeConComaOPunto.contains(",")){
			String[] importeSeparado = importeConComaOPunto.split("[,]");
			importesinDecimal= importeSeparado[0]+importeSeparado[1];
		}else {
			importesinDecimal = importeConComaOPunto+"00";
		}
		if(importesinDecimal.length()>14){
			addError("el importe supero la longitud permitida, maximo 12 caracteres", importesinDecimal);
		}else if (importesinDecimal.length()<=14){
			int cerosRestantes = 14 - importesinDecimal.length();			
			return this.cargarCeros(cerosRestantes, importesinDecimal); //Importe a Transferir - Sin puntos ni comas, multiplicado por 100
		}
		return importesinDecimal;
	}
	
	private String descripcionDe20Caracteres(String cuentaBancaria) {
		String cuentaConDescripcionCorrecta = new String();
		if(cuentaBancaria.length() < 20){
			StringBuilder completarConX =new StringBuilder();
			int cantidadFaltante = 20 - cuentaBancaria.length();
			for(int i=1;i<=cantidadFaltante;i++){
				completarConX.append("x");
			}
			cuentaConDescripcionCorrecta = completarConX.append(cuentaBancaria.replace(" ", "x")).toString();
		}
		if(cuentaBancaria.length() > 20){
			cuentaConDescripcionCorrecta = cuentaBancaria.substring(20).replace(" ", "x");
		}	
		if((cuentaBancaria.length() == 20)){
			cuentaConDescripcionCorrecta = cuentaBancaria;
		}
		if(cuentaConDescripcionCorrecta.isEmpty()){
			throw new ValidationException("Error con la descripcion");
		}
		return cuentaConDescripcionCorrecta;
	}

	private void validarNumeroSucursal(Integer numeroSucursal,Banco banco) {
		if(numeroSucursal == null){
			throw new ValidationException("El numero de la sucursal no esta asignado "+banco.getCodigo());
		}
		if ( numeroSucursal.toString().length() > 3){
			throw new ValidationException("El numero de la sucursal esta mal asignado, debe tener cómo máximo 3 cifras: " + numeroSucursal.toString());
		}		
	}

	private void cargarDatosPagosTrailer(int contador, CsvWriter writer) throws IOException {
		writer.write("T"); 
		writer.write(String.valueOf(contador)); // Cantidad total de transferencias
		writer.endRecord();
	}

	public boolean esTransferencia(ItemPagoProveedores item){
		return (item.getTipoValor().getComportamiento().equals(TipoValor.TransferenciaBancaria)
				|| item.getTipoValor().getComportamiento().equals(TipoValor.Efectivo));
	}
	
	private String tipoCuenta(Moneda moneda, TipoCuentaBancaria tipoCuenta){
		//Tipo Cuenta Débito “00” - CC en Pesos;//“01” - CA en Pesos;//“03” - CC en Dólares;//“04” - CA en Dólares
		if (Is.equalAsString(moneda.getCodigoAfip(), AfipMonedas.PesosArgentinos.getCodigoAfip())){ 
			if(tipoCuenta.equals(TipoCuentaBancaria.CajaAhorro)){
				return "01";															
			}
			else{
				return "00";
			}
		}
		else if (Is.equalAsString(moneda.getCodigoAfip(), AfipMonedas.DolaresEEUU.getCodigoAfip())){
			if(tipoCuenta.equals(TipoCuentaBancaria.CajaAhorro)){
				return "04";
			}
			else{
				return "03";
			}
		}
		else{
			throw new ValidationException("La moneda de la cuenta bancaria no es ni Pesos ni Dolares");
			
		}		
	}
	
	private String normalizarCuit(String numeroDocumento) {
		if(numeroDocumento.contains("-")){
			String[] cuitSeparado = numeroDocumento.split("-");
			if(cuitSeparado.length == 2){
				return cuitSeparado[0]+cuitSeparado[1];
			}else return cuitSeparado[0]+cuitSeparado[1]+cuitSeparado[2];
		}else return numeroDocumento;
	}
		
	@Override
	public String getNextAction() throws Exception {
		if (!this.files.isEmpty()){
			ArrayList<String> filesID = new ArrayList<String>();
			filesID.addAll(this.files);
			this.getRequest().setAttribute("facearg_filesID", filesID);    //
			return "Base.descargarArchivos";
		}
		else{
			return null;
		}
	}
}

