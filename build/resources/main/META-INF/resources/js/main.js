$( document ).ready(function() {
	showLoader();	
	window.scrollTo(0, 0);
	
	var retroactivo;
	/*var hoy = new Date();*/
	if(diasRetro > 0) {
		if(diasRetro == 90) {
			retroactivo = new Date();
			retroactivo.setMonth(retroactivo.getMonth()-3);
		}
		else {
			retroactivo = -diasRetro;
		}
	}
	else{
		retroactivo = new Date();
	}
	
	$( '.datepicker' ).pickadate( {
		format : 'yyyy-mm-dd', 
		formatSubmit : 'yyyy-mm-dd',
		min : retroactivo,
		max : 365
	} );
	
	hideLoader();
});

function aplicaReglas() {
	seleccionaModo();
}

/*
function seleccionaModo() {
	switch (infCotizacion.modo) {
		case modo.NUEVA :
			console.log('selmodomain');
			ocultaCampos( ".divFolio" );
			$('#dc_coaseguro option[value="2575"]').attr('selected', true);
			$('.divRdoVigencia .form-check-input[value=1]').attr('checked', true);
			$("#dc_movimientos").prop("disabled", true);
			break;
		case modo.EDICION :
			if(datosCliente != ''){
				auxP1.infoClientExistenttEncontrado = JSON.parse(datosCliente);
				bloqueaCampoEdicion();
			}else{
				showMessageError('.navbar', msj.es.errorInformacion, 0);
				console.error(" Modo Edicion");
			} 
			break;
		case modo.COPIA :
			if(datosCliente == ''){
				showMessageError('.navbar', msj.es.errorInformacion, 0);
				console.error(" Modo copia");
			}else{
				auxP1.infoClientExistenttEncontrado = JSON.parse(datosCliente);
				$("#dc_subgiro").prop("disabled", false);
			}
			break;
		case modo.ALTA_ENDOSO :
			auxP1.infoClientExistenttEncontrado = JSON.parse(datosCliente);
			$('#dc_dateDesde').off("change");
			bloqueaCampoAltaEndoso();
			
			break;
		case modo.EDITAR_ALTA_ENDOSO :
			auxP1.infoClientExistenttEncontrado = JSON.parse(datosCliente);
			$('#dc_dateDesde').off("change");
			bloqueaCampoAltaEndoso();
			break;
		case modo.CONSULTA :
			auxP1.infoClientExistenttEncontrado = JSON.parse(datosCliente);
			bloqueaCamposConsulta();
			break;
		case modo.BAJA_ENDOSO :
			auxP1.infoClientExistenttEncontrado = JSON.parse(datosCliente);
			$('#dc_dateDesde').off("change");
			bloqueaCampoBajaEndoso();
			break;
		case modo.EDITAR_BAJA_ENDOSO :
			auxP1.infoClientExistenttEncontrado = JSON.parse(datosCliente);
			$('#dc_dateDesde').off("change");
			bloqueaCampoBajaEndoso();
			break;
		case modo.FACTURA_492 :
			showLoader();
			ocultaCampos( "#paso1_next" );
			auxP1.infoClientExistenttEncontrado = JSON.parse(datosCliente);
			bloqueaCamposConsulta();
			redirigePaso3();
			break;
		case modo.EDICION_JAPONES:
			if(datosCliente != ''){
				auxP1.infoClientExistenttEncontrado = JSON.parse(datosCliente);
				bloqueaCamposEdicionJapones();
				bloqueaCampoEdicion();
			}
			else{
				showMessageError('.navbar', msj.es.errorInformacion, 0);
				console.error(" Modo Edicion");
			}
			
			break;
		default:
			showMessageError('.navbar', msj.es.errorInformacion, 0);
			console.error(" Modo default");
			break;
	}
}
*/

function seleccionaTipoCotizacion() {
	switch (infCotizacion.tipoCotizacion) {
		case tipoCotizacion.FAMILIAR:
			ocultaCampos( ".empresarial_giros" );
			ocultaCampos( $("#dc_detalleSubgiro").parent() );
			break;
		case tipoCotizacion.EMPRESARIAL:
			muestraCampos( ".empresarial_giros" );
			muestraCampos( $("#dc_detalleSubgiro").parent() );
			break;
		default:
			ocultaCampos( "#paso1_next" );
	}
}

function bloqueaRadio(){
	/*$(".divRdoVigencia .form-check-input[value = " + infVigencia +"]").trigger("click")*/
	deshabilitaRadio(".divRdoTpClient", true);
	deshabilitaRadio(".divRdoVigencia", true);
	
}

function bloqueaCampoEdicion(){
	bloqueaRadio();
	$("#ce_rfc").prop("disabled", true);
	$("#ce_nombre").prop("disabled", true);
	$("#ce_codigo").prop("disabled", true);
	$("#dc_movimientos").prop("disabled", true);
	$("#dc_dateDesde").prop("disabled", true);
	$("#dc_agentes").prop("disabled", true);
	$("#dc_giro").prop("disabled", true);
	$("#dc_subgiro").prop("disabled", true);
	$("#dc_detalleSubgiro").prop("disabled", true);
	activaMsjMoneda();
} 

function bloqueaCampoAltaEndoso(){
	bloqueaRadio();
	$("#ce_rfc").prop("disabled", true);
	$("#ce_nombre").prop("disabled", true);
	$("#ce_codigo").prop("disabled", true);
	$("#dc_movimientos").prop("disabled", true);
	$("#dc_agentes").prop("disabled", true);
	$("#dc_moneda").prop("disabled", true);
	$("#dc_formpago").prop("disabled", true);
	$("#dc_giro").prop("disabled", true);
	$("#dc_subgiro").prop("disabled", true);
	$("#dc_detalleSubgiro").prop("disabled", true);
	$(".divRdoVigencia").addClass("d-none");
	
	$("#dc_dateHasta").prop("disabled", true);
	var pick_fin = $( '#dc_dateHasta' ).pickadate( 'picker' );
	var fin = pick_fin.get("select");
	pick_fin.set('max', fin.obj);
} 


function bloqueaCampoBajaEndoso(){
	bloqueaRadio();
	$("#ce_rfc").prop("disabled", true);
	$("#ce_nombre").prop("disabled", true);
	$("#ce_codigo").prop("disabled", true);
	$("#dc_movimientos").prop("disabled", true);
	$("#dc_agentes").prop("disabled", true);
	$("#dc_moneda").prop("disabled", true);
	$("#dc_formpago").prop("disabled", true);
	$("#dc_giro").prop("disabled", true);
	$("#dc_subgiro").prop("disabled", true);
	$("#dc_detalleSubgiro").prop("disabled", true);
	$(".divRdoVigencia").addClass("d-none");
	
	$("#dc_dateHasta").prop("disabled", true);
	var pick_fin = $( '#dc_dateHasta' ).pickadate( 'picker' );
	var fin = pick_fin.get("select");
	pick_fin.set('max', fin.obj);
} 

function bloqueaCamposConsulta(){
	bloqueaRadio();
	$("#ce_rfc").prop("disabled", true);
	$("#ce_nombre").prop("disabled", true);
	$("#ce_codigo").prop("disabled", true);
	$("#dc_movimientos").prop("disabled", true);
	$("#dc_agentes").prop("disabled", true);
	$("#dc_moneda").prop("disabled", true);
	$("#dc_formpago").prop("disabled", true);
	$("#dc_dateDesde").prop("disabled", true);
	$("#dc_dateHasta").prop("disabled", true);
	$("#dc_giro").prop("disabled", true);
	$("#dc_subgiro").prop("disabled", true);
	$("#dc_detalleSubgiro").prop("disabled", true);
}

function bloqueaCamposEdicionJapones(){
	$("#dc_movimientos").prop("disabled", true);
	$("#dc_canalneg").removeAttr("disabled");
}

function activaMsjMoneda(){
	$("#dc_moneda").on("change", function(){
		showMessageError('.navbar', "Si modifica la moneda de esta cotización el cambio aplicara para todas las versiones", 0);
	});	
}

function desactivaMsjMoneda(){
	$("#dc_moneda").off("change");
}

$('#dc_dateDesde').on("change", function() {
	var pick_ini = $( '#dc_dateDesde' ).pickadate( 'picker' );
	var pick_fin = $( '#dc_dateHasta' ).pickadate( 'picker' );
	var iniSelec = pick_ini.get("select");
	if(valIsNullOrEmpty(iniSelec)){
		pick_fin.set('clear');
	}else{
		var anioSig = new Date((iniSelec.year +1), iniSelec.month, iniSelec.date);
		pick_fin.set('max', anioSig);
		pick_fin.set('select', anioSig);
		pick_fin.set('view', anioSig);		
	}
});

$(".tip_fisica input:text").keyup(function(){
	$("#cn_nombrecompleto").val(
	$("#cn_fisnombre"). val() + " "	+	
	$("#cn_fispaterno"). val() + " " +		
	$("#cn_fismaterno"). val() + "."		
	);
});



function actualisaFisica(){
	$(".cn_ncEx").addClass("col-md-6");
	$(".cn_ncEx").removeClass("col-md-8");
	$(".cn_tpEx").addClass("col-md-3");
	$(".cn_tpEx").removeClass("col-md-4");
	$(".cn_rdEx").removeClass("d-none");
}

function actualizaMoral(){
	$(".cn_ncEx").addClass("col-md-8");
	$(".cn_ncEx").removeClass("col-md-6");
	$(".cn_tpEx").addClass("col-md-4");
	$(".cn_tpEx").removeClass("col-md-3");
	$(".cn_rdEx").addClass("d-none");
}

$(".cn_rdEx .switch #chktoggle").change(function(){
	if($(".cn_rdEx .switch #chktoggle").is(":checked")){
		$("#cn_fismaterno").removeClass("valExistt");
	}else{
		$("#cn_fismaterno").addClass("valExistt");
	}
});


$( '.tip_fisica .tip_fisica_llena' ).keyup( function() {
	fLlenaNombreFisica();
} );

$( '.divPerMor #cn_nombrecontratante' ).keyup( function() {
	fLlenaNombreMoral();
} );

$( '.divPerMor #cn_denominacion' ).on( 'change', function() {
	fLlenaNombreMoral();
} );

function fLlenaNombreFisica() {
	var apPat = $( "#cn_fispaterno" ).val();
	var apMat = $( "#cn_fismaterno" ).val();
	var nom = $( "#cn_fisnombre" ).val();
	$( "#cn_nombrecompleto" ).val( nom + " " + apPat + " " + apMat );
	activaCampos( "#cn_nombrecompleto" );
}

function fLlenaNombreMoral() {
	var nom = ($( "#cn_nombrecontratante" ).val().length > 0) ? ($( "#cn_nombrecontratante" ).val() + ", ") : "";
	var tip = ($( "#cn_denominacion :selected" ).val() != '-1') ? $( "#cn_denominacion :selected" ).text() : "";
	$( "#cn_nombrecompleto" ).val( nom + tip );
	activaCampos( "#cn_nombrecompleto" );
}


$( '#modalClienteExistente' ).on( 'hidden.bs.modal', function() {
	$(".data_ctenvo .form-control:input:text").val("");
} );



/*function seleccionaTipoPer(){
	if (auxP1.infoClientExistenttEncontrado.tipoPer == 217){
		infCotizacion.tipoPersona = tipoPersona.FISICA;
	}else if(auxP1.infoClientExistenttEncontrado.tipoPer == 218){
		infCotizacion.tipoPersona = tipoPersona.MORAL;
	}
}*/

function validaRequeridos(){
	var campos = $("#contPaso1 input:visible:enabled:not(:radio)");
	var completos = true;
	if( $(".cn_rdEx .switch:visible #chktoggle").is(":checked")){
		campos = $("#contPaso1 input:visible:enabled:not(:radio)").not("#cn_fismaterno");
	}
	$.each(campos, function(key, campo) {
		if($(campo).hasClass("select-dropdown")){
			var select = $(campo).siblings("select");
			completos = noSelect($(select)) ? false : completos;
		}else{
			completos = vaciosInpText($(campo)) ? false : completos;	
		}	
	});
	return completos;
}

function llenaTpoVigencia(){
	if($("#dc_cotizarVig").is(":checked")){
		DatosGenerales.vigencia =  0;
		DatosGenerales.fecinicio =  $("#dc_dateDesde").val();
		DatosGenerales.fecfin =  $("#dc_dateHasta").val();
	}else{
		DatosGenerales.vigencia =  1;
		DatosGenerales.fecinicio =  "";
		DatosGenerales.fecfin =  "";
	}
}

function guardaPaso1(){
	$.post( ligasServicios.guardaInfo, {
		datos : JSON.stringify(DatosGenerales),
		infoCot : JSON.stringify(infCotizacion)
	}, function(data) {
		
		console.log(data);
		var response = JSON.parse(data);
		if(response.code == 0){
			window.location.href = response.url ;
		}else{
			showMessageError( '.navbar', response.msg, 0 );
			hideLoader();
		}
		
	});
	
}

$('#dc_subgiro').change(function() {
	if(!valIsNullOrEmpty(perfilSuscriptor)){
		if(perfilSuscriptor == '0'){
			if($('option:selected', this).attr('suscripcion') == '1'){
				$('#modalGiroSubgiro').modal({
	                show: true,
	                backdrop: 'static',
	                keyboard: false
	            });
			}
		}
	}
});


$("#btnSuscripGiroNo").click(function(){
	$('#dc_subgiro option[value=-1]').prop('selected', true);
    selectDestroy($('#dc_subgiro'), false);
    $('#modalGiroSubgiro').modal('hide');
});

function redirigePaso3(){
	
	llenaDatos();
	$.post( ligasServicios.guardaInfo, {
		datos : JSON.stringify(DatosGenerales),
		infoCot : JSON.stringify(infCotizacion)
	}, function(data) {
		console.log(data);
		$.post(ligasServicios.redirige, {
			infoCot : JSON.stringify( infCotizacion ),
			paso : "/paso3"
	    }).done(function(data) {
	        sessionExtend();
	        var response = JSON.parse( data );
			if (response.code == 0) {
				window.location.href = response.msg;
			} else {
				showMessageError( '.navbar', response.msg, 0 );
				hideLoader();
			}
	    });
	});
	
	
	
}