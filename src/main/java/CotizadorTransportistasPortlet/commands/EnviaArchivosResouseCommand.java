package CotizadorTransportistasPortlet.commands;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.apache.commons.io.FileUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.cotizadorModular.Bean.DocumentoResponse;
import com.tokio.cotizadorModular.Bean.IdCarpetaResponse;
import com.tokio.cotizadorModular.Constants.CotizadorModularServiceKey;
import com.tokio.cotizadorModular.Exception.CotizadorModularException;
import com.tokio.cotizadorModular.Interface.CotizadorGenerico;

import CotizadorTransportistasPortlet.constants.CotizadorTransportistasPortletKeys;

@Component(immediate = true, property = { "javax.portlet.name=" + CotizadorTransportistasPortletKeys.CotizadorTransportistas,
"mvc.command.name=/cotizadorTransportistas/enviaArchivos" }, service = MVCResourceCommand.class)

public class EnviaArchivosResouseCommand extends BaseMVCResourceCommand{
	@Reference
	CotizadorGenerico _ServiceGenerico;
	@Reference
	private DLAppService _dlAppService;
	
	private static final Log _log = LogFactoryUtil.getLog(EnviaArchivosResouseCommand.class);
	
	@Override
	protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws Exception {
		enviaArchivos(resourceRequest);
	}
	
	void enviaArchivos(ResourceRequest resourceRequest) {

		int cotizacion = ParamUtil.getInteger(resourceRequest, "cotizacion");
		int folio = ParamUtil.getInteger(resourceRequest, "folio");
		int version = ParamUtil.getInteger(resourceRequest, "version");
		String auxiliarDoc = HtmlUtil.unescape(ParamUtil.getString(resourceRequest, "auxiliarDoc"));
		int totArchivos = ParamUtil.getInteger(resourceRequest, "totArchivos");

		User user = (User) resourceRequest.getAttribute(WebKeys.USER);
		String p_usuario = user.getScreenName();
		System.out.println("-------> totArchivos : " + totArchivos);
		try {
			if (totArchivos > 0) {
				
				JSONObject jsonObj;
				jsonObj = JSONFactoryUtil.createJSONObject(auxiliarDoc);
				UploadPortletRequest uploadRequest = PortalUtil.getUploadPortletRequest(resourceRequest);

				System.out.println(jsonObj.toString());

				IdCarpetaResponse carpeta = _ServiceGenerico.SeleccionaIdCarpeta(folio, cotizacion, version);
				

				for (int i = 0; i < totArchivos; i++) {
					JsonArray jsonDocumentos = new JsonArray();
					
					String nombre = "file-" + i;
					File file = uploadRequest.getFile(nombre);
					String mimeType = uploadRequest.getContentType(nombre);

					float n = file.length() / 1024 / 1024;			/*Peso en megas*/
					float n64 = 4 * (n / 3) + (n % 3 != 0 ? 4 : 0);	/*peso megas base 64*/

					JsonObject enviaDocumentos = new JsonObject();

					JSONObject jsonObj2;
					jsonObj2 = JSONFactoryUtil.createJSONObject(jsonObj.getString(nombre));
					
					String nom = jsonObj2.getString("nom").replace(" ", "_");

					enviaDocumentos.addProperty("nombre", nom);
					enviaDocumentos.addProperty("extension", jsonObj2.getString("ext"));
					enviaDocumentos.addProperty("idCarpeta", carpeta.getIdCarpeta());
					enviaDocumentos.addProperty("idDocumento", "0");
					enviaDocumentos.addProperty("idCatalogoDetalle",
							jsonObj2.getString("idcatdet"));

					Map<String, Object> info = null;
					if (n64 > 1.49) {
						info = guardaDocumentos(resourceRequest, file, nom, mimeType,
								jsonObj2.getString("ext"));
						enviaDocumentos.addProperty("documento", "");
						enviaDocumentos.addProperty("url", (String) info.get("url"));
						enviaDocumentos.addProperty("leer", 1);
					} else {
						enviaDocumentos.addProperty("documento",
								Base64.encode(FileUtils.readFileToByteArray(file)));
						enviaDocumentos.addProperty("url", "");
						enviaDocumentos.addProperty("leer", 0);
					}
					
					jsonDocumentos.add(enviaDocumentos);
					System.out.println("json :" + enviaDocumentos.toString());
					try {
						DocumentoResponse respuesta = _ServiceGenerico.wsDocumentos(CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
								CotizadorModularServiceKey.TMX_CTE_TRANSACCION_POST,
								jsonDocumentos, 1, "DOCTRANSPORTE", cotizacion, "", p_usuario,
								CotizadorTransportistasPortletKeys.CotizadorTransportistas);
						if (n64 > 1.49) {
							if (respuesta.getMsg().toLowerCase().trim().equals("ok")) {
								elimianArchivo((long) info.get("idDoc"));
							}
						}
					} catch (Exception e) {
						// TODO: handle exception
						e.getStackTrace();
					}
				}

			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("error en el archivo");
		}
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


			System.out.println("------------------------url2 : " + url );
			String aux2 = user.getScreenName() + "-" + nombre + "-F_" + folio + "-C_" + cotizacion + "-V_" + version;
			
			DLFolder fCotizadores = DLFolderLocalServiceUtil.getFolder(idGroup, DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
					"Documentos_Cotizadores");
			
			
			FileEntry fileEntry = _dlAppService.addFileEntry(idGroup, fCotizadores.getFolderId(), nombre + "." + ext,
					mimeType, nombre + "." + ext, aux2, "hi", file, serviceContext);
			
			
			String urlDoc = url + "/documents/" + idGroup + "/" + fileEntry.getFolderId() + "/" + fileEntry.getFileName()
			+ "/" + fileEntry.getUuid();
			
			System.out.println(urlDoc);
			
			_log.debug("--------------------------> documento:" + urlDoc);
			
			respuesta.put("url", urlDoc); 
			respuesta.put("idDoc", fileEntry.getFileEntryId()); 
			return respuesta;
		} catch (PortalException e) {
			// TODO Auto-generated catch block
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
		}
	}
	
	void enviaComentarios(ResourceRequest resourceRequest) throws CotizadorModularException {
		String comentario = ParamUtil.getString(resourceRequest, "comentarios");
		comentario = Validator.isNull(comentario) ? "-- Sin comentarios --" : comentario;
		String cotizacion = ParamUtil.getString(resourceRequest, "cotizacion");
		int version = ParamUtil.getInteger(resourceRequest, "version");
		User user = (User) resourceRequest.getAttribute(WebKeys.USER);
		String usuario = user.getScreenName();

		_ServiceGenerico.guardarComentario(cotizacion, version, 1, 0, comentario, usuario,
				CotizadorTransportistasPortletKeys.CotizadorTransportistas);
	}
	
}
