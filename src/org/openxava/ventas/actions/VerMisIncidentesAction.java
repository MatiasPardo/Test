package org.openxava.ventas.actions;

import org.openxava.actions.TabBaseAction;
import org.openxava.base.model.BaseDatosERP;
import org.openxava.util.Users;

public class VerMisIncidentesAction extends TabBaseAction {
	
	private boolean todos = false;
	
	public boolean isTodos() {
		return todos;
	}

	public void setTodos(boolean todos) {
		this.todos = todos;
	}

	@Override
	public void execute() throws Exception {
		if (this.isTodos()){
			this.getTab().setBaseCondition(null);
			
			this.addMessage("TODOS");
		}
		else{
			StringBuilder condition = new StringBuilder();
			condition.append("${estado} in (0, 4) and (");
			String usuario = Users.getCurrent();
			condition.append("${asignado.usuarioSistema.name} = ").append(BaseDatosERP.valorString(usuario));
			condition.append(" or ");
			condition.append("${responsable.usuarioSistema.name} = ").append(BaseDatosERP.valorString(usuario));
			condition.append(")");
			
			this.getTab().setBaseCondition(condition.toString());
			
			this.addMessage("MIS PENDIENTES");
		}
	}
}
