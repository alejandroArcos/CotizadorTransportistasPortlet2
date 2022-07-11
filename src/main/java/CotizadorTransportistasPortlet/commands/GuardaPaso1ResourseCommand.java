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
import com.tokio.cotizadorModular.Bean.InfoCotizacion;
import com.tokio.cotizadorModular.Bean.SimpleResponse;
import com.tokio.cotizadorModular.Interface.CotizadorPaso1Transportes;

import CotizadorTransportistasPortlet.bean.DatosGenerales;
import CotizadorTransportistasPortlet.bean.Paso1;
import CotizadorTransportistasPortlet.constants.CotizadorTransportistasPortletKeys;

@Component(
		 property = {
		 "javax.portlet.name="+ CotizadorTransportistasPortletKeys.CotizadorTransportistas,
		 "mvc.command.name=/CotizadorTransportistas/GuardaPaso1"
		 },
		 service = MVCResourceCommand.class
		 )

public class GuardaPaso1ResourseCommand extends BaseMVCResourceCommand{
	
//	@Reference
//	CotizadorPaso1 _CMServicesP1;
	@Reference
	CotizadorPaso1Transportes _CMPaso1Transportes;

	InfoCotizacion infCot;

	@Override
	protected void doServeResource(ResourceRequest resourceRequest,
			ResourceResponse resourceResponse) throws Exception {
		// TODO Auto-generated method stub
		
		
		Gson gson = new Gson();
		HttpServletRequest originalRequest = PortalUtil
				.getOriginalServletRequest(PortalUtil.getHttpServletRequest(resourceRequest));
		
		String datos = ParamUtil.getString(resourceRequest, "datos");
		String infoCot = ParamUtil.getString(resourceRequest, "infoCot");
		
		DatosGenerales dg = gson.fromJson(datos, DatosGenerales.class);
		infCot = gson.fromJson(infoCot, InfoCotizacion.class);
		
		System.out.println("DatosGenerales:" +dg);
		System.out.println("InfoCotizacion:" + infCot);
		
		User user = (User) resourceRequest.getAttribute(WebKeys.USER);
		String usuario = user.getScreenName();
		int idPerfilUser = (int) originalRequest.getSession().getAttribute("idPerfil");
		
		int modo = (dg.getModo().getModoCotizacion() >= 3) ? 1
				: dg.getModo().getModoCotizacion();
		
		SimpleResponse abc = _CMPaso1Transportes.guardarCotizacionTransportes(dg.getTipomov(), idPerfilUser, dg.getFecinicio(), dg.getFecfin(), dg.getMoneda(), dg.getFormapago(), dg.getVigencia(),
			dg.getAgente(), dg.getIdPersona(), dg.getTipoPer(), dg.getRfc(), dg.getNombre(), dg.getAppPaterno(), dg.getAppMaterno(), dg.getIdDenominacion(), dg.getCodigo(),
			dg.getExtranjero(), dg.getCanalN(), modo, dg.getCotizacion(), dg.getVersion(), dg.getGiro(), dg.getFolio(), dg.getTipoCoaseguro(), usuario, dg.getPantalla());
		
		
		Paso1 paso1Object = new Paso1();
		paso1Object.setDac_coaseguro( ParamUtil.getString(resourceRequest, "coaseguro"));
		
		
		String jsonString = gson.toJson(abc);
		PrintWriter writer = resourceResponse.getWriter();
		writer.write(jsonString);

	}
	
	
}
