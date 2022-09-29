package org.openxava.base.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.validators.*;
import org.openxava.calculators.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;


@Entity

@Views({
	@View(members=
			"Principal{" +
					"entidad;" + 
					"Moneda[moneda, monedaPorCircuito; " +
					 	"cotizacionSoloLectura, monedaSoloLectura];" +
					"Empresa[empresa, empresaSoloLectura, revierteAsientoFechaOrigen];" +
					"Numeracion[numeraEnEstado; numerador; numeradores];" + 
					"Estados[estadoInicial, estadoConfirmacion;estados;transiciones]" + 
			"}" +
			"Impresion{impresionAutomatica; concatenarImpresionMultiple; impresionPorEmpresa; nroCopias}" +
			"Imagenes{ocultarImagenes}" + 
			"EMail{activarEnvioMail; envioMailCualquierEstado; asunto; configuracionEmail; cuerpoMensaje}"),
	@View(name="Simple",
		members="entidad")	
})

@EntityValidators({
	@EntityValidator(value=ValidadorConfMailActivo.class, 
				properties={
						@PropertyValue(from="activarEnvioMail", name="activo"),
						@PropertyValue(from="configuracionEmail", name="configuracion")
				})
})

@Tab(
	properties="entidad, moneda.nombre, empresa.nombre, empresaSoloLectura, cotizacionSoloLectura, monedaPorCircuito, monedaSoloLectura, activarEnvioMail, impresionAutomatica"
)

public class ConfiguracionEntidad{
	
	public static Class<?> buscarClase(String tipoEntidad){
		String classname = tipoEntidad;
		Class<?> clase = null;
		if (!Is.emptyString(classname)){
			final Package[] packages = Package.getPackages();
			for (final Package p : packages) {
				final String pack = p.getName();
			    final String tentative = pack + "." + classname;
			    try {
			    	clase = Class.forName(tentative);
			    	break;
			        		        
			    }catch (final ClassNotFoundException e) {
			    	continue;
			    }
			}
		}
		return clase;
	}
	
	public static ConfiguracionEntidad buscarConfigurador(String tipoEntidad){
		String sql = "from ConfiguracionEntidad c where " +
				"c.entidad = :entidad";
		Query query = (Query)XPersistence.getManager().createQuery(sql);
		query.setParameter("entidad", tipoEntidad);
		query.setMaxResults(1);
		
		try{
			return (ConfiguracionEntidad) query.getSingleResult();
		}
		catch(NoResultException ex){
			return null;
		}
	}
	
	public static Numerador buscarNumeradorParaActualizar(String idNumerador){
		String sql = "select n.id from " + Esquema.concatenarEsquema("numerador") + " n where n.id = :id for update";
		Query query = XPersistence.getManager().createNativeQuery(sql);
		query.setParameter("id", idNumerador);
		try{
			String id = (String)query.getSingleResult();
			return (Numerador)XPersistence.getManager().find(Numerador.class, id);
		}
		catch(NoResultException e){
			return null;
		}
	}
	
	public static Numerador buscarNumeradorPorEmpresaParaActualizar(String tipoEntidad, Empresa empresa){
		if (empresa == null){
			throw new ValidationException("No se puede numerar: no esta asignad la empresa");
		}
		String sql = "select n.id from " + Esquema.concatenarEsquema("numerador") + " n where n.entidad_entidad = :entidad and n.empresa_id = :empresa for update";
		Query query = XPersistence.getManager().createNativeQuery(sql);
		query.setParameter("entidad", tipoEntidad);
		query.setParameter("empresa", empresa.getId());
		try{
			String id = (String)query.getSingleResult();
			return (Numerador)XPersistence.getManager().find(Numerador.class, id);
		}
		catch(NonUniqueResultException e){
			throw new ValidationException("Hay más de un numerador definido para " + tipoEntidad + " en la empresa " + empresa.getNombre());
		}
		catch(NoResultException e){
			return null;
		}
	}
	
	public static Numerador buscarNumeradorAutomaticoParaActualizar(String tipoEntidad){
		String sql = "select n.id from " + Esquema.concatenarEsquema("numerador") +" n join " + Esquema.concatenarEsquema("configuracionentidad") + " c on c.numerador_id = n.id and c.entidad = :entidad for update";
		Query query = XPersistence.getManager().createNativeQuery(sql);
		query.setParameter("entidad", tipoEntidad);
		query.setMaxResults(1);
		try{
			String id = (String)query.getSingleResult();
			return (Numerador)XPersistence.getManager().find(Numerador.class, id);
		}
		catch(NoResultException e){
			return null;
		}
		
	}
	
	public static EstadoEntidad buscarEstadoPorDefecto(String tipoEntidad){
		ConfiguracionEntidad configurador = ConfiguracionEntidad.buscarConfigurador(tipoEntidad);
		if (configurador != null){
			return configurador.getEstadoInicial();
		}
		else{
			return null;
		}
	}
	
	public static ConfiguracionEntidad crearConfiguracionTransaccion(String tipoEntidad){
		ConfiguracionEntidad config = new ConfiguracionEntidad();
		config.setEntidad(tipoEntidad);
		Empresa empresa = Empresa.buscarEmpresaPorNro(1);
		if (empresa == null){
			throw new ValidationException("No se encontró la empresa nro 1. No se puede crear la entidad");
		}
		config.setEmpresa(empresa);
		config.setMoneda(empresa.getMoneda1());
		config.ocultarImagenes = Boolean.FALSE;
		config.impresionAutomatica = Boolean.FALSE;
		XPersistence.getManager().persist(config);
		
		Numerador numerador = new Numerador();
		numerador.setEmpresa(empresa);
		numerador.setNombre(tipoEntidad + " - " + empresa.getCodigo());
		numerador.setCantidadDigitos(6);
		numerador.setEntidad(config);
		numerador.setProximoNumero(new Long(1));
		XPersistence.getManager().persist(numerador);
		
		config.getNumeradores().add(numerador);
		
		return config;
	}
	
	@Id
	@Column(length=50) 
	@SearchKey
	private String entidad = "";
		
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate
	@NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private Moneda moneda;
	
	@DefaultValueCalculator(value=TrueCalculator.class)
	private Boolean monedaPorCircuito = Boolean.TRUE;
	
	@DefaultValueCalculator(value=TrueCalculator.class)
	private Boolean cotizacionSoloLectura = Boolean.TRUE;
	
	@DefaultValueCalculator(value=TrueCalculator.class)
	private Boolean monedaSoloLectura = Boolean.TRUE;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(
			depends="entidad",
			condition="${entidad.entidad} = ?")
	private EstadoEntidad estadoInicial;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(
			depends="entidad",
			condition="${entidad.entidad} = ?")
	private EstadoEntidad estadoConfirmacion;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	@Required
	private Empresa empresa;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate
	@DescriptionsList(descriptionProperties="nombre",
					depends="entidad",
					condition="${entidad.entidad} = ?")
	private EstadoEntidad numeraEnEstado;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView(value="Simple")
	private Numerador numerador;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean ocultarImagenes = Boolean.FALSE;
	
	@OneToMany(mappedBy="entidad", cascade=CascadeType.ALL)
	@CollectionView(value="AltaNumerador")
	private Collection<Numerador> numeradores = new ArrayList<Numerador>();
	
	@OneToMany(mappedBy="entidad", cascade=CascadeType.ALL)
	private Collection<EstadoEntidad> estados = new ArrayList<EstadoEntidad>();
	
	@OneToMany(mappedBy="entidad", cascade=CascadeType.ALL)
	@ReadOnly
	@ListProperties("nombre, origen.nombre, destino1.nombre, condicion1, destino2.nombre")
	@CollectionView(value="Simple")
	private Collection<TransicionEstado> transiciones = new ArrayList<TransicionEstado>();
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean impresionAutomatica = Boolean.FALSE;

	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean concatenarImpresionMultiple = Boolean.FALSE;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean impresionPorEmpresa = Boolean.FALSE;
	
	private Integer nroCopias = 1;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean activarEnvioMail = Boolean.FALSE;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean envioMailCualquierEstado = Boolean.FALSE;
	
	@Column(length=50) 	
	private String asunto; 
	
	@Stereotype("MEMO") @Column(length=511)	
	private String cuerpoMensaje;
		
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	private ConfiguracionEMail configuracionEmail;
	
	@DefaultValueCalculator(value=TrueCalculator.class)
	private Boolean empresaSoloLectura = Boolean.TRUE;
		
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean revierteAsientoFechaOrigen = Boolean.FALSE;
	
	public Boolean getActivarEnvioMail() {
		return activarEnvioMail == null ? Boolean.FALSE : this.activarEnvioMail;
	}

	public void setActivarEnvioMail(Boolean activarEnvioMail) {
		this.activarEnvioMail = activarEnvioMail;
	}

	public Boolean getEnvioMailCualquierEstado() {
		return envioMailCualquierEstado == null ? Boolean.FALSE : this.envioMailCualquierEstado;
	}

	public void setEnvioMailCualquierEstado(Boolean envioMailCualquierEstado) {
		this.envioMailCualquierEstado = envioMailCualquierEstado;
	}

	public String getAsunto() {
		return asunto;
	}

	public void setAsunto(String asunto) {
		this.asunto = asunto;
	}

	public ConfiguracionEMail getConfiguracionEmail() {
		return configuracionEmail;
	}

	public void setConfiguracionEmail(ConfiguracionEMail configuracionEmail) {
		this.configuracionEmail = configuracionEmail;
	}

	public String getEntidad() {
		return entidad;
	}
	
	public void setEntidad(String entidad) {
		this.entidad = entidad;
	}
	
	public Collection<EstadoEntidad> getEstados() {
		return estados;
	}

	public void setEstados(Collection<EstadoEntidad> estados) {
		this.estados = estados;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public Empresa empresaDefault(){
		Empresa emp = this.getEmpresa();		
		return emp;
	}
	
	public Collection<TransicionEstado> getTransiciones() {
		return transiciones;
	}

	public void setTransiciones(Collection<TransicionEstado> transiciones) {
		this.transiciones = transiciones;
	}
	
	public EstadoEntidad getEstadoInicial() {
		return estadoInicial;
	}

	public void setEstadoInicial(EstadoEntidad estadoInicial) {
		this.estadoInicial = estadoInicial;
	}

	public EstadoEntidad getEstadoConfirmacion() {
		return estadoConfirmacion;
	}

	public void setEstadoConfirmacion(EstadoEntidad estadoConfirmacion) {
		this.estadoConfirmacion = estadoConfirmacion;
	}

	public Boolean getOcultarImagenes() {
		return ocultarImagenes == null ? Boolean.FALSE : this.ocultarImagenes;
	}

	public void setOcultarImagenes(Boolean ocultarImagenes) {
		this.ocultarImagenes = ocultarImagenes;
	}
	
	@PrePersist
	private void onPrePersist() {
		Class<?> clase = ConfiguracionEntidad.buscarClase(this.getEntidad());		
		if (clase == null){
			throw new ValidationException("No existe la entidad " + this.getEntidad());
		}
		
		if (this.getEstadoInicial() != null){
			if (!this.getEstadoInicial().getEstadoTransaccional().equals(Estado.Borrador)){
				throw new ValidationException("El estado inicial debe ser " + Estado.Borrador.toString());
			}
		}
		
		if (this.getEstadoConfirmacion() != null){
			if (!this.getEstadoConfirmacion().getEstadoTransaccional().equals(Estado.Confirmada)){
				throw new ValidationException("El estado al confirmar debe ser " + Estado.Confirmada.toString());
			}
		}
	}

	public Moneda getMoneda() {
		return moneda;
	}

	public void setMoneda(Moneda moneda) {
		this.moneda = moneda;
	}

	public Boolean getCotizacionSoloLectura() {
		return this.cotizacionSoloLectura == null ? Boolean.TRUE : this.cotizacionSoloLectura;
	}

	public void setCotizacionSoloLectura(Boolean cotizacionSoloLectura) {
		this.cotizacionSoloLectura = cotizacionSoloLectura;
	}

	public EstadoEntidad getNumeraEnEstado() {
		return numeraEnEstado;
	}

	public void setNumeraEnEstado(EstadoEntidad numeraEnEstado) {
		this.numeraEnEstado = numeraEnEstado;
	}

	public Collection<Numerador> getNumeradores() {
		if (numeradores == null){
			this.numeradores = new ArrayList<Numerador>();
		}
		return numeradores;
	}

	public void setNumeradores(Collection<Numerador> numeradores) {
		this.numeradores = numeradores;
	}

	public Numerador getNumerador() {
		return numerador;
	}

	public void setNumerador(Numerador numerador) {
		this.numerador = numerador;
	}

	public Boolean getImpresionAutomatica() {
		return impresionAutomatica;
	}

	public void setImpresionAutomatica(Boolean impresionAutomatica) {
		this.impresionAutomatica = impresionAutomatica;
	}

	public Boolean getConcatenarImpresionMultiple() {
		return concatenarImpresionMultiple == null ? Boolean.FALSE : concatenarImpresionMultiple;
	}

	public void setConcatenarImpresionMultiple(Boolean concatenarImpresionMultiple) {
		if (this.getNroCopias() > 1){
			this.concatenarImpresionMultiple = Boolean.TRUE; 
		}
		else{
			this.concatenarImpresionMultiple = concatenarImpresionMultiple;
		}
	}

	public Boolean tieneNumerador(Empresa empresa){
		Boolean hayNumerador = Boolean.FALSE;
		if (this.getNumeradores() != null){
			for(Numerador num: this.getNumeradores()){
				if (Is.equal(num.getEmpresa(), empresa)){
					hayNumerador = Boolean.TRUE;
					break;
				}
			}
		}
		return hayNumerador;
	}
	
	public void generarCircuito(Map<String, Object> procesados, Collection<ConfiguracionCircuito> circuitos){
		Class<?> clase = ConfiguracionEntidad.buscarClase(this.getEntidad());
		try {
			Object instancia = clase.newInstance();
			Collection<Class<?>> trsdestino = new LinkedList<Class<?>>();
			clase.getMethod("tipoTrsDestino", Collection.class).invoke(instancia, trsdestino);
			
			for(Class<?> tipoTrDestino: trsdestino){
				String clave = this.getEntidad().concat(tipoTrDestino.getSimpleName());
				if (!procesados.containsKey(clave)){
					procesados.put(clave, null);
					
					ConfiguracionEntidad entidadDestino = ConfiguracionEntidad.buscarConfigurador(tipoTrDestino.getSimpleName());
					if (entidadDestino == null){
						entidadDestino = ConfiguracionEntidad.crearConfiguracionTransaccion(tipoTrDestino.getSimpleName());
					}					
					ConfiguracionCircuito circuito = ConfiguracionCircuito.buscarCircuito(this.getEntidad(), entidadDestino.getEntidad());
					if (circuito == null){
						circuito = new ConfiguracionCircuito();
						circuito.setOrigen(this);
						circuito.setDestino(entidadDestino);						
						XPersistence.getManager().persist(circuito);
						circuitos.add(circuito);
					}					
					entidadDestino.generarCircuito(procesados, circuitos);
				}
			}			
		} catch (Exception e) {
			throw new ValidationException(e.toString());
		}
		
	}

	public Boolean getEmpresaSoloLectura() {
		return empresaSoloLectura == null ? Boolean.TRUE : this.empresaSoloLectura;
	}

	public void setEmpresaSoloLectura(Boolean empresaSoloLectura) {
		this.empresaSoloLectura = empresaSoloLectura;
	}

	public String getCuerpoMensaje() {
		return cuerpoMensaje;
	}

	public void setCuerpoMensaje(String cuerpoMensaje) {
		this.cuerpoMensaje = cuerpoMensaje;
	}

	public Boolean getMonedaSoloLectura() {
		return monedaSoloLectura == null ? Boolean.TRUE : this.monedaSoloLectura;
	}

	public void setMonedaSoloLectura(Boolean monedaSoloLectura) {
		this.monedaSoloLectura = monedaSoloLectura;
	}
	
	public EstadoEntidad buscarEstado(String codigo){
		Query query = XPersistence.getManager().createQuery("from EstadoEntidad where codigo = :codigo and entidad.entidad = :entidad");
		query.setParameter("codigo", codigo);
		query.setParameter("entidad", this.getEntidad());
		try{
			return (EstadoEntidad)query.getSingleResult();
		}
		catch(Exception e){
			return null;
		}
	}

	public Boolean getMonedaPorCircuito() {
		return monedaPorCircuito == null ? Boolean.TRUE : monedaPorCircuito;
	}

	public void setMonedaPorCircuito(Boolean monedaPorCircuito) {
		if (monedaPorCircuito == null){
			this.monedaPorCircuito = Boolean.TRUE;
		}
		else{
			this.monedaPorCircuito = monedaPorCircuito;
		}
	}

	public Boolean getImpresionPorEmpresa() {
		return impresionPorEmpresa == null ? Boolean.FALSE : this.impresionPorEmpresa;
	}

	public void setImpresionPorEmpresa(Boolean impresionPorEmpresa) {
		this.impresionPorEmpresa = impresionPorEmpresa;
	}

	public Integer getNroCopias() {
		if (nroCopias == null){
			return 1;
		}
		else if (nroCopias <= 0){
			return 1;
		}
		else{
			return this.nroCopias;
		}
	}

	public void setNroCopias(Integer nroCopias) {
		if (nroCopias == null){
			this.nroCopias = 1;
		}
		else if (nroCopias <= 0){
			this.nroCopias = 1;
		}
		else{
			this.nroCopias = nroCopias;
		}
		
		if (this.nroCopias > 1){
			this.setConcatenarImpresionMultiple(Boolean.TRUE);
		}
	}
	
	public Boolean getRevierteAsientoFechaOrigen() {
		return revierteAsientoFechaOrigen == null ? Boolean.FALSE : this.revierteAsientoFechaOrigen;
	}

	public void setRevierteAsientoFechaOrigen(Boolean revierteAsientoFechaOrigen) {
		this.revierteAsientoFechaOrigen = revierteAsientoFechaOrigen;
	}
}
