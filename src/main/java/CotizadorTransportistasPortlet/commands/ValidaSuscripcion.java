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
import com.tokio.cotizadorModular.Bean.SimpleResponse;
import com.tokio.cotizadorModular.Interface.CotizadorPaso2Transportes;

import CotizadorTransportistasPortlet.constants.CotizadorTransportistasPortletKeys;

@Component(
		immediate = true,
		property = {
			"javax.portlet.name=" + CotizadorTransportistasPortletKeys.CotizadorTransportistas,
			"mvc.command.name=/cotizadorTransportistas/validaSuscripcion"
		},
		service = MVCResourceCommand.class
	)

public class ValidaSuscripcion extends BaseMVCResourceCommand{
	@Reference
	CotizadorPaso2Transportes _CMPaso2Transportes;
	
	@Override
	protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws Exception {
		Gson gson = new Gson();
		PrintWriter writer = resourceResponse.getWriter();
		User user = (User) resourceRequest.getAttribute(WebKeys.USER);
		HttpServletRequest originalRequest = PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(resourceRequest));
		
		int cotizacion = ParamUtil.getInteger(resourceRequest, "cotizacion");
		int version = ParamUtil.getInteger(resourceRequest, "version");
		int idPerfil = (int) originalRequest.getSession().getAttribute("idPerfil");
		int canalNegocio = ParamUtil.getInteger(resourceRequest, "canalNegocio");
		String usuario = user.getScreenName();
		
		
		System.err.println("cotizacion: " + cotizacion);
		System.err.println("version: " + version);
		System.err.println("idPerfil: " + idPerfil);
		System.err.println("canalNegocio: " + canalNegocio);
		System.err.println("usuario: " + usuario);

		SimpleResponse response = _CMPaso2Transportes.validaSuscripcionTransportes(cotizacion, version, idPerfil, canalNegocio, usuario, CotizadorTransportistasPortletKeys.CotizadorTransportistas);
		
		String responseString = gson.toJson(response);
		writer.write(responseString);
	}
}
