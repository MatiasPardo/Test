package org.openxava.base.model;



import java.lang.reflect.*;
import java.math.*;
import java.text.*;
import java.util.*;

import javax.persistence.*;

import org.hibernate.validator.constraints.*;
import org.openxava.annotations.*;
import org.openxava.base.actions.*;
import org.openxava.calculators.*;
import org.openxava.contabilidad.model.*;
import org.openxava.cuentacorriente.model.*;
import org.openxava.inventario.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.calculators.*;
import org.openxava.negocio.model.*;
import org.openxava.tesoreria.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

@MappedSuperclass

public abstract class Transaccion extends ObjetoNegocio implements ITransicionable{
	
	public static final String ACCIONGRABAR = "Transaccion.save";
	public static final String ACCIONCONFIRMAR = "Transaccion.confirmar";
	public static final String ACCIONANULAR = "Transaccion.anular";
	public static final String ACCIONCANCELAR = "Transaccion.cancelar";
	public static final String ACCIONCAMBIOESTADO = "Transaccion.CambiarEstado";

	public static final String VISTACAMBIOATRIBUTOSESPECIALES = "AccionesCambioAtributosTr";
	public static final String VISTACAMBIOFECHA = "CambioFecha";
	
	public static BigDecimal convertirMoneda(Transaccion origen, Transaccion destino, BigDecimal importe) {
		Moneda monedaDestino = destino.getMoneda();
		if (monedaDestino == null){
			throw new ValidationException("No se puede convertir moneda: " + destino.toString() + " no tiene asignada la moneda");
		}
		Moneda monedaOrigen = origen.getMoneda();
		if (monedaOrigen == null){
			throw new ValidationException("No se puede convertir moneda: " + origen.toString() + " no tiene asignada la moneda");
		}
		
		BigDecimal importeConvertido = BigDecimal.ZERO;
		if (monedaDestino.equals(monedaOrigen)){
			importeConvertido = importe;
		}
		else{
			if ((destino.getMoneda1() != null) && (destino.getMoneda1().equals(monedaOrigen))){
				// (origen Pesos )
				if (destino.getCotizacion().compareTo(BigDecimal.ZERO) != 0){
					importeConvertido = importe.divide(destino.getCotizacion(), importe.scale(), RoundingMode.HALF_EVEN);
				}
			}
			else if ((destino.getMoneda2() != null) && (destino.getMoneda2().equals(monedaOrigen))){
				// (origen Dolar) 
				if (destino.getMoneda1().equals(monedaDestino)){
					// (destino Pesos)
					importeConvertido = importe.multiply(destino.getCotizacion2());
				}
				else{
					// (destino moneda diferente Pesos y Dolar)
					if (destino.getCotizacion().compareTo(BigDecimal.ZERO) != 0){
						// convierto primero a Moneda1 y luego a MonedaDestino
						importeConvertido = importe.multiply(destino.getCotizacion2()).divide(destino.getCotizacion());
					}
				}
			}
			else{
				// origen otra moneda
				if (origen.getMoneda1().equals(monedaDestino)){
					// (destino Pesos)
					// Se toma la cotización de la transacción origen
					importeConvertido = importe.multiply(origen.getCotizacion());
				}
				else if (origen.getMoneda2().equals(monedaDestino)){
					// (destino Dolar)
					// convierto a moneda1 y luego a moneda2
					importeConvertido = importe.multiply(origen.getCotizacion()).divide(origen.getCotizacion2());
				}
				else{
					importeConvertido = destino.convertirImporteEnMonedaTr(monedaOrigen, importe);
				}
			}
		}
		
		return importeConvertido;
	}
			
	public BigDecimal buscarCotizacionTrConRespectoA(Moneda moneda){
		BigDecimal cotizacion = BigDecimal.ZERO;
		if (this.getMoneda() != null){
			cotizacion = new BigDecimal(1);
			if (!this.getMoneda().equals(moneda)){
				if(moneda.equals(this.getMoneda1())){
					cotizacion = this.getCotizacion();
				}
				else if(moneda.equals(this.getMoneda2())){
					if (this.getMoneda().equals(this.getMoneda1())){
						cotizacion = new BigDecimal(1).divide(this.getCotizacion2(), 16, RoundingMode.HALF_EVEN);
					}
					else{
						cotizacion = Cotizacion.buscarCotizacion(this.getMoneda(), moneda, this.getFecha());
					}
				}
				else{
					cotizacion = Cotizacion.buscarCotizacion(this.getMoneda(), moneda, this.getFecha());
				}
			}
		}
		return cotizacion;
	}
	
	public BigDecimal convertirImporteEnMonedaTr(Moneda moneda, BigDecimal importe){
		BigDecimal cotizacionTr = this.buscarCotizacionTrConRespectoA(moneda);
		if (cotizacionTr.compareTo(BigDecimal.ZERO) != 0){
			BigDecimal importeConvertido = importe;
			if (cotizacionTr.compareTo(new BigDecimal(1)) != 0){
				importeConvertido = importe.divide(cotizacionTr, 2, RoundingMode.HALF_EVEN);
			}
			return importeConvertido;
		}
		else{
			return BigDecimal.ZERO;
		}
	}
	
	public BigDecimal convertirImporteEnMoneda1Tr(Moneda moneda, BigDecimal importe){
		if (moneda.equals(this.getMoneda1())){
			return importe;
		}
		else{
			return importe.multiply(this.getCotizacion()).setScale(2, RoundingMode.HALF_EVEN);
		}
	}
	
	@Required @DefaultValueCalculator(CurrentDateCalculator.class)
	@Action(value="ModificarFechaTransaccion.Cambiar", forViews=Transaccion.VISTACAMBIOATRIBUTOSESPECIALES, notForViews="CambioFecha", alwaysEnabled=true)
	private Date fecha = UtilERP.trucarDateTime(new Date());
	
	@Column(length=20)  
	@SearchKey
	//@Action(value="Transaccion.cambiarNumero", alwaysEnabled=true)
	private String numero = new String("");
	
	@Hidden
	@DisplaySize(value=15)
	public String getDescripcion(){
		return this.descripcionTipoTransaccion();
	}
	
	@Hidden
	@ReadOnly
	private Long numeroInterno;
	
	@ReadOnly
	@DefaultValueCalculator(EstadoDefaultCalculator.class)
	private Estado estado = Estado.Borrador;

	@Transient
	private Estado cambiandoEstado = null;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify	
	private Empresa empresa;
		
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify	
	private Sucursal sucursal;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	private Moneda moneda;
	
	private BigDecimal cotizacion = BigDecimal.ZERO;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify @ReadOnly
	private Moneda moneda1;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify @ReadOnly
	private Moneda moneda2;
	
	@ReadOnly
	private BigDecimal cotizacion2;
	
	@Stereotype("MEMO")
	@Length(max=255)
	private String observaciones;
	
	@ReadOnly
	@Stereotype("DATETIME")
	private Date fechaConfirmacion;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly
	@DescriptionsList(descriptionProperties="nombre", forTabs="combo")
	private EstadoEntidad subestado;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly
	@DescriptionsList(descriptionProperties="nombre")
	private TransicionEstado ultimaTransicion;
	
	@Version
	@Hidden
	private Integer version;
	
	@Transient
	private ConfiguracionEntidad configuracionEntidad;
	
	public ConfiguracionEntidad configurador(){
		if (this.configuracionEntidad == null){
			this.configuracionEntidad = ConfiguracionEntidad.buscarConfigurador(this.getClass().getSimpleName());
			if (this.configuracionEntidad == null){
				throw new ValidationException("Debe definir un configurador para la entidad");
			}
		}
		return this.configuracionEntidad;
	}
	
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public TransicionEstado getUltimaTransicion() {
		return ultimaTransicion;
	}

	public void setUltimaTransicion(TransicionEstado ultimaTransicion) {
		this.ultimaTransicion = ultimaTransicion;
	}

	public EstadoEntidad getSubestado() {
		return subestado;
	}

	public void setSubestado(EstadoEntidad subestado) {
		this.subestado = subestado;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		if (fecha != null){
			this.fecha = UtilERP.trucarDateTime(fecha);
		}
		else{
			this.fecha = fecha;
		}
	}

	public String getNumero() {
		if (numero == null) return "";
		else return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public Long getNumeroInterno() {
		return numeroInterno;
	}

	public void setNumeroInterno(Long numeroInterno) {
		this.numeroInterno = numeroInterno;
	}

	public Estado getEstado() {
		return estado == null ? Estado.Borrador : this.estado;
	}

	public void setEstado(Estado estado) {
		if (estado != null){
			if (!this.esNuevo()){
				Estado estadoAnterior = this.estado;
				if (Is.equal(estadoAnterior, Estado.Confirmada) || Is.equal(estadoAnterior, Estado.Anulada)){
					if (estado.equals(Estado.Borrador) || estado.equals(Estado.Abierta) || estado.equals(Estado.Cancelada)){
						throw new ValidationException("Comprobante esta en estado " + estadoAnterior.toString() + " y se esta intentando modificar al estado " + estado.toString());
					}
				}
				// PROBAR PROCESANDO
				/*else if (Is.equal(estadoAnterior, Estado.Procesando)){
					if (estado.equals(Estado.Borrador)){
						throw new ValidationException("Comprobante esta en estado " + estadoAnterior.toString() + " y se esta intentando modificar al estado " + estado.toString());
					}
				}*/
			}			
		}
		
		this.estado = estado;
	}
	
	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
		if (empresa != null){
			this.setMoneda1(empresa.getMoneda1());
			this.setMoneda2(empresa.getMoneda2());
		}
	}

	protected Numerador numeradorSeleccionado() {
		return null;
	}

	protected Numerador buscarNumeradorDefault(){
		Numerador numeradorDefault = null;
		if (this.configurador() != null){
			numeradorDefault = this.configurador().getNumerador();
			if (this.getEmpresa() == null){
				throw new ValidationException("Falta asignar empresa");
			}
			if (numeradorDefault != null){
				if (!numeradorDefault.getEmpresa().equals(this.getEmpresa())){
					// no coincide la empresa, se pone en null para buscar el primero que encuentre.
					numeradorDefault = null;
				}
			}
			
			if (numeradorDefault == null){
				for(Numerador numerador: this.configurador().getNumeradores()){
					if (numerador.getEmpresa().equals(this.getEmpresa())){
						numeradorDefault = numerador;
						break;
					}
				}
			}
		}
		return numeradorDefault;
	}
	
	public Moneda getMoneda() {
		return moneda;
	}

	public void setMoneda(Moneda moneda) {
		this.moneda = moneda;
	}

	public BigDecimal getCotizacion() {
		return cotizacion == null ? BigDecimal.ZERO : this.cotizacion;
	}

	public void setCotizacion(BigDecimal cotizacion) {
		this.cotizacion = cotizacion;
	}

	public Date getFechaConfirmacion() {
		return fechaConfirmacion;
	}

	public void setFechaConfirmacion(Date fechaConfirmacion) {
		this.fechaConfirmacion = fechaConfirmacion;
	}

	public Moneda getMoneda1() {
		if (moneda1 == null){
			if (this.getEmpresa() != null){
				this.moneda1 = this.getEmpresa().getMoneda1();
			}
		}
		return moneda1;
	}

	public void setMoneda1(Moneda moneda1) {
		this.moneda1 = moneda1;
	}
	
	public Moneda getMoneda2() {
		if (this.moneda2 == null){
			if (this.getEmpresa() != null){
				this.moneda2 = this.getEmpresa().getMoneda2();
			}
		}
		return moneda2;
	}

	public void setMoneda2(Moneda moneda2) {
		this.moneda2 = moneda2;
	}

	public BigDecimal getCotizacion2() {
		return cotizacion2 == null ? BigDecimal.ZERO : this.cotizacion2;
	}

	public void setCotizacion2(BigDecimal cotizacion2) {
		this.cotizacion2 = cotizacion2;
	}

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}
	
	@Override
	public String toString(){
		String string = this.descripcionTipoTransaccion();
		if (Is.emptyString(string)) string = this.getClass().getSimpleName();
		
		if (!Is.emptyString(getNumero())){
			return string + " " + this.getNumero();
		}
		else{
			return string + " sin número";
		}
	}
	
	@Transient
	private boolean dispararRecalculo = true;
	
	public boolean ejecutarRecalculoTotales(){
		boolean ejecuta = false;
		if (this.dispararRecalculo){
			ejecuta = cumpleCondicionesRecalculoTotales() && !this.soloLectura();
			if (ejecuta){
			// los recalculos se deben disparar una sola vez, porque calculan todas los totalizadores.
				this.dispararRecalculo = false;
			}
		}
		return ejecuta;
	}
	
	protected boolean cumpleCondicionesRecalculoTotales(){
		return true;
	}
	
	public void recalcularTotales(){
	}
	
	@Override
	public void onPreDelete(){
		super.onPreDelete();
		if (!this.getEstado().equals(Estado.Borrador)){
			throw new ValidationException("No se puede eliminar la operación en estado " + this.getEstado().toString());
		}
	}
	
	protected void inicializar(){
		ConfiguracionEntidad configurador = this.configurador();
		if (configurador != null){
			this.setSubestado(configurador.getEstadoInicial());
			if (getEmpresa() == null){
				this.setEmpresa(this.buscarEmpresaDefault());
			}
			if (getMoneda() == null){
				this.setMoneda(this.buscarMonedaDefault());
			}
			this.asignarSucursal();			
		}
	}
	
	@Override
	public void onPreCreate(){
		super.onPreCreate();
		
		this.inicializar();
		
		this.grabarTransaccion();
	}
	
	
	
	protected Empresa buscarEmpresaDefault(){
		ConfiguracionEntidad configurador = this.configurador();
		if (configurador != null){
			return configurador.empresaDefault();
		}
		else{
			return null;
		}
	}
	
	@Hidden
	public String getEmpresaDefault(){
		Empresa empresaDefault = this.buscarEmpresaDefault();
		if (empresaDefault != null){
			return empresaDefault.getNombre();
		}
		else{
			return null;
		}
	}
	
	public Moneda buscarMonedaDefault(){
		ConfiguracionEntidad configurador = this.configurador();
		if (configurador != null){
			return configurador.getMoneda();
		}
		else{
			return null;
		}
	}
	
	@Override
	protected void onPrePersist(){
		super.onPrePersist();				
	}
	
	@Override
	protected void onPreUpdate(){
		super.onPreUpdate();
	}
	
	public void verificarEstadoParaModificarTr(){
		if (this.getEstado() != null){
			if (!Is.equal(this.cambiandoEstado, this.getEstado())){
				if (this.finalizada() || this.getEstado().equals(Estado.Procesando)){
					throw new ValidationException("No se puede modificar: Estado " + this.getEstado().toString());
				}				
			}
		}
	}
	
	public void grabarTransaccion(){
		
		this.verificarEstadoParaModificarTr();
		
		this.validacionesUsuario();
		
		Messages erroresFecha = new Messages();
		this.validarFechaComprobante(erroresFecha);
		
		Messages errores = new Messages();
		this.validacionesPreGrabarTransaccion(errores);
		
		if (!erroresFecha.isEmpty()){
			errores.add(erroresFecha);
		}
		if (!errores.isEmpty()){
			throw new ValidationException(errores);
		}		
		
		this.sincronizarCotizaciones();
		
		this.asignarSucursal();
		
		this.recalcularTotales();
		
		List<String> atributosMoneda = new LinkedList<String>();
		this.atributosConversionesMoneda(atributosMoneda);
		if (!atributosMoneda.isEmpty()){
			this.sincronizarMonedas(this, atributosMoneda);
		}
		
		this.validacionesUsuario();
	}
	
	protected void asignarSucursal(){
		if (this.getSucursal() == null){
			this.setSucursal(Sucursal.sucursalDefault());
		}
	}
	
	private void validacionesUsuario(){
		if (this.getEmpresa() != null){
			if (!this.getEmpresa().usuarioHabilitado(Users.getCurrent())){
				throw new ValidationException("Usuario no habilitado en la empresa " + this.getEmpresa().getNombre());
			}
		}
		else{
			throw new ValidationException("Empresa no asignada");
		}
		
		if (this.getSucursal() != null){
			if (!this.getSucursal().usuarioHabilitado()){
				throw new ValidationException("Usuario no habilitado para la sucursal " + this.getSucursal().toString());
			}
		}
	}
	
	protected void validarFechaComprobante(Messages errores){
		if (this.getFecha() != null){
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.DAY_OF_YEAR, 1);
			calendar.set(Calendar.HOUR_OF_DAY, calendar.getMinimum(Calendar.HOUR_OF_DAY));
			calendar.set(Calendar.MINUTE, calendar.getMinimum(Calendar.MINUTE));
			calendar.set(Calendar.SECOND, calendar.getMinimum(Calendar.SECOND));
			calendar.set(Calendar.MILLISECOND, calendar.getMinimum(Calendar.MILLISECOND));
			if (calendar.getTime().compareTo(this.getFecha()) <= 0){
				errores.add("La fecha no puede ser mayor a la fecha actual");
			}			
		}
	}
	
	protected void validacionesPreGrabarTransaccion(Messages errores){
		
	}
	
	protected boolean debeBuscarCotizacion(){
		boolean buscar = true;
		
		if ((this.configurador() != null) && (!this.configurador().getCotizacionSoloLectura())){
			if (this.getCotizacion().compareTo(BigDecimal.ZERO) > 0){
				buscar = false;
			}
		}
		
		return buscar;
	}
	
	protected void sincronizarCotizaciones() {
		if ((this.getMoneda() != null) && (this.getMoneda1() != null) && (this.getMoneda2() != null)){
			boolean buscaCotizacion = this.debeBuscarCotizacion();
			
			if (buscaCotizacion){
				BigDecimal cotizacion = Cotizacion.buscarCotizacion(this.getMoneda2(), this.getMoneda1(), this.getFecha());
				this.setCotizacion2(cotizacion);
				if (this.getMoneda().equals(this.getMoneda1())){
					this.setCotizacion(cotizacion);
				}
				else if (this.getMoneda().equals(this.getMoneda2())){
					this.setCotizacion(cotizacion);
				}
				else{
					this.setCotizacion(Cotizacion.buscarCotizacion(this.getMoneda(), this.getMoneda1(), this.getFecha()));
				}
			}
			else{
				if (this.getMoneda().equals(this.getMoneda1())){
					this.setCotizacion2(this.getCotizacion());
				}
				else if (getMoneda().equals(this.getMoneda2())){
					this.setCotizacion2(this.getCotizacion());
				}
				else{
					this.setCotizacion2(Cotizacion.buscarCotizacion(this.getMoneda2(), this.getMoneda1(), this.getFecha()));
				}
			}
		}
		else{
			throw new ValidationException("No están asignadas las monedas");
		}
	}
	
	@Override
	public void asignarNumeracion(String numeracion, Long numero){
		this.setNumero(numeracion);
		this.setNumeroInterno(numero);
	}
		
	public Boolean soloLectura(){
		if (this.estado.equals(Estado.Borrador) || this.estado.equals(Estado.Abierta)){
			if (this.getUltimaTransicion() != null){
				return this.getUltimaTransicion().getSoloLectura();
			}
			else{
				return false;
			}
		}
		else{
			return true;
		}
	}
	
	@Override
	public void propiedadesSoloLecturaAlEditar(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion) {
		if (configuracion != null){
			if (configuracion.getCotizacionSoloLectura()){
				propiedadesSoloLectura.add("cotizacion");
			}
			else{
				propiedadesEditables.add("cotizacion");
			}
			
			if (configuracion.getEmpresaSoloLectura()){
				propiedadesSoloLectura.add("empresa");
			}
			else{
				propiedadesEditables.add("empresa");
			}
			
			if (configuracion.getMonedaSoloLectura()){
				propiedadesSoloLectura.add("moneda");
			}
			else{
				propiedadesEditables.add("moneda");
			}
			
			if (!this.numeraSistema()){
				propiedadesSoloLectura.add("numero");
			}
			else if(this.getEmpresa() != null){
				if (configuracion.tieneNumerador(this.getEmpresa())){
					propiedadesSoloLectura.add("numero");
				}
				else{
					propiedadesEditables.add("numero");
				}
			}
			else{
				propiedadesSoloLectura.add("numero");
			}
		}
		else{
			if (!this.numeraSistema()){
				propiedadesSoloLectura.add("numero");
			}
			else{
				propiedadesEditables.add("numero");
			}
		}
		
	}
	
	protected void registrarEstadisticaItems(){
	}
	
	public void abrirTransaccion(){
		if (this.getEstado().equals(Estado.Borrador)){
			this.grabarTransaccion();
			this.setEstado(Estado.Abierta);
		}
		else{
			throw new ValidationException("No se puede abrir en estado " + this.estado.toString());
		}
	}
	
	public void cambiarEstadoAProcesando(){
		if (this.estado.equals(Estado.Borrador) || this.estado.equals(Estado.Abierta)){
			Messages errores = new Messages();
			this.validacionesPreConfirmarTransaccion(errores);
			if (!errores.isEmpty()){
				throw new ValidationException(errores);
			}
			this.setEstado(Estado.Procesando);
			try{
				this.cambiandoEstado = Estado.Procesando;	
				this.grabarTransaccion();
				this.cumplirPendientes();
			}
			finally{
				this.cambiandoEstado = null;
			}
		}
		else{
			throw new ValidationException("No se puede poner en proceso un comprobante en estado " + this.estado.toString());
		}
	}
	
	public void volverEstadoAnteriorProcesando(){
		if (this.getEstado().equals(Estado.Procesando)){
			this.setEstado(Estado.Abierta);
			this.liberarPendientesCumplidos();
		}
		else{
			throw new ValidationException("El comprobante no esta en estado " + Estado.Procesando.toString());
		}
	}
	
	public void confirmarTransaccion(){
		if ((this.estado.equals(Estado.Borrador)) || (this.estado.equals(Estado.Abierta)) || (this.estado.equals(Estado.Procesando))){
			try{	
				if (this.getEstado().equals(Estado.Borrador) || this.getEstado().equals(Estado.Abierta)){
					Messages errores = new Messages();
					this.validacionesPreConfirmarTransaccion(errores);
					if (!errores.isEmpty()){
						throw new ValidationException(errores);
					}
					this.setEstado(Estado.Confirmada);
					this.cambiandoEstado = Estado.Confirmada;
					this.setFechaConfirmacion(new Date());
					this.grabarTransaccion();
					this.cumplirPendientes();
				}
				else if (this.getEstado().equals(Estado.Procesando)){
					this.setEstado(Estado.Confirmada);
					this.cambiandoEstado = Estado.Confirmada;
					this.setFechaConfirmacion(new Date());
				}
				
				this.registrarEstadisticaItems();			
				
				this.preConfirmarTransaccion();
				
				// Los motores se deben ejecutar antes de la numeración de la transacción, por bloqueo
				Collection<Kardex> kardex = new LinkedList<Kardex>();
				if (this instanceof ITransaccionInventario){
					this.actualizarInventario(kardex);
				}
				
				Collection<MovimientoValores> finanzas = new LinkedList<MovimientoValores>();
				if (this instanceof ITransaccionValores){
					if (((ITransaccionValores)this).actualizarFinanzasAlConfirmar()){
						this.actualizarFinanzas(finanzas);
					}
				}
				
				this.numerar();
						
				if (this instanceof ITransaccionCtaCte){
					this.ctaCteCreado = CuentaCorriente.crearCuentaCorriente((ITransaccionCtaCte)this);
				}
				
				if (this instanceof ITransaccionContable){
					Asiento.crearAsientoContable((ITransaccionContable)this);
				}
				this.posConfirmarTransaccion();
				
				this.crearPendientes();
				registrarKardex(kardex);
				registrarMovimientosFinancieros(finanzas, false);	
				
				this.registrarTrazabilidadWorkFlow();
				
				EstadoEntidad estadoAlConfirmar = this.configurador().getEstadoConfirmacion();
				if (estadoAlConfirmar != null){
					this.setSubestado(estadoAlConfirmar);
				}
			}
			finally{
				this.cambiandoEstado = null;
			}
			
		}
		else{
			throw new ValidationException("No se puede confirmar en estado " + this.estado.toString());
		}
	}

	protected void validacionesPreConfirmarTransaccion(Messages errores){
	}
	
	protected void preConfirmarTransaccion(){
		
	}
	
	protected void posConfirmarTransaccion(){
	}
	
	protected void posAnularTransaccion(){		
	}
	
	protected void validacionesPreAnularTransaccion(Messages errores){		
	}
	
	public void anularTransaccion() {
		if (this.getEstado().equals(Estado.Confirmada)){
			this.verificarTransaccionesDestinoParaAnular();
			
			Messages errores = new Messages();
			this.validacionesPreAnularTransaccion(errores);
			if (!errores.isEmpty()){
				throw new ValidationException(errores);
			}
			
			this.setEstado(Estado.Anulada);
			
			this.liberarPendientesCumplidos();
			
			if (this instanceof ITransaccionInventario){
				if (((ITransaccionInventario)this).revierteInventarioAlAnular()){
					this.anularActualizacionInventario();
				}
			}
			if (this instanceof ITransaccionValores){
				if (((ITransaccionValores)this).revierteFinanzasAlAnular()){
					this.anularActualizacionFinanzas();
				}
			}
			
			if (this instanceof ITransaccionCtaCte){
				CuentaCorriente.anularCuentaCorriente((ITransaccionCtaCte)this);
			}
			
			if (this instanceof ITransaccionContable){
				Asiento.anularAsientoContable((ITransaccionContable)this);
			}
			
			this.posAnularTransaccion();
		}
		else{
			throw new ValidationException("No se puede anular en estado " + this.getEstado().toString());
		}
	}

	// Este método debe ser llamado para confirmar una transacción desde otro proceso
	// En el caso de factura electrónica, se sobreescribe este método para que además de confirmar solicite el cae
	public void impactarTransaccion(){
		this.confirmarTransaccion();
	}
	
	private void verificarTransaccionesDestinoParaAnular(){
		Collection<Class<?>> tiposTr = new LinkedList<Class<?>>();
		this.tipoTrsDestino(tiposTr);
		if (!tiposTr.isEmpty()){
			Collection<Transaccion> trsgeneradas = new LinkedList<Transaccion>();
			this.getTransaccionesGeneradas(trsgeneradas);
			org.openxava.util.Messages messages = new org.openxava.util.Messages();
			for(Transaccion trdestino: trsgeneradas){
				if (trdestino.getEstado().equals(Estado.Confirmada)){
					if (!trdestino.estaRevertida()){
						messages.add(trdestino.toString() + " estado " + trdestino.getEstado().toString());
					}
				}
				else if (!trdestino.getEstado().equals(Estado.Cancelada) && (!trdestino.getEstado().equals(Estado.Anulada))){
					messages.add(trdestino.toString() + " estado " + trdestino.getEstado().toString());					
				}
			}
			if (!messages.isEmpty()){
				messages.add("Primero debe anular/cancelar/borrar estas operaciones");
				throw new ValidationException(messages);
			}
			else{
				// Anulación de pendientes generados
				for(Class<?> tipoTr: tiposTr){
					Pendiente pendiente = this.buscarPendiente(tipoTr);
					if (pendiente != null){
						pendiente.setAnulado(true);
					}
				}
			}
		}
	}
	
	public void liberarPendientesCumplidos(){
		IEstrategiaCancelacionPendiente cancelacion = establecerEstrategiaCancelacionPendiente();
		if (cancelacion != null){
			cancelacion.liberarPendientes();
		}
	}
	
	public void cancelarTransaccion(){
		if ( (this.getEstado().equals(Estado.Abierta)) || (this.getEstado().equals(Estado.Borrador))){
			this.setEstado(Estado.Cancelada);
			this.registrarEstadisticaItems();
		}
		else{
			throw new ValidationException("No se puede cancelar en estado " + this.getEstado().toString());
		}
	}
	
	public String nombreReporteImpresion() {
		if (this.configurador().getImpresionPorEmpresa()){
			return this.getClass().getSimpleName() + Integer.toString(this.getEmpresa().getNumero()) + "_reporte.jrxml";
		}
		else{
			return this.getClass().getSimpleName() + "_reporte.jrxml";
		}
	}
	
	public void agregarParametrosImpresion(Map<String, Object> parameters) {
		parameters.put("COMPROBANTE", this.getId());
		parameters.put("LOGO", ConfiguracionERP.pathConfig().concat("logo.png"));
		parameters.put("USUARIO_EJECUCION", Users.getCurrent());
		parameters.put("USUARIO_CREO", this.getUsuario());		
		if (this.getEmpresa() != null) {
			parameters.put("EMPRESA", this.getEmpresa().getNombre());
			parameters.put("CODIGO_EMPRESA", this.getEmpresa().getCodigo());
			parameters.put("CUIT_EMPRESA", this.getEmpresa().getCuit());
			parameters.put("RAZONSOCIAL_EMPRESA", this.getEmpresa().getRazonSocial());
			parameters.put("IIBB", this.getEmpresa().getIngresosBrutos());
			parameters.put("INICIOACTIVIDAD", DateFormat.getDateInstance(DateFormat.LONG).format(this.getEmpresa().getInicioActividad()));
			parameters.put("INICIOACTIVIDADDATE", this.getEmpresa().getInicioActividad());
			parameters.put("EMAIL_EMPRESA", this.getEmpresa().getMail());
			parameters.put("TELEFONO_EMPRESA", this.getEmpresa().getTelefono());
			parameters.put("WEB_EMPRESA", this.getEmpresa().getWeb());
			parameters.put("DIRECCION_EMPRESA", this.getEmpresa().getDomicilio().getDireccion());
			parameters.put("CIUDAD_EMPRESA", this.getEmpresa().getDomicilio().getCiudad().getCiudad());
			parameters.put("PROVINCIAL_EMPRESA", this.getEmpresa().getDomicilio().getCiudad().getProvincia().getProvincia());
			parameters.put("CODIGOPOSTAL_EMPRESA", this.getEmpresa().getDomicilio().getCiudad().getCodigoPostal());
		}
		else{
			parameters.put("EMPRESA", "");
			parameters.put("CODIGO_EMPRESA", "");
			parameters.put("CUIT_EMPRESA", "");
			parameters.put("RAZONSOCIAL_EMPRESA", "");
			parameters.put("INICIOACTIVIDAD", DateFormat.getDateInstance(DateFormat.LONG).format(new Date()));
			parameters.put("INICIOACTIVIDADDATE", new Date());
			parameters.put("EMAIL_EMPRESA", "");
			parameters.put("TELEFONO_EMPRESA", "");
			parameters.put("WEB_EMPRESA", "");
			parameters.put("DIRECCION_EMPRESA", "");
			parameters.put("CIUDAD_EMPRESA", "");
			parameters.put("PROVINCIAL_EMPRESA", "");
			parameters.put("CODIGOPOSTAL_EMPRESA", "");
		}
		parameters.put("FECHA", DateFormat.getDateInstance(DateFormat.LONG).format(getFecha()));
		parameters.put("FECHADATE", getFecha());
		parameters.put("NUMERO", this.getNumero());
		parameters.put("OBSERVACIONES", this.getObservaciones());
		
		if (this.getMoneda() != null){
			parameters.put("MONEDA_ID", this.getMoneda().getId());
			parameters.put("MONEDA_NOMBRE", this.getMoneda().getNombre());
			parameters.put("MONEDA_SIMBOLO", this.getMoneda().getSimbolo());
		}
		else{
			parameters.put("MONEDA_ID", "");
			parameters.put("MONEDA_NOMBRE", "");
			parameters.put("MONEDA_SIMBOLO", "");
		}
		
		if (this.getSucursal() != null){
			parameters.put("SUCURSAL_CODIGO", this.getSucursal().getCodigo());
			parameters.put("SUCURSAL_NOMBRE", this.getSucursal().getNombre());
			parameters.put("SUCURSAL_EMAIL", this.getSucursal().getMail());
			parameters.put("SUCURSAL_TELEFONO", this.getSucursal().getTelefono());
			if (this.getSucursal().getDomicilio() != null){
				parameters.put("SUCURSAL_PROVINCIA", this.getSucursal().getDomicilio().getProvincia().getProvincia());
				parameters.put("SUCURSAL_CIUDAD", this.getSucursal().getDomicilio().getCiudad().getCiudad());
				parameters.put("SUCURSAL_CODIGOPOSTAL", this.getSucursal().getDomicilio().getCiudad().getCodigoPostal());
				parameters.put("SUCURSAL_DIRECCION", this.getSucursal().getDomicilio().getDireccion());
			}
		}
	}		
		
	public EstadoEntidad ejecutarTransicion(TransicionEstado transicion){
		boolean estadoInvalido = false;
		EstadoEntidad origen = transicion.getOrigen();
		if (origen == null){
			if (this.getSubestado() != null){
				estadoInvalido = true;
			}
		}
		else if (this.getSubestado() == null){
			estadoInvalido = true;
		}
		else if (!this.getSubestado().equals(origen)){
			estadoInvalido = true;
		}
		
		if (estadoInvalido){
			throw new ValidationException("Estado inválido para ejecutar transición");
		}
		
		EstadoEntidad destino = this.establecerEstadoDestino(transicion);
		if (destino == null){
			throw new ValidationException("En la transición " + transicion.getNombre() + " no se encontró estado destino al cual cambiar");
		}
		if (puedeCambiarEstado(destino.getEstadoTransaccional())){
			if (destino.getEstadoTransaccional().equals(Estado.Confirmada)){
				if (!this.getEstado().equals(Estado.Confirmada)){
					this.confirmarTransaccion();
				}
			}
			else if (destino.getEstadoTransaccional().equals(Estado.Anulada)){
				if (!this.getEstado().equals(Estado.Anulada)){
					this.anularTransaccion();
				}
			}
			else if (destino.getEstadoTransaccional().equals(Estado.Cancelada)){
				if (!this.getEstado().equals(Estado.Cancelada)){
					this.cancelarTransaccion();
				}
			}
			else if (destino.getEstadoTransaccional().equals(Estado.Abierta)){
				if (!this.getEstado().equals(Estado.Abierta)){
					this.abrirTransaccion();
					
					if (this.configurador() != null){
						if (this.configurador().getNumeraEnEstado() != null){
							if (this.configurador().getNumeraEnEstado().equals(destino)){
								this.numerar();
							}
						}
					}
				}
				if (transicion.getSoloLectura()){
					this.registrarEstadisticaItems();
				}
			}
			else{
				if (transicion.getSoloLectura()){
					this.registrarEstadisticaItems();
				}
				
			}
		}
		else{
			throw new ValidationException("No se puede pasar de " + this.getEstado().toString() + " a " + destino.getEstadoTransaccional().toString());
		}
		
		return destino;
	}
	
	protected EstadoEntidad establecerEstadoDestino(TransicionEstado transicion){
		return transicion.getDestino();
	}
	
	public boolean puedeCambiarEstado(Estado estado){
		boolean puedeCambiarEstado = true;
		if (this.getEstado().equals(Estado.Abierta)){
			if (estado.equals(Estado.Borrador)) puedeCambiarEstado = false;
			else if (estado.equals(Estado.Anulada)) puedeCambiarEstado = false;
		}
		else if (this.getEstado().equals(Estado.Anulada)){
			puedeCambiarEstado = false;
		}
		else if (this.getEstado().equals(Estado.Confirmada)){
			if (estado.equals(Estado.Borrador)) puedeCambiarEstado = false;
			else if (estado.equals(Estado.Abierta)) puedeCambiarEstado = false;
			else if (estado.equals(Estado.Cancelada)) puedeCambiarEstado = false;			
		}
		else if (this.getEstado().equals(Estado.Cancelada)){
			puedeCambiarEstado = false;
		}
		else if (this.getEstado().equals(Estado.Borrador)){
			if (estado.equals(Estado.Anulada)) puedeCambiarEstado = false;
		}
		else if (this.getEstado().equals(Estado.Procesando)){
			puedeCambiarEstado = false;
		}
		return puedeCambiarEstado;
	}

	
	public void accionesValidas(List<String> showActions, List<String> hideActions) {
				
		if (TransicionEstado.tieneTransiciones(this.getClass().getSimpleName(), this.getSubestado())){
			if (!this.soloLectura()){
				showActions.add(ACCIONGRABAR);
			}
			else{
				hideActions.add(ACCIONGRABAR);
			}
			showActions.add(ACCIONCAMBIOESTADO);
			
			hideActions.add(ACCIONCONFIRMAR);
			hideActions.add(ACCIONANULAR);
			hideActions.add(ACCIONCANCELAR);
		}
		else if (getEstado().equals(Estado.Borrador)){
			showActions.add(ACCIONGRABAR);
			showActions.add(ACCIONCONFIRMAR);
			
			hideActions.add(ACCIONCAMBIOESTADO);
			hideActions.add(ACCIONANULAR);
			hideActions.add(ACCIONCANCELAR);
		}
		else if (getEstado().equals(Estado.Confirmada)){
			showActions.add(ACCIONANULAR);
			
			hideActions.add(ACCIONCONFIRMAR);
			hideActions.add(ACCIONCAMBIOESTADO);
			hideActions.add(ACCIONGRABAR);
			hideActions.add(ACCIONCANCELAR);
		}
		else if (getEstado().equals(Estado.Anulada)){
			hideActions.add(ACCIONANULAR);
			hideActions.add(ACCIONCONFIRMAR);
			hideActions.add(ACCIONCAMBIOESTADO);
			hideActions.add(ACCIONGRABAR);
			hideActions.add(ACCIONCANCELAR);
		}
		else if (getEstado().equals(Estado.Procesando)){
			// TO do: ver transicion 
			showActions.add(ACCIONCONFIRMAR);
			
			hideActions.add(ACCIONGRABAR);
			hideActions.add(ACCIONCAMBIOESTADO);
			hideActions.add(ACCIONANULAR);
			hideActions.add(ACCIONCANCELAR);
		}
		else if (getEstado().equals(Estado.Abierta)){
			showActions.add(ACCIONCONFIRMAR);			
			showActions.add(ACCIONGRABAR);
			showActions.add(ACCIONCANCELAR);
			
			hideActions.add(ACCIONCAMBIOESTADO);
			hideActions.add(ACCIONANULAR);
		}
		
		if (this.configurador().getActivarEnvioMail() && this.estadoValidoParaEnvioMail()){
			showActions.add("Transaccion.EMail");
		}
		else{
			hideActions.add("Transaccion.EMail");
		}
		
	}

	public static void accionesValidasAlCrear(String tipoEntidad, ConfiguracionEntidad configuracion, List<String> showActions, List<String> hideActions) {
		EstadoEntidad estadoInicial = ConfiguracionEntidad.buscarEstadoPorDefecto(tipoEntidad);
		showActions.add(ACCIONGRABAR);
		if (TransicionEstado.tieneTransiciones(tipoEntidad, estadoInicial)){
			showActions.add(ACCIONCAMBIOESTADO);
			hideActions.add(ACCIONCONFIRMAR);
		}
		else{
			showActions.add(ACCIONCONFIRMAR);
			hideActions.add(ACCIONCAMBIOESTADO);
		}
		
		hideActions.add(ACCIONANULAR);
		hideActions.add(ACCIONCANCELAR);
		
		if ((configuracion != null) && (configuracion.getActivarEnvioMail()) && (configuracion.getEnvioMailCualquierEstado())){
			showActions.add("Transaccion.EMail");
		}
		else{
			hideActions.add("Transaccion.EMail");
		}
	}
			
	public Boolean numeraSistema(){
		return true;
	}
	
	private void numerar(){
		if (Is.emptyString(getNumero())){			
			if (this.numeraSistema()){
				Numerador numerador = null;
				if (this.numeradorSeleccionado() != null){
					numerador = ConfiguracionEntidad.buscarNumeradorParaActualizar(this.numeradorSeleccionado().getId());
				}
				if (numerador == null){
					numerador = ConfiguracionEntidad.buscarNumeradorPorEmpresaParaActualizar(UtilERP.tipoEntidad(this).getSimpleName(), this.getEmpresa());
				}
								
				if (numerador != null){
					if (numerador.getEmpresa().equals(this.getEmpresa())){
						numerador.numerarObjetoNegocio(this);
					}
					else{
						throw new ValidationException("Error al numerador: numerador no permitido para la empresa " + this.getEmpresa().toString());
					}
				}
				else{
					throw new ValidationException("Número no asignado");
				}
			}
		}
		
		if (!Is.emptyString(this.getNumero())){
			if (this.numeroRepetido()){
				throw new ValidationException("Número repetido");
			}
		}
	}
	
	protected boolean numeroRepetido(){
		return false;
	}
	
	@Override
	public void copiarPropiedades(Object objeto){
		super.copiarPropiedades(objeto);
		this.setEstado(Estado.Borrador);
		
		Calendar cal = Calendar.getInstance();		
		cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
		cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
		cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
		cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
		this.setFecha(cal.getTime());
		
		this.setNumero("");
		this.setNumeroInterno(null);
		this.setSubestado(null);
		this.setUltimaTransicion(null);
		this.setVersion(0);
	}
	
	public Pendiente buscarPendiente(Class<?> tipoTransaccionDestino){
		Class<?> tipoPendiente = getTipoPendiente(tipoTransaccionDestino);
		Query query = XPersistence.getManager().createQuery("from " + tipoPendiente.getSimpleName() + " p where " + 
								"p.idTrOrigen = :id and p.tipoTrDestino = :tipoTrDestino");
		query.setParameter("id", this.getId());
		query.setParameter("tipoTrDestino", tipoTransaccionDestino.getSimpleName());
		query.setMaxResults(1);
		Pendiente pend = null;
		
		@SuppressWarnings("unchecked")
		List<Pendiente> resultado = (List<Pendiente>)query.getResultList();
			
		if (!resultado.isEmpty()){
			pend = (Pendiente)resultado.iterator().next();
		}		
		return pend;
	}
	
	public Pendiente buscarPendienteGeneradoPorTr(String tipoPendiente){
		Query query = XPersistence.getManager().createQuery("from " + tipoPendiente + " p where " + 
				"p.idTrOrigen = :id");
		query.setParameter("id", this.getId());		
		query.setMaxResults(1);
		Pendiente pend = null;

		@SuppressWarnings("unchecked")
		List<Pendiente> resultado = (List<Pendiente>)query.getResultList();
		if (!resultado.isEmpty()){
			pend = (Pendiente)resultado.iterator().next();
		}		
		return pend;
	}
	
	public Pendiente buscarPendienteParaProcesar(String tipoTransaccionDestino){
		Collection<Class<?>> clases = new LinkedList<Class<?>>();
		this.tipoTrsDestino(clases);
		Class<?> claseDestino = null;
		for (Class<?> clase: clases){
			if (clase.getSimpleName().equals(tipoTransaccionDestino)){
				claseDestino = clase;
				break;
			}
		}
		if (claseDestino != null){
			return this.buscarPendienteParaProcesar(claseDestino);
		}
		else{
			return null;
		}
	}
	
	public Pendiente buscarPendienteParaProcesar(Class<?> tipoTransaccionDestino) {
		Pendiente pendiente = null;
		if (this.getEstado().equals(Estado.Confirmada)){
			Pendiente pendienteEncontrado = this.buscarPendiente(tipoTransaccionDestino);
			if (pendienteEncontrado != null){
				if ((!pendienteEncontrado.getCumplido()) && (!pendienteEncontrado.getAnulado())){
					pendiente = pendienteEncontrado;
				}	
			}
			else{
				String mensaje = "No se encontró pendiente para el comprobante " + this.getNumero();
				throw new ValidationException(mensaje);
			}			
		}
		else{
			throw new ValidationException("No esta confirmado el comprobante " + this.getNumero());
		}
		return pendiente;
	}

	public void generarTransaccionesDestino(List<Pendiente> pendientes, List<Transaccion> transaccionesDestino) {
		
		if (!pendientes.isEmpty()){
			verificarTransaccionesGeneradas(pendientes);			
			List<List<Pendiente>> grupoPendientes = new LinkedList<List<Pendiente>>();
			Pendiente.separarPendientes(grupoPendientes, pendientes);
			
			for (List<Pendiente> lista: grupoPendientes){
				Transaccion origen = lista.get(0).origen();
				origen.crearTransaccionesDestino(lista, transaccionesDestino);	
			}
		}
	}
	
	public void generarTransaccionesDestinoDesdeItems(List<IItemPendiente> itemsPendiente, List<Transaccion> transaccionesDestino){
		// misma lógica que generarTransaccionesDestino, pero antes de procesar, se incluyen únicamente los items que estén en clavesItemsPendiente
		if (!itemsPendiente.isEmpty()){
			List<Pendiente> pendientes = new LinkedList<Pendiente>();
			Map<String, Object> pendientesProcesados = new HashMap<String, Object>();
			Map<String, IItemPendiente> mapItemsPendientes = new HashMap<String, IItemPendiente>();
			for(IItemPendiente itemPendiente: itemsPendiente){
				mapItemsPendientes.put(itemPendiente.getItem().getId(), itemPendiente);
				
				Pendiente pendiente = itemPendiente.getPendiente();
				if (!pendientesProcesados.containsKey(pendiente.getId())){
					pendientes.add(pendiente);
					pendientesProcesados.put(pendiente.getId(), null);
				}
			}
			
			verificarTransaccionesGeneradas(pendientes);			
			List<List<Pendiente>> grupoPendientes = new LinkedList<List<Pendiente>>();
			Pendiente.separarPendientes(grupoPendientes, pendientes);
			for (List<Pendiente> lista: grupoPendientes){
				Transaccion origen = lista.get(0).origen();
				origen.crearTransaccionesDestinoDesdeItems(lista, transaccionesDestino, mapItemsPendientes);	
			}
		}
	}
	
	public void verificarTransaccionesGeneradas(List<Pendiente> pendientes){
		Messages mensajes = new Messages();
		for(Pendiente p: pendientes){
			if (!p.getCumplido()){
				if (p.getEjecutado()){
					mensajes.add(p.origen().toString() + " ha generado operaciones que están pendientes");					
				}
			}
			else{
				mensajes.add(p.toString() + " ya fue cumplido");
			}
		}
		if (!mensajes.isEmpty()){
			throw new ValidationException(mensajes);
		}
	}
	
	private void crearTransaccionesDestinoDesdeItems(List<Pendiente> pendientes, List<Transaccion> creadas, Map<String, IItemPendiente> itemsIncluidos){
		// se aplica la lógica para todo el pendiente y luego se filtran los items de pendiente que no estén en ItemsIncluidos
		Pendiente tipoPendiente = pendientes.get(0);
		List<List<IItemPendiente>> grupoItemsPendientes = new LinkedList<List<IItemPendiente>>();
		tipoPendiente.separarItemsPendientes(pendientes, grupoItemsPendientes);
		
		if (!grupoItemsPendientes.isEmpty()){
			for (List<IItemPendiente> items: grupoItemsPendientes){
				Transaccion destino = tipoPendiente.crearTransaccionDestino();
				List<IItemPendiente> itemsFiltrados = new LinkedList<IItemPendiente>();
				for (IItemPendiente item: items){
					if (itemsIncluidos.containsKey(item.getItem().getId())){
						itemsFiltrados.add(itemsIncluidos.get(item.getItem().getId()));
					}
				}
				
				if (!itemsFiltrados.isEmpty()){
					this.pasajeAtributosWorkFlow(destino, itemsFiltrados);
					creadas.add(destino);
				}
			}
		}
		else{
			throw new ValidationException("Procesamiento de Items de Pendientes: no hay items");
		}
	}
	
	private void crearTransaccionesDestino(List<Pendiente> pendientes, List<Transaccion> creadas){
		Pendiente tipoPendiente = pendientes.get(0);
		List<List<IItemPendiente>> grupoItemsPendientes = new LinkedList<List<IItemPendiente>>();
		tipoPendiente.separarItemsPendientes(pendientes, grupoItemsPendientes);
		if (!grupoItemsPendientes.isEmpty()){
			for (List<IItemPendiente> items: grupoItemsPendientes){
				Transaccion destino = tipoPendiente.crearTransaccionDestino();
				this.pasajeAtributosWorkFlow(destino, items);
				creadas.add(destino);
			}
		}
		else{
			// se genera una sola transacción para todos los pendientes agrupados
			Transaccion destino = tipoPendiente.crearTransaccionDestino();
			this.pasajeAtributosWorkFlowSinItems(destino, pendientes);			
			creadas.add(destino);
		}
		
	}
	
	private void pasajeAtributosWorkFlowSinItems(Transaccion destino, List<Pendiente> pendientes){
		this.inicializarTrCreadaPorWorkFlow(destino);		
		pasajeAtributosWorkFlowSinItemsPrePersist(destino, pendientes);
		XPersistence.getManager().persist(destino);
		pasajeAtributosWorkFlowSinItemsPosPersist(destino, pendientes);
		
		destino.grabarTransaccion();
	}
			
	protected void pasajeAtributosWorkFlowSinItemsPrePersist(Transaccion destino, List<Pendiente> pendientes){
		// sobreescribir este método cuando se usa una relación de transacción de cabecera, sin items de pendiente
	}
	
	protected void pasajeAtributosWorkFlowSinItemsPosPersist(Transaccion destino, List<Pendiente> pendientes){
		// sobreescribir este método cuando se usa una relación de transacción de cabecera, sin items de pendiente
	}
	
	protected void pasajeAtributosWorkFlow(Transaccion destino, List<IItemPendiente> items){
		this.inicializarTrCreadaPorWorkFlow(destino);
			
		pasajeAtributosWorkFlowPrePersist(destino, items);
		XPersistence.getManager().persist(destino);
		pasajeAtributosWorkFlowPosPersist(destino, items);
			
		destino.grabarTransaccion();
	}
	
	protected void pasajeAtributosWorkFlowPrePersist(Transaccion destino, List<IItemPendiente> items){
		// sobreescribir este método cuando se usa una relación de transacción con items pendientes  
	}
	
	protected void pasajeAtributosWorkFlowPosPersist(Transaccion destino, List<IItemPendiente> items){
		// sobreescribir este método cuando se usa una relación de transacción con items pendientes		
	}
	
	public void inicializarTrCreadaPorWorkFlow(Transaccion destino){
		destino.copiarPropiedades(this);
		ConfiguracionEntidad conf = destino.configurador();
		if (conf != null){
			if (!conf.getMonedaPorCircuito()){
				destino.setMoneda(destino.buscarMonedaDefault());
			}
		}
		
		// Cotización: si son distintas monedas, se analiza si se copia del origen o se deja en cero para que se busque luego la cotización actual
		if (!Is.equal(this.getMoneda(), destino.getMoneda())){
			// Empresas que tiene misma moneda1 y moneda2
			if (Is.equal(this.getMoneda1(), destino.getMoneda1()) && Is.equal(this.getMoneda2(), destino.getMoneda2())){	
				if (!Is.equal(this.getMoneda(), destino.getMoneda1()) && !Is.equal(this.getMoneda(), destino.getMoneda2())){
					// Si las monedas son diferentes a la 1 y 2, no se puede copiar la cotización directamente, no tiene sentido
					if (Is.equal(destino.getMoneda(), destino.getMoneda1()) || Is.equal(destino.getMoneda(), destino.getMoneda2())){						
						destino.setCotizacion(this.getCotizacion2());
					}
					else{
						destino.setCotizacion(BigDecimal.ZERO);
					}
				}
			}							
		}	
	}
	
	private void crearPendientes(){
		LinkedList<Pendiente> pendientes = new LinkedList<Pendiente>();
		this.generarPendientes(pendientes);
		for(Pendiente pendiente: pendientes){
			XPersistence.getManager().persist(pendiente);
		}
	}
	
	public Class<?> getTipoPendiente(Class<?> tipoTransaccionDestino){
		
		String className = tipoTransaccionDestino.getName();
		String classSimpleName = tipoTransaccionDestino.getSimpleName();
		String pendienteClassName = className.split(classSimpleName)[0] + "Pendiente" + classSimpleName;
		try {
			return Class.forName(pendienteClassName);
		} catch (ClassNotFoundException e) {
			throw new ValidationException("Falta definir la clase para el pendiente: " + pendienteClassName );
		}
	}
		
	private void generarPendientes(Collection<Pendiente> pendientes){
		Collection<Class<?>> tiposTrDestino = new LinkedList<Class<?>>();
		this.tipoTrsDestino(tiposTrDestino);
		for(Class<?> tipoTr: tiposTrDestino){
			Class<?> tipoPendiente = this.getTipoPendiente(tipoTr);
			this.agregarPendientesPorTipoTrDestino(tipoPendiente, tipoTr, pendientes);			
		}
	}
	
	private void agregarPendientesPorTipoTrDestino(Class<?> tipoPendiente, Class<?> tipoTrDestino, Collection<Pendiente> pendientes){
		if (this.cumpleCondicionGeneracionPendiente(tipoTrDestino)){
			pendientes.add(crearPendientesPorTipoTrDestino(tipoPendiente));
		}
	}

	// Este método indica si la transacción generar un pendiente o no para la transacción destino
	// Reescribir si de acuerdo a una condición se genera o no el pendiente
	protected boolean cumpleCondicionGeneracionPendiente(Class<?> tipoTrDestino){
		return true;
	}
	
	protected Pendiente crearPendientesPorTipoTrDestino(Class<?> tipoPendiente){
		try {
			Constructor<?> constructor = tipoPendiente.getConstructor(new Class[]{Transaccion.class});
			
			try {
				Pendiente pendiente = (Pendiente)constructor.newInstance(this);
				pendiente.inicializar(this);
				return pendiente;
			} catch (InstantiationException e) {
				throw new ValidationException("Generación pendiente: " + tipoPendiente.getSimpleName() + " "+ e.toString());					
			} catch (IllegalAccessException e) {
				throw new ValidationException("Generación pendiente: " + tipoPendiente.getSimpleName() + " "+ e.toString());
			} catch (IllegalArgumentException e) {
				throw new ValidationException("Generación pendiente: " + tipoPendiente.getSimpleName() + " "+ e.toString());
			} catch (InvocationTargetException e) {
				String detalleError = e.toString();
				if (e.getTargetException() != null){
					detalleError = e.getTargetException().toString();
				}				 
				throw new ValidationException("Generación pendiente: " + tipoPendiente.getSimpleName() + " "+ detalleError);
			}
		} catch (NoSuchMethodException e) {
			throw new ValidationException("Error al generar pendientes: Falta definir el constructor para el pendiente de " + tipoPendiente.getSimpleName());

		} catch (SecurityException e) {
			throw new ValidationException("Error al generar pendientes " + tipoPendiente.getSimpleName() + ": " + e.toString());
		}
	}
	
	// Método a definir por las transacciones que generan workflow 
	public void tipoTrsDestino(Collection<Class<?>> tipoTrsDestino){
	}
	
	@Transient
	private IEstrategiaCancelacionPendiente cancelacionPendiente = null;
		
	private void cumplirPendientes(){
		this.cancelacionPendiente = establecerEstrategiaCancelacionPendiente();
		if (cancelacionPendiente != null){			
			cancelacionPendiente.cancelarPendientes();
		}
	}
	
	private void registrarTrazabilidadWorkFlow(){
		if (this.cancelacionPendiente == null){
			this.cancelacionPendiente = this.establecerEstrategiaCancelacionPendiente();
		}
		
		if (this.cancelacionPendiente != null){
			Collection<Pendiente> pendientes = new LinkedList<Pendiente>();
			cancelacionPendiente.pendientesProcesados(pendientes);
			if (pendientes != null){
				for(Pendiente p: pendientes){
					Transaccion origen = p.origen();					
					if (origen != null){
						Trazabilidad.crearTrazabilidad(origen, p.getTipoTrOrigen(), this, this.getClass().getSimpleName());
					}
				}
			}
			this.cancelacionPendiente = null;
		}		
	}
	
	protected IEstrategiaCancelacionPendiente establecerEstrategiaCancelacionPendiente(){
		return null;
	}
	
	public void getTransaccionesGeneradas(Collection<Transaccion> trs){
		throw new ValidationException("No se pudo obtener las transacciones generadas");
	}
	
	
	
	@ReadOnly
	@ListProperties("usuario, fechaCreacion, transicion.nombre")
	public Collection<EjecucionCambioEstado> getHistoricoEstados(){
		if (!Is.emptyString(this.getId())){
			Query query = XPersistence.getManager().createQuery("from EjecucionCambioEstado e where e.idEntidad = :id");
			query.setParameter("id", this.getId());
			try{
				@SuppressWarnings("unchecked")
				List<EjecucionCambioEstado> resultado = (List<EjecucionCambioEstado>)query.getResultList();
				Collection<EjecucionCambioEstado> historico = new ArrayList<EjecucionCambioEstado>();
				historico.addAll(resultado);
				return historico;
			}
			catch(Exception e){
				return Collections.emptyList();
			}
		}
		else{
			return Collections.emptyList();
		}
		
	}
	
	private void actualizarFinanzas(Collection<MovimientoValores> detalleMovFinancieros){
		List<IItemMovimientoValores> movimientosValores = new LinkedList<IItemMovimientoValores>();
		((ITransaccionValores)this).movimientosValores(movimientosValores);
		if (!movimientosValores.isEmpty()){
			Valor.actualizarValores(this, movimientosValores, detalleMovFinancieros, false);
		}
	}
	
	private void anularActualizacionFinanzas(){
		List<IItemMovimientoValores> movimientosValores = new LinkedList<IItemMovimientoValores>();
		((ITransaccionValores)this).movimientosValores(movimientosValores);
		if (!movimientosValores.isEmpty()){
			Collection<MovimientoValores> detalleMovFinancieros = new LinkedList<MovimientoValores>();
			Valor.actualizarValores(this, movimientosValores, detalleMovFinancieros, true);
			registrarMovimientosFinancieros(detalleMovFinancieros, true);
		}
	}
	
	private void registrarMovimientosFinancieros(Collection<MovimientoValores> finanzas, boolean revierte){
		if (!finanzas.isEmpty()){
			Map<String, Object> idsTr = new HashMap<String, Object>();
			for(MovimientoValores item: finanzas){				
				item.setTipoTrDestino(this.getClass().getSimpleName());
				item.setIdTransaccion(this.getId());
				item.setFechaComprobante(this.getFecha());
				item.setNumeroComprobante(this.getNumero());
				item.setTipoComprobante(this.descripcionTipoTransaccion());
					
				((ITransaccionValores)this).antesPersistirMovimientoFinanciero(item);				
				XPersistence.getManager().persist(item);
				((ITransaccionValores)this).despuesPersistirMovimientoFinanciero(item, revierte);
				
				// Por cada idTransaccion, se genera un movimiento financiero.
				// en general, cada transacción es un único movimiento financiero, pero el caso de la 
				// cobranza, genera un recibo por empresa
				if (!idsTr.containsKey(item.getIdTransaccion())){
					idsTr.put(item.getIdTransaccion(), null);
					
					if (! revierte){
						// se registra un movimiento por transacción
						MovimientoFinanciero movFinanciero = MovimientoFinanciero.crearMovimientoFinanciero(item, (ITransaccionValores)this);				
						XPersistence.getManager().persist(movFinanciero);
					}
					else{
						// se anula el movimiento financiero
						MovimientoFinanciero movFinanciero = MovimientoFinanciero.buscarMovimientoFinanciero(item); 
						if (movFinanciero != null){
							movFinanciero.setAnulado(Boolean.TRUE);
							XPersistence.getManager().persist(movFinanciero);
						}
						else{
							throw new ValidationException("No se encontró movimiento financiero: " + item.getIdTransaccion());
						}
					}
					
				}
			}
			
			
		}
	}
	
	public void actualizarInventario(Collection<Kardex> kardex){
		ArrayList<IItemMovimientoInventario> movimientosInventario = ((ITransaccionInventario)this).movimientosInventario();
		if (movimientosInventario != null){
			Inventario.actualizarInventario(movimientosInventario, false, kardex);
		}
	}
	
	private void registrarKardex(Collection<Kardex> kardex){
		for(Kardex item: kardex){
			item.setTipoTrDestino(this.getClass().getSimpleName());
			item.setIdTransaccion(this.getId());
			item.setFechaComprobante(this.getFecha());
			item.setNumero(this.getNumero());
			item.setTipoComprobante(this.descripcionTipoTransaccion());
			item.setClienteProveedor(((ITransaccionInventario)this).empresaExternaInventario());
			XPersistence.getManager().persist(item);
		}
	}
	
	public void anularActualizacionInventario(){
		ArrayList<IItemMovimientoInventario> movimientosInventario = ((ITransaccionInventario)this).movimientosInventario();
		if (movimientosInventario != null){
			Collection<Kardex> kardex = new LinkedList<Kardex>();
			Inventario.actualizarInventario(movimientosInventario, true, kardex);
			registrarKardex(kardex);
		}
	}
			
	public abstract String descripcionTipoTransaccion();
	
	protected void atributosConversionesMoneda(List<String> atributos){
	}
	
	public void sincronizarMonedas(Object object, List<String> atributos){
		if (!atributos.isEmpty()){
			
			if ((this.getMoneda() != null) && (this.getMoneda1() != null) && (this.getMoneda2() != null)){
				if ((this.getCotizacion().compareTo(BigDecimal.ZERO) != 0) && (this.getCotizacion2().compareTo(BigDecimal.ZERO) != 0)){
					for(String atributo: atributos){						
						try {
							int escala = ((BigDecimal)object.getClass().getMethod("get" + atributo + "1").invoke(object)).scale();
							if (escala == 0){
								escala = 2;
							}
																					
							Method setAtributo1 = object.getClass().getMethod("set" + atributo + "1", BigDecimal.class);
							BigDecimal nuevoImporte1 = (BigDecimal)object.getClass().getMethod("get" + atributo).invoke(object);
							if (!this.getMoneda().equals(this.getMoneda1())){
								nuevoImporte1 = nuevoImporte1.multiply(this.getCotizacion()).setScale(escala, RoundingMode.HALF_EVEN);
							}							
							
							setAtributo1.invoke(object, nuevoImporte1);
							
							BigDecimal nuevoImporte2 = nuevoImporte1.divide(this.getCotizacion2(), escala, RoundingMode.HALF_EVEN);
							
							Method setAtributo2 = object.getClass().getMethod("set" + atributo + "2", BigDecimal.class);
							setAtributo2.invoke(object, nuevoImporte2);
							
						} catch (Exception e) {
							throw new ValidationException("Error al sincronizar Monedas: " + e.toString());
						}
					}
				}
			}
		}
	}
	
	public BigDecimal calcularImporte1(BigDecimal importeMonedaTr){
		if (this.getMoneda().equals(this.getMoneda1())){
			return importeMonedaTr;
		}
		else{
			return importeMonedaTr.multiply(this.getCotizacion()).setScale(2, RoundingMode.HALF_EVEN);
		}
	}
	
	public BigDecimal calcularImporte2(BigDecimal importeMoneda1){
		return importeMoneda1.divide(this.getCotizacion2(), 2, RoundingMode.HALF_EVEN);
	}
	
	// métodos genéricos de contabilidad
	
	public Transaccion ContabilidadTransaccion(){
		return this;
	}

	public Date ContabilidadFecha(){
		return this.getFecha();
	}

	public String ContabilidadDetalle(){
		return this.descripcionTipoTransaccion() + " " + this.getNumero();
	}
	
	public boolean generaContabilidad(){
		// por defecto si se activa la contabilidad, se genera
		return true;
	}
	
	public boolean contabilizaEnCero(){
		// por defecto no se genera asiento si el importe es cero
		return false;
	}
	
	public boolean confirmada(){
		return Is.equal(this.getEstado(), Estado.Confirmada);
	}
	
	public boolean cerrado(){
		if (!Is.emptyString(this.getId()) && (this.getEstado() != null)){
			if (this.getEstado().equals(Estado.Anulada) || this.getEstado().equals(Estado.Confirmada)){
				return true;
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
	}
	
	public boolean finalizada(){
		boolean finalizada = this.cerrado();
		if (!finalizada){
			if (Is.equal(this.getEstado(), Estado.Cancelada)){
				finalizada = true;
			}
		}
		return finalizada;

	}
	
	@Transient
	private CuentaCorriente ctaCteCreado = null;
	
	public CuentaCorriente comprobanteCuentaCorriente(){
		if (this.ctaCteCreado != null){
			return this.ctaCteCreado;
		}
		else{
			return CuentaCorriente.buscarCuentaCorriente((ITransaccionCtaCte)this);
		}
	}
	
	@ReadOnly
	@ListProperties("fechaCreacion, comprobanteOrigen, comprobanteDestino, usuario")
	@ListAction("TrazabilidadTransaccion.graficar")
	public Collection<Trazabilidad> getTrazabilidad(){
		if (this.cerrado()){
			ArrayList<Trazabilidad> trazabilidad = new ArrayList<Trazabilidad>();
			Trazabilidad.buscarTrazabilidadCompleta(trazabilidad, this);			
			trazabilidad.sort(new ComparatorTrazabilidad());
			return trazabilidad;
		}
		else{
			return Collections.emptyList();
		}
	}

	@ListProperties(value="fechaCreacion, modificacion, valorAnterior, valorNuevo, usuario")
	public Collection<Auditoria> getRegistroModificaciones(){
		return Auditoria.registrosAuditoria(this);
	}
	
	public boolean refrescarColecciones(){
		return false;
	}
	
	public ConfiguracionEMail emailConfigurador() {
		return this.configurador().getConfiguracionEmail();
	}
	
	public String emailAsunto() {
		String asunto = this.configurador().getAsunto();
		if (asunto != null){
			asunto += " ";
		}
		else{
			asunto = " ";
		}
		if (!Is.emptyString(this.getNumero())){
			asunto += this.getNumero();
		}
		return asunto;
	}
	
	public String emailContenido() {
		return this.configurador().getCuerpoMensaje();
	}
	
	public boolean estadoValidoParaEnvioMail(){
		boolean valido = false;
		if (this.getEstado().equals(Estado.Confirmada)){
			valido = true;
		}
		else if (this.configurador().getEnvioMailCualquierEstado()){
			if (this.getEstado().equals(Estado.Abierta) || (this.getEstado().equals(Estado.Borrador))){
				valido = true;
			}
		}		
		return valido;
	}
	
	public void agregarItemsMultiseleccion(Map<?, ?>[] keys){
		if (this.soloLectura()){
			Messages errors = new Messages();
			errors.add("no_modificar");
			throw new ValidationException(errors);
		}
		
		boolean itemAgregado = false;
		Map<String, Object> itemsMultiseleccion = new HashMap<String, Object>();
		for (int i = 0; i < keys.length; i++) {
			Map<?, ?> key = keys[i];
		
			if (agregarItemDesdeMultiseleccion(key, itemsMultiseleccion)){
				itemAgregado = true;
			}						
		}
		if (itemAgregado){
			this.grabarTransaccion();
		}
		
	}
	
	protected boolean agregarItemDesdeMultiseleccion(Map<?, ?> key, Map<String, Object> itemsMultiseleccion){
		return false;
	}
	
	protected boolean estaRevertida(){
		return false;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	public int nroCopiasImpresion() {	
		return this.configurador().getNroCopias();
	}
	
	public void permiteCambiarAtributo(String nombreVista) {
		super.permiteCambiarAtributo(nombreVista);
		if (Is.equal(this.getEstado(), Estado.Abierta)){
			if (!this.soloLectura()){
				throw new ValidationException("Acción no permitida en estado " + this.getEstado().toString());
			}
		}
		else if (!Is.equal(this.getEstado(), Estado.Confirmada)){
			throw new ValidationException("Acción no permitida en estado " + this.getEstado().toString());
		}		
	}
}
