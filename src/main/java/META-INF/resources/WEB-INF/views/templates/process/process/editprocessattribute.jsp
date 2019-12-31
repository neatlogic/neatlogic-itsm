<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<div>
	<form id="formProcessAttribute">
		<table class="table table-hover">
			<thead>
				<tr class="noborder">
					<%-- <th>${tk:lang("名称","") }</th>--%>
					<th>${tk:lang("标签","") }</th>
					<%--<th>${tk:lang("宽度","") }</th>--%>
					<th>${tk:lang("类型","") }</th>
					<th>${tk:lang("处理器","") }</th>
					<th style="text-align: right">
						<button type="button" class="btn btn-grey btn-min btnAddAttribute">
							<i class="ts-plus"></i>
						</button>
					</th>
				</tr>
			</thead>
			<tbody id="tbAttribute">
				{{?it.attributeList && it.attributeList.length > 0}} {{~it.attributeList:attr:index}}
				<tr class="trProcessAttribute">
					<%--<td>
						<input type="text" class="txtProcessAttributeName" value="{{=attr.name||''}}">
						
					</td>--%>
					<td>
						<input type="text" class="txtProcessAttributeLabel" value="{{=attr.label||''}}">
						<input type="hidden" class="hidProcessAttributeUuid" value="{{=attr.uuid}}">
					</td>
					<%--<td>
						<select class="sltProcessAttributeWidth" plugin-checkselect data-width="80px" data-searchable="false">
							<option value="1" {{=attr.width=='1'?'selected':''}}>100%</option>
							<option value="2" {{=attr.width=='2'?'selected':''}}>50%</option>
						</select>
					</td>--%>
					<td>
						{{=attr.typeName}}
						<input type="hidden" class="hidProcessAttributeTypeName" value="{{=attr.typeName}}">
					</td>
					<td>
						{{=attr.handlerName}}
						<input type="hidden" class="hidProcessAttributeHandlerName" value="{{=attr.handlerName}}">
						<input type="hidden" class="hidProcessAttributeHandler" value="{{=attr.handler}}">
					</td>
					<td style="text-align: right">
						<div class="btn-group btnManager ">
							<button type="button" class="btn btn-grey dropdown-toggle btn-min" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
								<i class="glyphicon glyphicon-option-horizontal"></i>
							</button>
							<ul class="dropdown-menu pull-right">
								{{?attr.configPage}}
								<li>
									<a href="javascript:void(0)" class="btnConfigAttribute">${tk:lang("配置","") }</a>
								</li>
								{{?}}
								<li>
									<a href="javascript:void(0);" class="btnDelAttribute">${tk:lang("删除","") }</a>
								</li>
							</ul>
						</div>
					</td>
				</tr>
				{{~}} {{?}}
			</tbody>
		</table>
		<script class="xdotScript">
			var fn = {
				'.btnAddAttribute' : {
					'click' : function() {
						var that = this.root;
						var html = xdoT.render('process.attribute.searchattribute', {});
						var slidedialog = createSlideDialog({
							title : '${tk:lang("选择属性","")}',
							content : html,
							width : 1000,
							successFuc : function() {
								$('.chkAttribute:checked').each(function() {
									var uuid = $(this).val();
									var isExists = false;
									$('.hidProcessAttributeUuid', that).each(function() {
										if ($(this).val() == uuid) {
											isExists = true;
											return false;
										}
									});
									if (!isExists) {
										$.getJSON('${pageContext.request.contextPath}/module/process/attribute/get/' + uuid, function(data) {
											var html = xdoT.render('process.process.processattributetr', data);
											$('#tbAttribute', that).append(html);
										});
									}
								});
							}
						});
					}
				},
				'.btnDelAttribute' : {
					'click' : function() {
						$(this).closest('tr').remove();
					}
				}
			};
		</script>
	</form>
</div>