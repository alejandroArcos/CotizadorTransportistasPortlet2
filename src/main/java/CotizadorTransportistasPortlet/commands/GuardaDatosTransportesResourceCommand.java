package CotizadorTransportistasPortlet.commands;

import java.io.PrintWriter;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.Gson;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.cotizadorModular.Bean.CoberturasAdicionalesTransportes;
import com.tokio.cotizadorModular.Bean.CondicionesAseguramiento;
import com.tokio.cotizadorModular.Bean.DatosTransportesResponse;
import com.tokio.cotizadorModular.Bean.LimitesGenerales;
import com.tokio.cotizadorModular.Interface.CotizadorPaso2Transportes;

import CotizadorTransportistasPortlet.constants.CotizadorTransportistasPortletKeys;

@Component(
		 property = {
		 "javax.portlet.name="+ CotizadorTransportistasPortletKeys.CotizadorTransportistas,
		 "mvc.command.name=/cotizadorTransportistas/GuardaDatosPaso2"
		 },
		 service = MVCResourceCommand.class
		 )

public class GuardaDatosTransportesResourceCommand extends BaseMVCResourceCommand {

	@Reference
	CotizadorPaso2Transportes _CMPaso2Transportes;
	
	@Override
	protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws Exception {
		
		HttpServletRequest originalRequest = PortalUtil
				.getOriginalServletRequest(PortalUtil.getHttpServletRequest(resourceRequest));
		
		Gson gson = new Gson();
		
		User user = (User) resourceRequest.getAttribute(WebKeys.USER);
		int idPerfilUser = (int) originalRequest.getSession().getAttribute("idPerfil");
		
		String condicionesAseguramiento = ParamUtil.getString(resourceRequest, "condicionesAseguramiento");
		String limitesGenerales = ParamUtil.getString(resourceRequest, "limitesGenerales");
		String coberturasAdicionales = ParamUtil.getString(resourceRequest, "coberturasAdicionales");
		int cotizacion = ParamUtil.getInteger(resourceRequest, "cotizacion");
		String folio = ParamUtil.getString(resourceRequest, "folio");
		int version = ParamUtil.getInteger(resourceRequest, "version");
		String pantalla = ParamUtil.getString(resourceRequest, "pantalla");
		
		CondicionesAseguramiento condicionesAseg = gson.fromJson(condicionesAseguramiento, CondicionesAseguramiento.class);
		LimitesGenerales limitesGral = gson.fromJson(limitesGenerales, LimitesGenerales.class);
		CoberturasAdicionalesTransportes coberturasAdic = gson.fromJson(coberturasAdicionales, CoberturasAdicionalesTransportes.class);
		
		DatosTransportesResponse dtResponse = _CMPaso2Transportes.guardaDatosRyMTransportes(folio, idPerfilUser, cotizacion, 
				version, pantalla, user.getScreenName(), condicionesAseg, limitesGral, coberturasAdic);
		
		String jsonStringResponse = gson.toJson(dtResponse);
		
		PrintWriter writer = resourceResponse.getWriter();
		writer.write(jsonStringResponse);
		
	}

}
