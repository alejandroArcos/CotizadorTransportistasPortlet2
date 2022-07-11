package CotizadorTransportistasPortlet.commands;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;

import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;

import CotizadorTransportistasPortlet.constants.CotizadorTransportistasPortletKeys;

@Component(
		 property = {
		 "javax.portlet.name="+ CotizadorTransportistasPortletKeys.CotizadorTransportistas,
		 "mvc.command.name=/RC/backP1"
		 },
		 service = MVCResourceCommand.class
		 )

public class GetCotizadorDataResourceCommand extends BaseMVCResourceCommand{
	@Override
	protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws Exception {
		System.err.println("RESOURCE BACK PASO 1");
	}
}
