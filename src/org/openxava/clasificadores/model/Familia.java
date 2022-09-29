package org.openxava.clasificadores.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;

@Entity

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})

public class Familia extends ObjetoEstatico{

}
