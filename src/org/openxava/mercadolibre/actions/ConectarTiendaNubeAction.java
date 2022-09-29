package org.openxava.mercadolibre.actions;

import org.openxava.actions.ViewBaseAction;
import org.openxava.validators.ValidationException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tiendanube.base.ApiClient;
import com.tiendanube.base.ApiCredentials;
import com.tiendanube.base.InternalApiResponse;

public class ConectarTiendaNubeAction extends ViewBaseAction{

static final String AUTHORIZE = "https://www.tiendanube.com/apps/1970/authorize";
	
	@Override
	public void execute() throws Exception { //si es editable la 
		String appId = getView().getValueString("appId");
		String appSecret = getView().getValueString("secretKey");;
		String code = getView().getValueString("codeToken");
		if(appId == null){
			throw new ValidationException("Primero debe poner el app id de la aplicacion de tienda nube");
		}
		if(appSecret == null){
			throw new ValidationException("Primero debe poner el secret key de la aplicacion de tienda nube");
		}
		if(code.isEmpty()){
			throw new ValidationException("Debe copiar el code que trae la pagina: "+AUTHORIZE+" RECUERDE QUE TIENE 30 SEGUNDOS");
		}
		ApiCredentials credentials = ApiCredentials.prepareCredentials(appId, appSecret);
		ApiClient apiclient = new ApiClient(credentials,"info", "info@clouderp.com.ar");
		InternalApiResponse response = apiclient.authenticate(code);
		JsonObject json = null;
		json = new Gson().fromJson(response.getResponse(),JsonObject.class);
		getView().setValue("storeID", json.get("user_id").getAsString());
		getView().setValue("accessToken", json.get("access_token").getAsString());
		addMessage("La conexion fue exitosa, se pudo generar un acces token y el store id, recuerde grabar los cambios");
	}  	
}
