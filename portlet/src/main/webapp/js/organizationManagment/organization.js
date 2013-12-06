$(function () {


	// Load the fragment from the resource controller
	$("#organizationTable").jzLoad("OrganizationManagment.getPage()",{"pageToDisplay":"1"});

	$.validator.addMethod("customRemote", function(value, element) {
		if (!($("#inputOrganizationName").val() == $("#oldOrganizationName").val())) {
		    return $.validator.methods.remote.apply(this, arguments);
		} else {
			return true;
		}
	}, $.validator.messages.remote);

	validator = $("#newOrganizationForm").validate({
        	rules: {
                inputOrganizationName: {
                	remote: "/opencloudware-portlet/organization/isValidOrganizationName",
    				minlength: 2
                },
    			inputOrganizationAddress: {
    				minlength: 2
    			}
            },
            messages: {
                inputOrganizationName: { remote: "This organization name already exists." }
             },
            highlight: function(element) {
            	$(element).closest('.control-group').removeClass('success').addClass('error');
            },
            success: function(element) {
            	element.text("OK !").addClass('valid').closest('.control-group').removeClass('error').addClass('success');
            }
    	});

    	validatorEdit = $("#editOrganizationForm").validate({
			rules: {
				inputOrganizationName: {
					customRemote: "/opencloudware-portlet/organization/isValidOrganizationName",
					minlength: 2
				},
				inputOrganizationAddress: {
					minlength: 2
				}
			},
			messages: {
				inputOrganizationName: { remote: "This organization name already exists." }
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
	$("#organizationTable").jzLoad("OrganizationManagment.getPage()",{"pageToDisplay":wantedPage});
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