$(function () {



    var params = extractUrlParams();
    var model = "/portal/g/:opencloudware:"

    if(location.pathname.substring(0,model.length) == model) {
        var groupId = location.pathname.split("/")[3];
        groupId = groupId.replace(/:/g,"/");

        $("#users").jzLoad("UsersManagement.getUsersFragmentByGroupId()",{"groupId":groupId});

    }else if (params["organization"]) {
        $("#users").jzLoad("UsersManagement.getUsersFragment()",{"organizationId":params["organization"]});
    } else {
        $("#users").jzLoad("UsersManagement.getUsersFragment()",{"organizationId":$( "#myOrganizationSelect option:selected" ).val()});
    }



    $("#addRole").click(function() {
        $("#possibleRoles option:selected" ).each(function() {
            var currentVal = $(this).val();
            $("#actualRoles").append('<option value="'+currentVal+'">'+currentVal+'</option>');

        });
        $("#possibleRoles option:selected").remove();

    });

    $("#removeRole").click(function() {
        $("#actualRoles option:selected" ).each(function() {
            var currentVal = $(this).val();
            $("#possibleRoles").append('<option value="'+currentVal+'">'+currentVal+'</option>');
        });
        $("#actualRoles option:selected").remove();


    });


});


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