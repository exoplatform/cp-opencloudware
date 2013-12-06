$(function () {

  	// Setup the collapse component
  	$("#organizationInformation").collapse();
	$("#organizationInformation").hide();


	validator = $("#newUserForm").validate({
    	rules: {
        	inputUserName: {
				minlength: 2,
				required: true,
				remote: "/opencloudware-portlet/register/isValidUsername"
			},
			inputFirstName: {
				minlength: 2,
				required: true
			},
			inputLastName: {
       			required: true,
				minlength: 2
          	},
			inputEmail: {
				email: true,
				maxlength: 255,
				minlength: 2,
				remote: "/opencloudware-portlet/register/isValidEmail",
				required: true
			},
			inputPassword: {
                required: true,
            	minlength: 5
			},
			inputConfirmPassword: {
                required: true,
            	minlength: 5,
            	equalTo: "#inputPassword"
			},
            inputOrganizationName: {
            	required: '#radioYes:checked',
            	remote: "/opencloudware-portlet/register/isValidOrganizationName",
				minlength: 2
            },
			inputOrganizationAddress: {
				required: '#radioYes:checked',
				minlength: 2
			},
			inputCreditCardNumber: {
				required: '#radioYes:checked',
				creditcard: true
			}
        },
        messages: {
            inputUserName: { remote: "This username already exists." },
            inputOrganizationName: { remote: "This organization name already exists." },
            inputEmail: { remote: "This mail address already exists." }
         },
        highlight: function(element) {
        	$(element).closest('.control-group').removeClass('success').addClass('error');
        },
        success: function(element) {
        	element.text("OK !").addClass('valid').closest('.control-group').removeClass('error').addClass('success');
        }/*,
        }
        errorPlacement: function(error,element) {
            element.addClass('error').closest('.control-group').removeClass('success').addClass('error');
            error.appendTo(element.parent().next());
        } */

	});


});


function clearValidation (){

	validator.resetForm();
    $('.control-group').removeClass('success');
    $('.control-group').removeClass('error');

}