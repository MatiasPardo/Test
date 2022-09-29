package org.openxava.contabilidad.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;

@Entity

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})

public class AsientoPlantilla extends ObjetoEstatico{
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate
	@NoModify
	private TipoAsiento tipoAsiento;
	
	@Column(length=100)
	private String detalle;
	
	@OneToMany(mappedBy="asientoPlantilla", cascade=CascadeType.ALL) 
	@ListProperties("cuenta.codigo, cuenta.nombre, detalle, debe, haber")
	private Collection<ItemAsientoPlantilla> items = new ArrayList<ItemAsientoPlantilla>();

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

	public Collection<ItemAsientoPlantilla> getItems() {
		return items;
	}

	public void setItems(Collection<ItemAsientoPlantilla> items) {
		this.items = items;
	}
	
	public Asiento crearAsiento(){
		Asiento asiento = new Asiento();
		asiento.copiarPropiedades(this);
		XPersistence.getManager().persist(asiento);		
		Collection<ItemAsiento> itemsAsiento = new ArrayList<ItemAsiento>();
		asiento.setItems(itemsAsiento);
		for(ItemAsientoPlantilla item: this.getItems()){
			ItemAsiento itemAsiento = new ItemAsiento();
			itemAsiento.setAsiento(asiento);
			itemAsiento.copiarPropiedades(item);
			itemsAsiento.add(itemAsiento);
			XPersistence.getManager().persist(itemAsiento);
		}
		return asiento;
	}
}
