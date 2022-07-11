package CotizadorTransportistasPortlet.commands;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.cotizadorModular.Bean.DocumentoResponse;
import com.tokio.cotizadorModular.Bean.IdCarpetaResponse;
import com.tokio.cotizadorModular.Bean.InfoCotizacion;
import com.tokio.cotizadorModular.Constants.CotizadorModularServiceKey;
import com.tokio.cotizadorModular.Interface.CotizadorGenerico;

import CotizadorTransportistasPortlet.constants.CotizadorTransportistasPortletKeys;

@Component(
		immediate = true,
		property = {
			"javax.portlet.name=" + CotizadorTransportistasPortletKeys.CotizadorTransportistas,
			"mvc.command.name=/CotizadorTransportes/generarSlipSemi"
		},
		service = MVCResourceCommand.class
	)

public class GenerarSlipSemiResourceCommand extends BaseMVCResourceCommand {
	
	private static final Log _log = LogFactoryUtil.getLog(GenerarSlipSemiResourceCommand.class);
	
	@Reference
	private DLAppService _dlAppService;
	
	@Reference
	CotizadorGenerico _CMServisGen;
	
	String errorServicios = "";
	
	InfoCotizacion infCot = null;

	@Override
	protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws Exception {
		
		Gson gson = new Gson();
		
		User user = (User) resourceRequest.getAttribute(WebKeys.USER);
		String p_usuario = user.getScreenName();
		
		generaInfoCot(resourceRequest);
		
		String auxiliarDoc = HtmlUtil.unescape(ParamUtil.getString(resourceRequest, "auxiliarDoc"));
		
		int totArc = ParamUtil.getInteger(resourceRequest, "totArc");
		
		JSONObject jsonObj;
		jsonObj = JSONFactoryUtil.createJSONObject(auxiliarDoc);
		UploadPortletRequest uploadRequest = PortalUtil.getUploadPortletRequest(resourceRequest);
		
		JsonObject jsonObject = new JsonObject();
		
		IdCarpetaResponse carpeta = _CMServisGen.SeleccionaIdCarpeta((int)infCot.getFolio(), (int)infCot.getCotizacion(),
				infCot.getVersion());
		
		for(int i = 0; i < totArc; i++) {
		
			String nombre = "file-" + i;
			File file = uploadRequest.getFile(nombre);
			String mimeType = uploadRequest.getContentType(nombre);
			int idCatalogoSlip = ParamUtil.getInteger(resourceRequest, "idCatalogoSlip");
			
			JsonArray listaDocumentos = new JsonArray();
			JsonObject enviaDocumentos = new JsonObject();
	
			JSONObject jsonObj2;
			jsonObj2 = JSONFactoryUtil.createJSONObject(jsonObj.getString("plantillaSlip-" + i));
	
			String nom = jsonObj2.getString("nom").replace(" ", "_");
	
			enviaDocumentos.addProperty("nombre", nom);
			enviaDocumentos.addProperty("extension", jsonObj2.getString("ext"));
			enviaDocumentos.addProperty("idCarpeta", carpeta.getIdCarpeta());
			enviaDocumentos.addProperty("idDocumento", "0");
			enviaDocumentos.addProperty("idCatalogoDetalle", idCatalogoSlip);
			
			Map<String, Object> info = null;
			
			info = guardaDocumentos(resourceRequest, file, nom, mimeType, jsonObj2.getString("ext"));
			
			enviaDocumentos.addProperty("documento", "");
			enviaDocumentos.addProperty("url", (String) info.get("url"));
			enviaDocumentos.addProperty("leer", 1);		
			
			listaDocumentos.add(enviaDocumentos);
			
			DocumentoResponse respuesta =
					_CMServisGen.wsDocumentos(0, CotizadorModularServiceKey.TMX_CTE_TRANSACCION_POST,
							listaDocumentos, CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS,
							"SLIP_CARGO", (int) infCot.getCotizacion(), "", p_usuario, infCot.getPantalla());
			
			if(respuesta.getMsg().equalsIgnoreCase("ok")) {
				jsonObject.addProperty("code", 0);
				jsonObject.addProperty("msg", "El documento fue agregado con éxito");
				elimianArchivo((long) info.get("idDoc"));
			}
			else {
				jsonObject.addProperty("code", 2);
				jsonObject.addProperty("msg", "Hubo un error al cargar el documento");
			}
		
		}	
		
		String responseString = gson.toJson(jsonObject);
		
		PrintWriter writer = resourceResponse.getWriter();
		writer.write(responseString);
	}
	
	private void generaInfoCot(ResourceRequest resourceRequest){
		
		Gson gson = new Gson();
		
		String infoCot = ParamUtil.getString(resourceRequest, "infoCot");
		System.out.println("infoCot String :" + infoCot);
		infCot = gson.fromJson(infoCot, InfoCotizacion.class);
		System.out.println("infCot objeto: " + infCot);
		
	}
	
	Map<String, Object> guardaDocumentos(ResourceRequest resourceRequest, File file, String nombre, String mimeType, String ext){
		try {
			
			Map<String, Object> respuesta = new HashMap<String, Object>();

			long idGroup = PortalUtil.getScopeGroupId(resourceRequest);
			ServiceContext serviceContext = ServiceContextFactory.getInstance(DLFolder.class.getName(), resourceRequest);
			
			serviceContext.setAddGroupPermissions(true);
			serviceContext.setAddGuestPermissions(true);
			
			
			User user = (User) resourceRequest.getAttribute(WebKeys.USER);
			
			String cotizacion = ParamUtil.getString(resourceRequest, "cotizacion");
			String version = ParamUtil.getString(resourceRequest, "version");
			String folio = ParamUtil.getString(resourceRequest, "folio");
			String url = ParamUtil.getString(resourceRequest, "url2");
//			

			
			String aux2 = user.getScreenName() + "-" + nombre + "-F_" + folio + "-C_" + cotizacion + "-V_" + version;
			
			DLFolder fCotizadores = DLFolderLocalServiceUtil.getFolder(idGroup, DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
					"Documentos_Cotizadores");
			
			
			FileEntry fileEntry = _dlAppService.addFileEntry(idGroup, fCotizadores.getFolderId(), nombre + "." + ext,
					mimeType, nombre + "." + ext, aux2, "hi", file, serviceContext);
			
			
			String urlDoc = url + "/documents/" + idGroup + "/" + fileEntry.getFolderId() + "/" + fileEntry.getFileName()
			+ "/" + fileEntry.getUuid();
			
			System.out.println(urlDoc);
			
			respuesta.put("url", urlDoc); 
			respuesta.put("idDoc", fileEntry.getFileEntryId()); 
			return respuesta;
		} catch (PortalException e) {
			// TODO Auto-generated catch block
			agregaErrorString(" - Error al almacenar los documentos");
			_log.error(e.getMessage());
			e.printStackTrace();
			return null;
		}
		
	}
	
	void elimianArchivo(long idDoc){
		try {
			_dlAppService.deleteFileEntry(idDoc);
		} catch (PortalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			_log.error(e.getMessage());
		}
	}
	
	void agregaErrorString(String msgErr){
		if(Validator.isNull(errorServicios)){
			errorServicios = msgErr;
		}else{
			errorServicios += ",<br>" +  errorServicios;
		}
	}

}
