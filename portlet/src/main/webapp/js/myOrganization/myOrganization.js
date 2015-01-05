$(function () {


    var model = "/portal/g/:opencloudware:"
    console.log(location.pathname);
    if(location.pathname.substring(0,model.length) == model) {
        var groupId = location.pathname.split("/")[3];
        groupId = groupId.replace(/:/g,"/");

        $("#organization").jzLoad("MyOrganization.getOrganizationFragmentByGroupId()",{"groupId":groupId});
    }else {
        $("#organization").jzLoad("MyOrganization.getOrganizationFragment()",{"organizationId":$( "#myOrganizationSelect option:selected" ).val()});

    }





});
