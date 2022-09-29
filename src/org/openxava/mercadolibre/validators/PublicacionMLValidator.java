package org.openxava.mercadolibre.validators;

import java.util.List;

import javax.persistence.FlushModeType;
import javax.persistence.Query;

import org.openxava.jpa.XPersistence;
import org.openxava.mercadolibre.model.EstadoPublicacionML;
import org.openxava.util.Is;
import org.openxava.util.Messages;
import org.openxava.validators.IValidator;
import org.openxava.ventas.model.Producto;

@SuppressWarnings("serial")
public class PublicacionMLValidator implements IValidator{

	private String idMercadoLibre;
	
	private String idProducto;
	
	private Producto producto;
	
	private EstadoPublicacionML estado;
	
	private String id;
	
	@Override
	public void validate(Messages errors) throws Exception {
		if (Is.equal(this.getEstado(), EstadoPublicacionML.Publicada)){
			if (!Is.emptyString(this.getIdMercadoLibre()) && this.getProducto() != null){
				StringBuilder sql = new StringBuilder();
				sql.append("from PublicacionML where idMercadoLibre = :idMercadoLibre");
				if (!Is.emptyString(this.getId())){
					sql.append(" and id != :id");
				}
				if (!Is.emptyString(this.getIdProducto())){
					sql.append(" and idProducto = :idProducto");
				}
				Query query = XPersistence.getManager().createQuery(sql.toString());
				query.setParameter("idMercadoLibre", this.getIdMercadoLibre());
				query.setMaxResults(1);
				query.setFlushMode(FlushModeType.COMMIT);
				if (!Is.emptyString(this.getId())){
					query.setParameter("id", this.getId());
				}
				if (!Is.emptyString(this.getIdProducto())){
					query.setParameter("idProducto", this.getIdProducto());
				}
				List<?> results = query.getResultList(); 
				if (!results.isEmpty()){
					if(this.getIdProducto() != null){
						errors.add("Ya existe una publicación con id Mercado Libre: " + this.getIdMercadoLibre() + " y id producto: "  
										 + this.getIdProducto());
					}else{
						errors.add("Ya existe una publicación de Mercado Libre " + this.getIdMercadoLibre());
					}
				}
			}
		}
	}

	public String getIdMercadoLibre() {
		return idMercadoLibre;
	}

	public void setIdMercadoLibre(String idMercadoLibre) {
		this.idMercadoLibre = idMercadoLibre;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public EstadoPublicacionML getEstado() {
		return estado;
	}

	public void setEstado(EstadoPublicacionML estado) {
		this.estado = estado;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIdProducto() {
		return idProducto;
	}

	public void setIdProducto(String idProducto) {
		this.idProducto = idProducto;
	}
	
	
}
