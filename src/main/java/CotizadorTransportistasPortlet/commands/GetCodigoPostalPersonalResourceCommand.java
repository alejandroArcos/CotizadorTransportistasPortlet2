package CotizadorTransportistasPortlet.commands;

import com.google.gson.Gson;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.tokio.cotizadorModular.Bean.DomicilioResponse;
import com.tokio.cotizadorModular.Exception.CotizadorModularException;
import com.tokio.cotizadorModular.Interface.CotizadorPaso4;

import java.io.PrintWriter;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import CotizadorTransportistasPortlet.constants.CotizadorTransportistasPortletKeys;

@Component(immediate = true, property = { "javax.portlet.name=" + CotizadorTransportistasPortletKeys.CotizadorTransportistas,
"mvc.command.name=/getDomicilioPersonasUrl" }, service = MVCResourceCommand.class)

public class GetCodigoPostalPersonalResourceCommand extends BaseMVCResourceCommand{
	@Reference
	CotizadorPaso4 _ServicePaso4;
	
	@Override
    protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws Exception {
		String cp = ParamUtil.getString(resourceRequest, "cp");
        if (cp != null && !cp.isEmpty()) {
            try {
                DomicilioResponse cpresp = _ServicePaso4.getDomicilioPersonas(cp);
                Gson gson = new Gson();
                String jsonString = gson.toJson(cpresp);
                PrintWriter writer = resourceResponse.getWriter();
                writer.write(jsonString);
            } catch (CotizadorModularException e) {
                // TODO Auto-generated catch block
                PrintWriter writer = resourceResponse.getWriter();
                String jsonString = "{\"code\" : \"5\", \"msg\" : \"Error al consultar la información\" }";
                writer.write(jsonString);
            }
        } else {
            PrintWriter writer = resourceResponse.getWriter();
            String jsonString = "{\"code\" : \"5\", \"msg\" : \"Error al consultar la información\" }";
            writer.write(jsonString);
        }
	}
}
