package CotizadorTransportistasPortlet.commands;

import java.util.Comparator;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.cotizadorModular.Bean.CaratulaResponse;
import com.tokio.cotizadorModular.Bean.InfoCotizacion;
import com.tokio.cotizadorModular.Bean.ListaRegistro;
import com.tokio.cotizadorModular.Bean.Registro;
import com.tokio.cotizadorModular.Bean.RutasResponse;
import com.tokio.cotizadorModular.Constants.CotizadorModularServiceKey;
import com.tokio.cotizadorModular.Enum.ModoCotizacion;
import com.tokio.cotizadorModular.Interface.CotizadorGenerico;
import com.tokio.cotizadorModular.Interface.CotizadorPaso2Transportes;
import com.tokio.cotizadorModular.Interface.CotizadorPaso3;
import com.tokio.cotizadorModular.Interface.CotizadorPaso3Transportes;
import com.tokio.cotizadorModular.Util.CotizadorModularUtil;

import CotizadorTransportistasPortlet.constants.CotizadorTransportistasPortletKeys;

@Component(
		 property = {
		 "javax.portlet.name="+ CotizadorTransportistasPortletKeys.CotizadorTransportistas,
		 "mvc.command.name=/CotizadorTransportistas/SavePaso2"
		 },
		 service = MVCActionCommand.class
		 )

public class SavePaso2ActionCommand extends BaseMVCActionCommand {
	
	@Reference
	CotizadorPaso2Transportes _CMPaso2Transportes;
	
	@Reference
	CotizadorPaso3 _ServicePaso3;
	
	@Reference
	CotizadorPaso3Transportes _ServiceP3Transportes;
	
	@Reference
	CotizadorGenerico _ServiceGenerico;
	
	User user;
	int idPerfilUser;
	
	InfoCotizacion infCotizacion = new InfoCotizacion();
	CaratulaResponse caratulaResponse = new CaratulaResponse();
	
	protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
		
		HttpServletRequest originalRequest = PortalUtil
				.getOriginalServletRequest(PortalUtil.getHttpServletRequest(actionRequest));
		
		user = (User) actionRequest.getAttribute(WebKeys.USER);
		idPerfilUser = (int) originalRequest.getSession().getAttribute("idPerfil");
		
		String paso = ParamUtil.getString(actionRequest, "paso");
		String folioCoti = ParamUtil.getString(actionRequest, "folioCoti");
		String versionCoti = ParamUtil.getString(actionRequest, "versionCoti");
		String cotizacion = ParamUtil.getString(actionRequest, "cotizacion");
		
		/*
		infCotizacion.setFolio(Long.parseLong(folioCoti));
		infCotizacion.setVersion(Integer.parseInt(versionCoti));
		infCotizacion.setCotizacion(Long.parseLong(cotizacion));
		*/
		
		llenaInfoCotizacion(actionRequest);
		llenaCatalogos(actionRequest);
		validaModoCotizacion(actionRequest);
		
		String infoCotJson = CotizadorModularUtil.objtoJson(infCotizacion);
		
		actionRequest.setAttribute("paso", paso );
		actionRequest.setAttribute("folioCoti", folioCoti );
		actionRequest.setAttribute("versionCoti", versionCoti );
		actionRequest.setAttribute("cotizacion", cotizacion );
		actionRequest.setAttribute("infoCotJson", infoCotJson);
		actionRequest.setAttribute("infCotizacion", infCotizacion);
		actionRequest.setAttribute("caratulaResponse", caratulaResponse);
		
		actionRequest.setAttribute("perfilSuscriptor", perfilSuscriptor());
		actionRequest.setAttribute("perfilJapones", perfilJapones());
		actionRequest.setAttribute("numeroRutas", getNumeroRutas());
		
		actionResponse.setRenderParameter("jspPage", "/pantallas/paso3.jsp");
	}
	
	private int getNumeroRutas() {
		
		try {
		
			RutasResponse rutas = _CMPaso2Transportes.consultaRuta("" + infCotizacion.getFolio(), (int)infCotizacion.getCotizacion(), (int) infCotizacion.getVersion(),
					infCotizacion.getPantalla(), user.getScreenName());
			
			if(rutas.getCode() == 0) {
				return rutas.getLista().size();
			}
			else {
				return 0;
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	private void llenaInfoCotizacion(ActionRequest actionRequest) {

		try {
			/*
			HttpServletRequest originalRequest = PortalUtil
					.getOriginalServletRequest(PortalUtil.getHttpServletRequest(actionRequest));
			*/
			String inf = ParamUtil.getString(actionRequest, "infoCotizacion");
			System.out.println(inf);

			String nombreCotizador = "";
			if (Validator.isNotNull(inf)) {
				infCotizacion = CotizadorModularUtil.decodeURL(inf);
			} else {
				infCotizacion = new InfoCotizacion();
			}
			
			
//			auxRenovacion();

			System.out.println("-----------------------------------------");
			System.err.println("inf: " + infCotizacion.toString());

			nombreCotizador = "Cotizador Paquete Empresarial";
			actionRequest.setAttribute("tituloCotizador", nombreCotizador);
			
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println("------------------ llenaInfoCotizacion:");
			SessionErrors.add(actionRequest, "errorServicios");
			e.printStackTrace();
		}

	}
	
	private void llenaCatalogos(ActionRequest actionRequest){

		ListaRegistro listaMotivoRechazo = fGetCatalogos(
				CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
				CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
				CotizadorModularServiceKey.LIST_CAT_MOTI_RECHAZO,
				CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
				infCotizacion.getPantalla());
		
		ListaRegistro listaPrimaDeposito = fGetCatalogos(
				CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
				CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
				CotizadorModularServiceKey.LIST_CAT_PRIMA,
				CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
				infCotizacion.getPantalla());
		
		ListaRegistro listaCriterio = fGetCatalogos(
				CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
				CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
				CotizadorModularServiceKey.LIST_CAT_CRIT,
				CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
				infCotizacion.getPantalla());
		
		ListaRegistro listaUnidades = fGetCatalogos(
				CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
				CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
				CotizadorModularServiceKey.LIST_CAT_UNI,
				CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
				infCotizacion.getPantalla());

		actionRequest.setAttribute("motivoRechazo", listaMotivoRechazo.getLista());
		actionRequest.setAttribute("primaDeposito", listaPrimaDeposito.getLista());
		actionRequest.setAttribute("listaCriterio", listaCriterio.getLista());
		actionRequest.setAttribute("listaUnidades", listaUnidades.getLista());
		
		Gson gson = new Gson();
		String jsonString = gson.toJson(listaUnidades, ListaRegistro.class);
		JsonObject jsonAux = gson.fromJson(jsonString, JsonObject.class);
		
		actionRequest.setAttribute("catalogoUnidades", jsonAux);
	}
	
	
	private ListaRegistro fGetCatalogos(int p_rownum, String p_tiptransaccion, String p_codigo,
			int p_activo, String p_usuario, String p_pantalla) {
		try {
			ListaRegistro list = _ServiceGenerico.getCatalogo(p_rownum, p_tiptransaccion, p_codigo,
					p_activo, p_usuario, p_pantalla);
			list.getLista().sort(Comparator.comparing(Registro::getDescripcion));
			return list;
			/* return null; */
		} catch (Exception e) {
			return null;
		}
	}
	
	private void fGetCaratula(ActionRequest actionRequest) {
		try {
			String cur_version = String.valueOf(infCotizacion.getVersion());
			caratulaResponse = _ServiceP3Transportes.getCaratula((int) infCotizacion.getCotizacion(),
					cur_version, user.getScreenName(), CotizadorTransportistasPortletKeys.PANTALLA);
			// if caratulaResponse.getPrimaNeta()
		} catch (Exception e) {
			// TODO: handle exception
			caratulaResponse = new CaratulaResponse();
		}
	}
	
	private void fGetCalculoTransportes(ActionRequest actionRequest) {
		try {
			CaratulaResponse response = _ServiceP3Transportes.getCalculoTransportes(
					(int) infCotizacion.getCotizacion(), infCotizacion.getVersion(), user.getScreenName(),
					CotizadorTransportistasPortletKeys.PANTALLA);
			
			if(response.getCode() == 0) {
				fGetCaratula(actionRequest);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void validaModoCotizacion(ActionRequest actionRequest) {
		switch (infCotizacion.getModo()) {
			case FACTURA_492:
				actionRequest.setAttribute("Leg492", "factura");
				fGetCaratula(actionRequest);
				break;
			case ALTA_ENDOSO:
				actionRequest.setAttribute("dBtns", "d-none");
				fGetCaratula(actionRequest);
				break;
			case EDITAR_ALTA_ENDOSO:
				actionRequest.setAttribute("dBtns", "d-none");
				fGetCaratula(actionRequest);
				break;
			case BAJA_ENDOSO:
				actionRequest.setAttribute("dBtns", "d-none");
				//caratulaBajaEnsos(actionRequest);
				break;
			case EDITAR_BAJA_ENDOSO:
				actionRequest.setAttribute("dBtns", "d-none");
				//caratulaBajaEnsos(actionRequest);
				break;
			case RENOVACION_AUTOMATICA:
				actionRequest.setAttribute("dBtns", "d-none");
				//actualizaInfoRenovacion();
				//recuperaInfoPaso1(actionRequest);
				fGetCaratula(actionRequest);
				break;
			case EDITAR_RENOVACION_AUTOMATICA:
				actionRequest.setAttribute("dBtns", "d-none");
				infCotizacion.setModo(ModoCotizacion.RENOVACION_AUTOMATICA);
				fGetCaratula(actionRequest);
			case CONSULTAR_RENOVACION_AUTOMATICA:
				actionRequest.setAttribute("dBtns", "d-none");
				fGetCaratula(actionRequest);
			case CONSULTA:
				fGetCaratula(actionRequest);
				break;
			case NUEVA:
			case EDICION:
			case EDICION_JAPONES:
				fGetCalculoTransportes(actionRequest);		
				break;
			default:
				fGetCaratula(actionRequest);
				break;
		}
			
	}
	
	private int perfilJapones() {
		try {
			switch (idPerfilUser) {
				case CotizadorTransportistasPortletKeys.PERFIL_JAPONES:
					return 1;
			}
			return 0;
		} catch (Exception e) {
			return 0;
		}
	}
	
	private int perfilSuscriptor() {
		try {
			switch (idPerfilUser) {
				case CotizadorTransportistasPortletKeys.PERFIL_SUSCRIPTORJR:
					return 1;
				case CotizadorTransportistasPortletKeys.PERFIL_SUSCRIPTORSR:
					return 1;
				case CotizadorTransportistasPortletKeys.PERFIL_SUSCRIPTORMR:
					return 1;
			}
			return 0;
		} catch (Exception e) {
			return 0;
		}
	}
}
