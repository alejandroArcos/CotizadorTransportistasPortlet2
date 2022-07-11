$(document).ready(function() {
    var hoy = new Date();
    $('.datepicker2').pickadate({

        onOpen: function() {
            eliminaErrores();
        },

        format: 'yyyy-mm-dd',
        formatSubmit: 'yyyy-mm-dd',
        selectYears: 80,
        max: [hoy.getFullYear(), hoy.getMonth(), hoy.getDate()]
    });

    /*isFisica_Moral($('#tipoPersonaH').val());*/
});

/* #region  continuar chekbox */
$("#chkBxLeido").click(function() {

    if ($(this).is(':checked')) {
    	$('#btnsEmision').show();        
    } else {
    	$('#btnsEmision').hide();
    }
});




/**Funciones genericas**/
function eliminaErrores() {
    $(".alert-danger").remove();
    $('.invalid').removeClass('invalid');
}

function llenaInputSelectBySelector(campo, valor, desactivado) {
    $(campo + ' option[value=' + valor + ']').prop('selected', true);
    if ($(campo + ' option[value=' + valor + ']').length == 0) {
        desactivado = false;
    }
    if (desactivado) {
        $(campo).addClass("bqOri");
    }
    selectDestroyBySelector(campo, desactivado);
}

function selectDestroyBySelector(objeto, enabled) {
    $(objeto).prop("disabled", enabled);
    $(objeto).material_select('destroy');
    $(objeto).material_select();
}

function disableTxtCInfon() {
    $.each($('#frmPaso4 .infoMinRequerida input:text'), function(key, value) {
        if (!valIsNullOrEmpty($(this).val())) {
            $(this).attr('disabled', 'true');
        }
    });
}

function disableSelectCInfo() {
    $.each($('#frmPaso4 .infoMinRequerida select.infReqS'), function(key, value) {
        if ($(this).val() != -1) {
            selectDestroyBySelector('#' + $(this).attr('id'), true);
        }
        console.log("value: " + value);
    });
}
$("#frmPaso4 select").change(function() {
    eliminaErrores();
});

function valIsNullOrEmpty(value) {
    if (value === undefined) {
        return true;
    }
    value = value.trim();
    return (value == null || value == "null" || value === "");
}

$("#frmPaso4 .card-body .form-check-input").click(function() {
    console.log($(this).prop("id"));
    var divContenedor = $(this).closest(".check-valid");
    console.log($(divContenedor).prop("id"));
    console.log($(divContenedor).find(".form-check-input"));
    if ($(this).is(':checked')) {
        $(divContenedor).find(".form-check-input").not(this).prop('checked', false);
    } else {
        $(divContenedor).find(".form-check-input").not(this).prop('checked', true);
    }
});


/**Funciones genericas**/