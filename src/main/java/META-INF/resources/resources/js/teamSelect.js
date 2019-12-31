/**
 * 机构选择（单选）
 * 1、页面添加modal：
<div class="modal fade" id="teamSelectorModalDiv">
 <div class="modal-dialog">
 <div class="modal-content">
 <div class="modal-header">
 <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
 <h4 class="modal-title">机构选择</h4>
 </div>
 <div class="modal-body">
 <div class="divTeamSelector">
 <div class="menuContent" style="max-height: 300px; overflow-y: auto; overflow-x: hidden;">
 <ul class="ztree" id="teamTree">
 </ul>
 </div>
 <input type="hidden" id="hidCurrentTeamIndex" value="">
 </div>
 </div>
 <div class="modal-footer">
 <button type="button" class="btn btn-default btn-sm" data-dismiss="modal">关闭</button>
 </div>
 </div>
 </div>
</div>

 2、先调用initTeamNode();初始化机构数据
 3、增加onclick事件： showTeamSelectorMenu();
 */


var teamNodeData;
var setting = {
    /*
     check: {
     enable: true,
     chkboxType: {"Y":"s", "N":"s"}
     },
     */
    view: {
        dblClickExpand: false
    },
    data: {
        simpleData: {
            enable: true
        }
    },
    callback: {
        onClick: onTeamClick
    }
};

function loadTeamNodeData(){
    $.getJSON('/balantflow/team/getTeamListForTree.do', function(data){
        if(data.length > 0){
            teamNodeData = data;

        }else{
            showPopMsg.error("组织机构数据加载异常...");
        }
    });
}

function initTeamNode(){
    loadTeamNodeData();
    //初始化
    /*
    var currentTeams = $(".sltTeam").val();
    if(currentTeams!="") {
        $(currentTeams).each(function (i, item) {
            $(teamNodeData).each(function (j, node) {
                if (item == node.id) {
                    node.checked = true;
                }
            });
        });
    }
    */
}

function initTeamSelectorTree(id){
//    $.fn.zTree.destroy(id);
    $.fn.zTree.init( $("#"+id), setting, teamNodeData);
}

function onTeamClick(e, treeId, treeNode) {
    var index = $('#hidCurrentTeamIndex').val();
    $('.teamName' + index).val( treeNode.name );
    $('.teamValue' + index).val( treeNode.id );
    hideTeamSelectorMenu();
}

function onTeamMultiCheck(e,treeId,treeNode){
    var zTree = $.fn.zTree.getZTreeObj("teamTree"),
        nodes = zTree.getCheckedNodes(true),
        v = "";
    var dom = $(".sltTeam");
    dom.empty();
    for (var i=0, l=nodes.length; i<l; i++) {
        v += nodes[i].name + ",";
        var optionHtml = "<option value='" + nodes[i].id +"' selected>" + nodes[i].name +"</option>";
        dom.append(optionHtml);
    }
    if (v.length > 0 ) v = v.substring(0, v.length-1);
    $("#txtTeam").val( v );
}

function showTeamSelectorMenu(el,type) {
    var iptObj = $(el);
    var index =  iptObj.data("index") ;
    var domId = "divTeamTree" + index;
    $( "#" + domId ).html( $("#teamTreeTemplate") );
    $('#hidCurrentTeamIndex').val( index );
    $('#hidTeamSelectType').val( type );
    initTeamSelectorTree( domId );
    if(type == 'modal') {
        $("#teamSelectorModalDiv").modal('show');
    }
    if(type == 'popover'){
        var popOpt = {
            placement : 'bottom',
            title: '请选择组织机构',
            html: true,
            content : $( "#" + domId )
        };
        iptObj.popover(popOpt);
        iptObj.popover('show');
    }
}

function getTeamTreeDom(){
    return $('.divTeamSelector');
}

function hideTeamSelectorMenu() {
    var type = $('#hidTeamSelectType').val();
    if(type == 'modal') {
        $("#teamSelectorModalDiv").modal('hide');
    }
    if(type == 'popover'){
        $.fn.zTree.destroy();
        $('.btnTeamSelector').popover('hide');
    }
}

function onBodyDown(event) {
    if (!(event.target.id == "menuBtn" || event.target.id == "inputTeam" || event.target.id == "menuContent" || $(event.target).parents("#menuContent").length>0)) {
        hideTeamSelectorMenu();
    }
}