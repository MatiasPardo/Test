package org.openxava.contabilidad.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.calculators.CurrentDateCalculator;
import org.openxava.jpa.*;
import org.openxava.negocio.filter.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

@Entity

@Views({
	@View(members=
		"Principal{Principal[" + 
			"fechaCreacion, estado, fechaServicio;" +
			"fecha, numero, clasificacionAsiento;" +
			"detalle];" +
			"observaciones;" +
			"originante;" + 
			"tipoAsiento;" +
			"items}" + 
		"Trazabilidad{trazabilidad}" +
		"ContraAsiento{contraAsiento}"
		),
	@View(name="Simple",
		members="numero"),
	@View(name="Contraasiento",
		members="numero, fecha"),
	@View(name="ReimputarCentroCostos", members= 
		"Principal{Principal[" + 
				"fechaCreacion, estado;" +
				"detalle];" +
				"observaciones;" +
				"items}" + 
			"Trazabilidad{trazabilidad}")
})

@Tab(filter=SucursalEmpresaFilter.class,
	baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL,
	properties="numero, fecha, estado, detalle, observaciones, fechaCreacion, usuario",
	defaultOrder="${fechaCreacion} desc")

public class Asiento extends Transaccion implements IGeneradoPor{

	private static final String ERROR_MAXIMO_REDONDEO = "0.04";
	
	public static Asiento existeAsiento(String numero) {
		Query query = XPersistence.getManager().createQuery("from Asiento where numero = :numero");
		query.setParameter("numero", numero);		
		query.setMaxResults(1);
		List<?> results = query.getResultList();
		if (!results.isEmpty()){
			return (Asiento)results.get(0);
		}
		else{
			return null;
		}
	}
	
	private static BigDecimal convertirImporte(BigDecimal importe, BigDecimal cotizacion){
		return importe.multiply(cotizacion);		
	}
	
	public static Asiento crearAsientoContable(ITransaccionContable tr){
		Transaccion transaccion = tr.ContabilidadTransaccion();
		if (transaccion.getEmpresa().getGeneraContabilidad() && tr.generaContabilidad()){
			Asiento nuevo = new Asiento();
			nuevo.copiarPropiedades(transaccion);
			
			nuevo.setFecha(tr.ContabilidadFecha());
			nuevo.setDetalle(tr.ContabilidadDetalle());
			nuevo.setIdTransaccion(transaccion.getId());
			nuevo.setTipoTransaccion(transaccion.getClass().getSimpleName());
			nuevo.setOriginante(tr.ContabilidadOriginante());
			XPersistence.getManager().persist(nuevo);
			Collection<IGeneradorItemContable> items = new LinkedList<IGeneradorItemContable>();
			tr.generadorPasesContable(items);
			if (!items.isEmpty()){
				BigDecimal cotizacion = transaccion.buscarCotizacionTrConRespectoA(transaccion.getMoneda1());
				List<ItemAsiento> itemsAsientos = new LinkedList<ItemAsiento>();
				
				boolean distribuirCentroCostos = false;
				for(IGeneradorItemContable itemGenerador: items){
					if ((itemGenerador.igcDebeOriginal().compareTo(BigDecimal.ZERO) != 0) ||
						(itemGenerador.igcHaberOriginal().compareTo(BigDecimal.ZERO) != 0) ||
						tr.contabilizaEnCero()){
						ItemAsiento nuevoItem = new ItemAsiento();
						nuevoItem.setAsiento(nuevo);
						nuevoItem.setCuenta(itemGenerador.igcCuentaContable());
						if (itemGenerador.igcDebeOriginal().compareTo(BigDecimal.ZERO) >= 0){
							nuevoItem.setDebe(Asiento.convertirImporte(itemGenerador.igcDebeOriginal(), cotizacion));
						}
						else{
							nuevoItem.setHaber(Asiento.convertirImporte(itemGenerador.igcDebeOriginal().negate(), cotizacion));
						}
						
						if (itemGenerador.igcHaberOriginal().compareTo(BigDecimal.ZERO) >= 0){
							nuevoItem.setHaber(nuevoItem.getHaber().add( Asiento.convertirImporte(itemGenerador.igcHaberOriginal(), cotizacion)) );
						}
						else{
							nuevoItem.setDebe(nuevoItem.getDebe().add( Asiento.convertirImporte(itemGenerador.igcHaberOriginal().negate(), cotizacion) ));
						}
						nuevoItem.setCentroCostos(itemGenerador.igcCentroCostos());
						if (nuevoItem.getCentroCostos() == null && nuevoItem.getCuenta().getCentroCostoObligatorio()){
							throw new ValidationException("Falta asignar centro de costos " + nuevoItem.getCuenta().getCodigo() + " - " + nuevoItem.getCuenta().getNombre());
						}
						UnidadNegocio unidadNegocio = itemGenerador.igcUnidadNegocio();
						if (unidadNegocio == null){
							if (nuevoItem.getCentroCostos() != null){
								unidadNegocio = nuevoItem.getCentroCostos().getUnidadNegocio();
							}							 
						}
						nuevoItem.setUnidadNegocio(unidadNegocio);
						nuevoItem.setDetalle(itemGenerador.igcDetalle());
								
						// Se intentan fusionar los items que son iguales
						boolean agregarItem = false;
						if (itemsAsientos.isEmpty()){
							agregarItem = true;
						}
						else{
							boolean fusion = false;
							for(ItemAsiento itemAsiento: itemsAsientos){
								fusion = nuevoItem.fusionarA(itemAsiento);
								if (fusion){ 
									break;
								}
							}
							if (!fusion){
								agregarItem = true;
							}
						}
						
						if (agregarItem){
							itemsAsientos.add(nuevoItem);
							
							if (!distribuirCentroCostos){
								distribuirCentroCostos =nuevoItem.distribuyePorCentroCostos();								
							}
							
						}
					}
				}
				
				if (distribuirCentroCostos){
					List<ItemAsiento> distribucionCC = new LinkedList<ItemAsiento>();
					Asiento.explotarItemsPorDistribucionCentroCostos(itemsAsientos, distribucionCC);
					itemsAsientos = distribucionCC;
				}
				
				// se redondean a dos decimales los items fusionados
				BigDecimal totalDebe = BigDecimal.ZERO;
				BigDecimal totalHaber = BigDecimal.ZERO;
				BigDecimal totalDebeSinRedondear = BigDecimal.ZERO;
				BigDecimal totalHaberSinRedondear = BigDecimal.ZERO;
				for(ItemAsiento itemAsiento: itemsAsientos){
					
					totalDebeSinRedondear = totalDebeSinRedondear.add(itemAsiento.getDebe());
					totalHaberSinRedondear = totalHaberSinRedondear.add(itemAsiento.getHaber());
					
					itemAsiento.setDebe(itemAsiento.getDebe().setScale(2, RoundingMode.HALF_EVEN));
					itemAsiento.setHaber(itemAsiento.getHaber().setScale(2, RoundingMode.HALF_EVEN));
					
					totalDebe = totalDebe.add(itemAsiento.getDebe());
					totalHaber = totalHaber.add(itemAsiento.getHaber());						
				}
				
				if ((tr.ContabilidadTotal() != null) && (tr.ContabilidadTotal().compareTo(BigDecimal.ZERO) > 0)){
					// si hay diferencia de redondeo se ajusta para llegar al total de la contabilidad
					if ((totalDebe.compareTo(tr.ContabilidadTotal()) != 0) || (totalHaber.compareTo(tr.ContabilidadTotal()) != 0)){
						BigDecimal totalAsiento = tr.ContabilidadTotal().setScale(2, RoundingMode.HALF_EVEN);
						BigDecimal diferenciaDebe = totalDebe.subtract(totalAsiento);
						boolean ajustarDebe = false;
						if (diferenciaDebe.compareTo(BigDecimal.ZERO) != 0){
							if (Asiento.cumpleErrorRelativoPorcentual(diferenciaDebe, totalAsiento)){
								ajustarDebe = true;
							}
						}
						BigDecimal diferenciaHaber = totalHaber.subtract(totalAsiento);
						boolean ajustarHaber = false;
					    if (diferenciaHaber.compareTo(BigDecimal.ZERO) != 0){
					    	if (Asiento.cumpleErrorRelativoPorcentual(diferenciaHaber, totalAsiento)){
					    		ajustarHaber = true;
					    	}
						}
						
						Iterator<ItemAsiento> iterator = itemsAsientos.iterator();
						while(iterator.hasNext() &&  (ajustarDebe || ajustarHaber)){
							ItemAsiento itemAsiento = iterator.next();
							if (ajustarDebe){
								if (itemAsiento.getDebe().compareTo(BigDecimal.ZERO) != 0){
									itemAsiento.setDebe(itemAsiento.getDebe().subtract(diferenciaDebe));
									totalDebe = totalDebe.subtract(diferenciaDebe);
									ajustarDebe = false;
								}
							}
							if (ajustarHaber){
								if (itemAsiento.getHaber().compareTo(BigDecimal.ZERO) != 0){
									itemAsiento.setHaber(itemAsiento.getHaber().subtract(diferenciaHaber));
									totalHaber = totalHaber.subtract(diferenciaHaber);
									ajustarHaber = false;
								}
							}
						}						
					}
				}
				else if (totalDebe.compareTo(totalHaber) != 0){
					BigDecimal margenError = new BigDecimal(ERROR_MAXIMO_REDONDEO);
					// problema de redondeo, se ajusta el primer item para que redonde
					BigDecimal diferencia = totalDebeSinRedondear.subtract(totalHaberSinRedondear).setScale(2, RoundingMode.HALF_EVEN);
					if (diferencia.abs().compareTo(margenError) <= 0){
						diferencia = totalDebe.subtract(totalHaber);
						ItemAsiento primerItemAsiento = itemsAsientos.get(0);
						if (primerItemAsiento.getDebe().compareTo(BigDecimal.ZERO) != 0){
							primerItemAsiento.setDebe(primerItemAsiento.getDebe().subtract(diferencia));
							totalDebe = totalDebe.subtract(diferencia);
						}
						else{
							primerItemAsiento.setHaber(primerItemAsiento.getHaber().add(diferencia));
							totalHaber = totalHaber.add(diferencia);
						}						
					}
				}
								
				if (totalDebe.compareTo(totalHaber) != 0){
					throw new ValidationException("No coincide el debe con el haber: " + totalDebe.toString() + " es diferente a " + totalHaber.toString());
				}
				
				// se persisten los items
				for(ItemAsiento itemAsiento: itemsAsientos){
					XPersistence.getManager().persist(itemAsiento);
				}
				nuevo.setItems(new ArrayList<ItemAsiento>());
				nuevo.getItems().addAll(itemsAsientos);
				nuevo.confirmarTransaccion();
				
				Trazabilidad.crearTrazabilidad(transaccion, null, nuevo, null);
			}
			else{
				throw new ValidationException("Error al generar la contabilidad: transacción sin items");
			}
			return nuevo;
		}
		else{
			return null;
		}
	}
	
	private static void explotarItemsPorDistribucionCentroCostos(Collection<ItemAsiento> items, Collection<ItemAsiento> distribucionGenerada){
		for(ItemAsiento itemAsiento: items){
			if (itemAsiento.distribuyePorCentroCostos()){
				Collection<DistribucionCentroCosto> distribucionCompleta = itemAsiento.getCentroCostos().distribucionCompletaCentroCostos();
				for(DistribucionCentroCosto distribucionCC: distribucionCompleta){
					ItemAsiento nuevoItem = new ItemAsiento();
					nuevoItem.copiarPropiedades(itemAsiento);
					nuevoItem.setCentroCostos(distribucionCC.getDistribucionCostos());					
					nuevoItem.distribuir(distribucionCC.getPorcentaje());
					
					// Se intentan fusionar los items que son iguales
					boolean agregarItem = false;
					if (distribucionGenerada.isEmpty()){
						agregarItem = true;
					}
					else{
						boolean fusion = false;
						for(ItemAsiento itemAsientoDistribuido: distribucionGenerada){
							fusion = nuevoItem.fusionarA(itemAsientoDistribuido);
							if (fusion){ 
								break;
							}
						}
						if (!fusion){
							agregarItem = true;
						}
					}
					
					if (agregarItem){
						distribucionGenerada.add(nuevoItem);
					}
				}
			}
			else{
				distribucionGenerada.add(itemAsiento);
			}
		}
	}
	
	public static void anularAsientoContable(ITransaccionContable tr){
		Transaccion transaccion = tr.ContabilidadTransaccion();
		if (transaccion.getEmpresa().getGeneraContabilidad() && tr.generaContabilidad()){
			String id = transaccion.getId();
			String tipoTr = transaccion.getClass().getSimpleName();
			Query query = XPersistence.getManager().createQuery("from Asiento where idTransaccion = :id and tipoTransaccion = :tipo");
			query.setParameter("id", id);
			query.setParameter("tipo", tipoTr);
			query.setMaxResults(1);
			List<?> results = query.getResultList();
			if (!results.isEmpty()){
				Asiento asiento = (Asiento)results.get(0);
				asiento.generarContrasiento(transaccion.configurador().getRevierteAsientoFechaOrigen());
			}
			else{
				throw new ValidationException("No se puede anular la contabilidad: no se encontró el asiento");
			}
		}
	}
	
	public static Asiento crearAsiento(Empresa empresa, Sucursal sucursal, Date fecha, String detalle){
		Asiento nuevo = new Asiento();
		nuevo.setEmpresa(empresa);
		nuevo.setSucursal(sucursal);
		nuevo.setFecha(fecha);
		nuevo.setDetalle(detalle);
		return nuevo;
	}
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@ReadOnly
	private EjercicioContable ejercicio;
		
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@ReadOnly
	private PeriodoContable periodo;
	
	@ReadOnly
	private Integer libroDiario;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate
	@NoModify
	private TipoAsiento tipoAsiento;
	
	@DefaultValueCalculator(CurrentDateCalculator.class)
	private Date fechaServicio = null;
	
	@ReadOnly
	private TipoComportamientoAsiento clasificacionAsiento = TipoComportamientoAsiento.Manual;
	
	@Column(length=100)
	@Required
	@ReadOnly(forViews= "ReimputarCentroCostos")
	private String detalle;
	
	@OneToMany(mappedBy="asiento", cascade=CascadeType.ALL) 
	@ListProperties("cuenta.codigo, cuenta.nombre, detalle, debe, haber, centroCostos.nombre")
	@CollectionView(value="ReimputarCentroCostos", forViews="ReimputarCentroCostos")
	@SaveAction(value="ItemTransaccion.save")
	@NewAction(value="ItemTransaccion.new", notForViews="ReimputarCentroCostos")
	@HideDetailAction(value="ItemTransaccion.hideDetail")
	@RemoveAction(value="ItemTransaccion.remove")
	@RemoveSelectedAction(value="ItemTransaccion.removeSelected")
	@EditAction("ItemTransaccion.edit")
	private Collection<ItemAsiento> items = new ArrayList<ItemAsiento>();

	@Hidden
	@ReadOnly
	@Column(length=32)
	private String idTransaccion;
	
	@Hidden
	@ReadOnly
	@Column(length=100)
	private String tipoTransaccion;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly
	@ReferenceView("Contraasiento")
	private Asiento contraAsiento;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly
	@NoCreate @NoModify 
	@ReferenceView("Simple")
	private EmpresaExterna originante;
	
	public EjercicioContable getEjercicio() {
		return ejercicio;
	}

	public void setEjercicio(EjercicioContable ejercicio) {
		this.ejercicio = ejercicio;
	}

	public PeriodoContable getPeriodo() {
		return periodo;
	}

	public void setPeriodo(PeriodoContable periodo) {
		this.periodo = periodo;
		if (periodo != null){
			this.setEjercicio(periodo.getEjercicio());
		}
	}

	public Collection<ItemAsiento> getItems() {
		return items;
	}

	public void setItems(Collection<ItemAsiento> items) {
		this.items = items;
	}

	public TipoAsiento getTipoAsiento() {
		return tipoAsiento;
	}

	public void setTipoAsiento(TipoAsiento tipoAsiento) {
		this.tipoAsiento = tipoAsiento;
	}
	
	
	public String getDetalle() {
		return detalle;
	}

	public void setDetalle(String detalle) {
		this.detalle = detalle;
	}

	@Override
	public void grabarTransaccion(){
		super.grabarTransaccion();
		
		boolean buscarPeriodo = true;
		if (this.periodo != null){
			if (periodo.valido(this.getFecha())){
				buscarPeriodo = false;
			}
		}
		
		if (buscarPeriodo){
			PeriodoContable periodo = EjercicioContable.buscarPeriodo(this.getFecha());
			if (periodo == null){
				throw new ValidationException("Falta definir el periodo contable para " + this.getFecha().toString());
			}
			else{
				this.setPeriodo(periodo);
			}
		}
		if (!this.getPeriodo().permiteAsientos()){
			throw new ValidationException("No se puede grabar asientos en el periodo " + this.getPeriodo().getNombre());
		}
		
		if (!Is.equal(this.getClasificacionAsiento(), TipoComportamientoAsiento.ReimputacionCC)){
			if (!Is.emptyString(this.getIdTransaccion())){
				this.setClasificacionAsiento(TipoComportamientoAsiento.Automatico);
			}
			else if (this.getContraAsiento() != null){
				this.setClasificacionAsiento(this.getContraAsiento().getClasificacionAsiento());
			}
		}		
	}
	
	@Override
	protected void validacionesPreConfirmarTransaccion(Messages errores){
		super.validacionesPreConfirmarTransaccion(errores);
		
		BigDecimal saldo = BigDecimal.ZERO;
		BigDecimal saldoDebe = BigDecimal.ZERO;
		BigDecimal saldoHaber = BigDecimal.ZERO;
		if (this.getItems() != null){
			for (ItemAsiento item: this.getItems()){
				saldo = saldo.add(item.getDebe().subtract(item.getHaber()));
				saldoDebe = saldoDebe.add(item.getDebe());
				saldoHaber = saldoHaber.add(item.getHaber());
			}
		}
		
		if (Is.equal(this.getClasificacionAsiento(), TipoComportamientoAsiento.ReimputacionCC)){
			Asiento asientoOriginal = (Asiento)this.buscarObjetoGenerador();
			if (asientoOriginal == null){
				throw new ValidationException("Es una reimputación de centro de costos y no tiene asiento asociado");
			}
			
			BigDecimal debeAsientoOriginal = BigDecimal.ZERO;			
			for(ItemAsiento itemAsientoOriginal: asientoOriginal.getItems()){
				debeAsientoOriginal = debeAsientoOriginal.add(itemAsientoOriginal.getDebe());
			}
			if (saldoDebe.compareTo(debeAsientoOriginal) != 0){
				errores.add("En una reimputación de centro de costos el Debe tiene que coincidir con el asiento original, que es " + UtilERP.convertirString(debeAsientoOriginal));
			}
			if (saldoHaber.compareTo(debeAsientoOriginal) != 0){
				errores.add("En una reimputación de centro de costos el Haber tiene que coincidir con el asiento original, que es " + UtilERP.convertirString(debeAsientoOriginal));
			}
		}
		
		if (saldo.compareTo(BigDecimal.ZERO) != 0){
			errores.add("Saldo debe ser cero: " + saldo.toString());
		}
	}
	
	@Override
	protected void posConfirmarTransaccion(){
		super.posConfirmarTransaccion();
		
		if (Is.equal(this.getClasificacionAsiento(), TipoComportamientoAsiento.ReimputacionCC)){
			Asiento asientoOriginal = (Asiento)this.buscarObjetoGenerador();
			asientoOriginal.anularTransaccion();
			Trazabilidad.crearTrazabilidad(asientoOriginal, Asiento.class.getSimpleName(), this, Asiento.class.getSimpleName());
		}
		
		if (this.getFechaServicio() == null){
			this.setFechaServicio(this.getFecha());
		}
	}
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Asiento";
	}

	public String getIdTransaccion() {
		return idTransaccion;
	}

	public void setIdTransaccion(String idTransaccion) {
		this.idTransaccion = idTransaccion;
	}

	public String getTipoTransaccion() {
		return tipoTransaccion;
	}

	public void setTipoTransaccion(String tipoTransaccion) {
		this.tipoTransaccion = tipoTransaccion;
	}
	
	@Override
	public void copiarPropiedades(Object objeto){
		super.copiarPropiedades(objeto);
		this.setContraAsiento(null);
		this.setItems(null);
		this.setIdTransaccion("");
		this.setTipoTransaccion("");		
	}

	@Override
	public String generadaPorId() {
		return this.getIdTransaccion();
	}

	@Override
	public String generadaPorTipoEntidad() {
		return this.getTipoTransaccion();
	}
	
	private Asiento generarContrasiento(boolean fechaOriginal){
		if (this.getContraAsiento() != null){
			throw new ValidationException(this.getNumero() + " ya tiene un contra-asiento generado");
		}
		else if (!this.getEstado().equals(Estado.Confirmada)){
			throw new ValidationException(this.getNumero() + " estado inválido para generar contra-asiento");
		}
		Asiento contraAsiento = new Asiento();
		contraAsiento.copiarPropiedades(this);
		if (fechaOriginal){
			contraAsiento.setFecha(this.getFecha());
		}
		contraAsiento.setIdTransaccion(this.getId());
		contraAsiento.setTipoTransaccion(Asiento.class.getSimpleName());
		contraAsiento.setDetalle("Reversión de asiento " + this.getNumero() + ": " + this.getDetalle());		
		contraAsiento.setItems(new ArrayList<ItemAsiento>());		
		XPersistence.getManager().persist(contraAsiento);
		for(ItemAsiento item: this.getItems()){
			ItemAsiento itemContraAsiento = new ItemAsiento();
			itemContraAsiento.copiarPropiedades(item);
			itemContraAsiento.setAsiento(contraAsiento);
			itemContraAsiento.invertirPaseContable();
			itemContraAsiento.recalcular();
			XPersistence.getManager().persist(itemContraAsiento);
			contraAsiento.getItems().add(itemContraAsiento);
		}
		contraAsiento.confirmarTransaccion();
		
		this.setContraAsiento(contraAsiento);
		Trazabilidad.crearTrazabilidad(this, null, contraAsiento, null);
		return contraAsiento;
	}

	public Asiento getContraAsiento() {
		return contraAsiento;
	}

	public void setContraAsiento(Asiento contraAsiento) {
		this.contraAsiento = contraAsiento;
	}
	
	@Override
	protected void validacionesPreAnularTransaccion(Messages errores){
		super.validacionesPreAnularTransaccion(errores);
		
		if (this.getContraAsiento() != null){
			if (!this.getContraAsiento().getEstado().equals(Estado.Anulada)){
				errores.add("Primero debe anular el contra-asiento");
			}
		}
	}
	
	@Override
	public ObjetoNegocio generarCopia() {
		Asiento copia = new Asiento();
		copia.copiarPropiedades(this);
		copia.setClasificacionAsiento(TipoComportamientoAsiento.Manual);
		copia.setItems(new ArrayList<ItemAsiento>());
		copia.setDetalle("Copiar de " + this.getNumero());
		
		XPersistence.getManager().persist(copia);
		
		for(ItemAsiento item: this.getItems()){
			ItemAsiento itemCopia = new ItemAsiento();
			itemCopia.copiarPropiedades(item);
			itemCopia.setAsiento(copia);
			copia.getItems().add(itemCopia);
			
			XPersistence.getManager().persist(copia);
		}
		return copia;
	}

	public Integer getLibroDiario() {
		return libroDiario;
	}

	public void setLibroDiario(Integer libroDiario) {
		this.libroDiario = libroDiario;
	}

	public TipoComportamientoAsiento getClasificacionAsiento() {
		return this.clasificacionAsiento == null ? TipoComportamientoAsiento.Manual : this.clasificacionAsiento;
	}

	public void setClasificacionAsiento(TipoComportamientoAsiento clasificacionAsiento) {
		if (clasificacionAsiento != null){
			this.clasificacionAsiento = clasificacionAsiento;
		}
	}

	public EmpresaExterna getOriginante() {
		return originante;
	}

	public void setOriginante(EmpresaExterna originante) {
		this.originante = originante;
	}
	
	@Override
	protected void preConfirmarTransaccion(){
		super.preConfirmarTransaccion();
		
		if (this.getClasificacionAsiento().equals(TipoComportamientoAsiento.Manual)){
			this.distribuirAsientoManualPorCentroCostos();
		}
	}
	
	private void distribuirAsientoManualPorCentroCostos(){
		// Solo cuando el asiento se registra en forma manual, ya que los items están persistidos y no se fusionan
		if (!this.getClasificacionAsiento().equals(TipoComportamientoAsiento.Manual)){
			throw new ValidationException("Solo para asientos manuales");
		}
		
		Collection<ItemAsiento> itemsNuevos = new LinkedList<ItemAsiento>();
		for(ItemAsiento itemAsiento: this.getItems()){
			if (itemAsiento.distribuyePorCentroCostos()){
				Collection<DistribucionCentroCosto> distribucionCompleta = itemAsiento.getCentroCostos().distribucionCompletaCentroCostos();
				boolean primerItem = true;
				BigDecimal haberOriginal = itemAsiento.getHaber();
				BigDecimal debeOriginal = itemAsiento.getDebe();				
				BigDecimal importePendiente = itemAsiento.importeAsiento();
				CentroCostos distribucion = itemAsiento.getCentroCostos();
				if (!distribucionCompleta.isEmpty()){
					for(DistribucionCentroCosto distribucionCC: distribucionCompleta){
						ItemAsiento itemDistribucion = itemAsiento;
						if (primerItem){
							primerItem = false;						
						}
						else{
							itemDistribucion = new ItemAsiento();
							itemDistribucion.copiarPropiedades(itemAsiento);
							itemDistribucion.setDebe(debeOriginal);
							itemDistribucion.setHaber(haberOriginal);
							itemsNuevos.add(itemDistribucion);
						}
						itemDistribucion.setCentroCostos(distribucionCC.getDistribucionCostos());
						if (itemDistribucion.getUnidadNegocio() == null){
							itemDistribucion.setUnidadNegocio(distribucion.getUnidadNegocio());
						}							
						itemDistribucion.distribuir(distribucionCC.getPorcentaje());
						itemDistribucion.redondear();
						importePendiente = importePendiente.subtract(itemDistribucion.importeAsiento());
					}				
					itemAsiento.ajustarImporteAsiento(importePendiente);
				}
			}
		}
		
		for(ItemAsiento nuevo: itemsNuevos){
			XPersistence.getManager().persist(nuevo);			
		}
		this.getItems().addAll(itemsNuevos);
	}
	
	public Asiento generarAsientoParaReimputarCentroCostos(){
		if (!this.getEstado().equals(Estado.Confirmada)){
			throw new ValidationException(this.getNumero() + " estado inválido para reimputar centro de costos");
		}
		if (this.getContraAsiento() != null){
			throw new ValidationException(this.getNumero() + " ya tiene un contra-asiento generado, no puede reimputarse");
		}
		Asiento reimputacionCentroCostos = this.generoAsientoReimputacion();
		if (reimputacionCentroCostos != null){
			throw new ValidationException(this.getNumero() + " ya generó un asiento para reimputar");
		}
		if (!this.getPeriodo().permiteAsientos()){
			throw new ValidationException("No se puede grabar asientos en el periodo " + this.getPeriodo().getNombre());
		}
		
		reimputacionCentroCostos = new Asiento();
		reimputacionCentroCostos.copiarPropiedades(this);
		reimputacionCentroCostos.setFecha(this.getFecha());
		reimputacionCentroCostos.setNumero(this.getNumero());
		reimputacionCentroCostos.setNumeroInterno(this.getNumeroInterno());
		reimputacionCentroCostos.setIdTransaccion(this.getId());
		reimputacionCentroCostos.setTipoTransaccion(Asiento.class.getSimpleName());
		reimputacionCentroCostos.setClasificacionAsiento(TipoComportamientoAsiento.ReimputacionCC);
		reimputacionCentroCostos.setItems(new ArrayList<ItemAsiento>());	
		XPersistence.getManager().persist(reimputacionCentroCostos);
		
		for(ItemAsiento item: this.getItems()){
			ItemAsiento itemParaReimputar = new ItemAsiento();
			itemParaReimputar.copiarPropiedades(item);			
			itemParaReimputar.setAsiento(reimputacionCentroCostos);
			itemParaReimputar.recalcular();
			XPersistence.getManager().persist(itemParaReimputar);
			reimputacionCentroCostos.getItems().add(itemParaReimputar);			
		}				
		return reimputacionCentroCostos;
	}

	private Asiento generoAsientoReimputacion() {
		Query query = XPersistence.getManager().createQuery("from Asiento where idTransaccion = :id and clasificacionAsiento = :clasificacion");
		query.setMaxResults(1);
		query.setFlushMode(FlushModeType.COMMIT);
		query.setParameter("id", this.getId());
		query.setParameter("clasificacion", TipoComportamientoAsiento.ReimputacionCC);
		List<?> result = query.getResultList();
		if (!result.isEmpty()){
			return (Asiento)result.get(0);
		}
		else{
			return null;
		}
	}
	
	@Override
	public String viewName(){
		if (Is.equal(this.getClasificacionAsiento(), TipoComportamientoAsiento.ReimputacionCC) && !this.soloLectura()){
			return "ReimputarCentroCostos";
		}
		else{
			return super.viewName();
		}
	}
	
	public ObjetoNegocio buscarObjetoGenerador(){
		ObjetoNegocio obj = null;
		
		if (!Is.emptyString(this.generadaPorId())){
			Query query = XPersistence.getManager().createQuery("from " + this.generadaPorTipoEntidad() + " where id = :id");
			query.setParameter("id", this.generadaPorId());
			query.setMaxResults(1);
			query.setFlushMode(FlushModeType.COMMIT);
			List<?> result = query.getResultList();
			if (!result.isEmpty()){
				obj = (ObjetoNegocio)result.get(0);
			}
		}
		return obj;
	}
	
	private static boolean cumpleErrorRelativoPorcentual(BigDecimal errorAbsoluto, BigDecimal resultadoExacto){
		BigDecimal margenErrorRelativoPorcentual = new BigDecimal(0.01);
		BigDecimal errorRelativoPorcentual = errorAbsoluto.divide(resultadoExacto, 5, RoundingMode.HALF_EVEN).multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_EVEN);
		if (errorRelativoPorcentual.abs().compareTo(margenErrorRelativoPorcentual) <= 0){
			return true;
		}
		else{
			if (errorAbsoluto.abs().compareTo(new BigDecimal(0.01)) <= 0){
				// diferencia de 1 centavo
				// Cuando la diferencia es de 1 centavo, no importe el error relativo. 
				// Esto es para comprobantes de importes menores a 100, que tengan 1 centavo de diferencia es aceptable.
				return true;
			}
			else{
				return false;
			}
		}
	}

	public Date getFechaServicio() {
		return fechaServicio;
	}

	public void setFechaServicio(Date fechaServicio) {
		if (fechaServicio != null){
			this.fechaServicio = UtilERP.trucarDateTime(fechaServicio);
		}
	}	
}
