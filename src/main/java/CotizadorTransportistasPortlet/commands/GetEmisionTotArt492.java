package CotizadorTransportistasPortlet.commands;

import com.google.gson.Gson;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.cotizadorModular.Bean.EmisionDataRequest;
import com.tokio.cotizadorModular.Bean.EmisionDataResponse;
import com.tokio.cotizadorModular.Bean.SolicitarEmisionResponse;
import com.tokio.cotizadorModular.Interface.CotizadorPaso3;

import java.io.PrintWriter;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import CotizadorTransportistasPortlet.constants.CotizadorTransportistasPortletKeys;

@Component(immediate = true, property = { 
		"javax.portlet.name=" + CotizadorTransportistasPortletKeys.CotizadorTransportistas,
		"mvc.command.name=/emisionArt492/getemision" 
		}, service = MVCResourceCommand.class)

public class GetEmisionTotArt492 extends BaseMVCResourceCommand{
	@Reference
	CotizadorPaso3 _ServicePaso3;
	
	@Override
	protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws Exception {
		PrintWriter writer = resourceResponse.getWriter();
		System.err.println("ENTRE GET EMISION DATA");
		
		try {
			Integer cotizacion = ParamUtil.getInteger(resourceRequest, "cotizacion");
			Integer version = ParamUtil.getInteger(resourceRequest, "version");
			Integer factura = ParamUtil.getInteger(resourceRequest, "factura");
			User user = (User) resourceRequest.getAttribute(WebKeys.USER);
			String usuario = user.getScreenName();
			String pantalla = CotizadorTransportistasPortletKeys.CotizadorTransportistas;

			EmisionDataResponse em1 = _ServicePaso3.getEmisionData(cotizacion, version, usuario, pantalla);
			int pnc = Validator.isNumber(em1.getPaisNaciminetoCodigo()) ?  Integer.parseInt(em1.getPaisNaciminetoCodigo()) : 0;

			EmisionDataRequest em2 = new EmisionDataRequest(
					cotizacion,
					version, 
					em1.getViculoPersona(),
					em1.getCalle(), em1.getNumero(), pnc, em1.getPaisNacimineto(),em1.getIdGiroMercantil(),
					em1.getDatosCliente(), em1.getCpData(), em1.getDatosFisica(),
					em1.getDatosMoral(), em1.getP_datosPep(), factura,
					usuario, pantalla, "", "", 2);
			
			System.err.println("em2: ");
			System.err.println(em2);
			
			SolicitarEmisionResponse soliEmi = _ServicePaso3.solicitarEmision(em2);
			Gson gson = new Gson();
			String jsonString = gson.toJson(soliEmi);
			writer.write(jsonString);
		} catch (Exception e) {
			// TODO: handle exception
			String jsonString = "{\"code\" : \"5\", \"msg\" : \"Error al consultar la información\" }";
			writer.write(jsonString);
		}
	}
}
