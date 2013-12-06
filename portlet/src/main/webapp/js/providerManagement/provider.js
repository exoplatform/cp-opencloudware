$(function () {


    var params = extractUrlParams();
    var model = "/portal/g/:opencloudware:"
    if(location.pathname.substring(0,model.length) == model) {
        var groupId = location.pathname.split("/")[3];
        groupId = groupId.replace(/:/g,"/");

        $("#providerList").jzLoad("ProviderManagement.changeOrganizationByGroupId()",{"groupId":groupId});
    }else if (params["organization"]) {
        $("#providerList").jzLoad("ProviderManagement.changeOrganization()",{"organizationId":params["organization"]});
    } else {
        $("#providerList").jzLoad("ProviderManagement.changeOrganization()",{"organizationId":$( "#myOrganizationSelect option:selected" ).val()});
    }



    validator = $("#newProviderForm").validate({
        rules: {
            inputProviderName: {
                required: true,
                minlength: 2
            },
            inputProviderVendor: {
                required: true,
                minlength: 2
            },
            inputProviderLogin: {
                required: true,
                minlength: 2
            },
            inputProviderPassword: {
                required: true,
                minlength: 5
            },
            inputConfirmPassword: {
                required: true,
                minlength: 5,
                equalTo: "#inputProviderPassword"
            }
        },
        highlight: function(element) {
            $(element).closest('.control-group').removeClass('success').addClass('error');
        },
        success: function(element) {
            element.text("OK !").addClass('valid').closest('.control-group').removeClass('error').addClass('success');
        }
    });

    validatorEdit = $("#editProviderForm").validate({
        rules: {
            inputProviderName: {
                required: true,
                minlength: 2
            },
            inputProviderVendor: {
                required: true,
                minlength: 2
            },
            inputProviderLogin: {
                required: true,
                minlength: 2
            },
            inputConfirmPassword: {
                equalTo: "#inputProviderPassword"
            }
        },
        highlight: function(element) {
            $(element).closest('.control-group').removeClass('success').addClass('error');
        },
        success: function(element) {
            element.text("OK !").addClass('valid').closest('.control-group').removeClass('error').addClass('success');
        }
    });
});


function changePage(wantedPage) {
    $("#providerTable").jzLoad("ProviderManagement.getPage()",{"pageToDisplay":wantedPage});
}


function clearValidation (){
    if (validator) {
        validator.resetForm();
    }
    if (validatorEdit) {
        validatorEdit.resetForm();
    }
    $('.control-group').removeClass('success');
    $('.control-group').removeClass('error');

}

/**
 * Fonction de récupération des paramètres GET de la page
 * @return Array Tableau associatif contenant les paramètres GET
 */
function extractUrlParams(){
    var t = location.search.substring(1).split('&');
    var f = [];
    for (var i=0; i<t.length; i++){
        var x = t[ i ].split('=');
        f[x[0]]=x[1];
    }
    return f;
}