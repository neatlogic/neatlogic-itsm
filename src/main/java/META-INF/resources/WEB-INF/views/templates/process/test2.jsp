<form id="form_attribute_{{=it.attributeUuid}}" class="form_attribute" data-uuid="{{=it.attributeUuid}}">
	<table class="table table-hover">
		<thead>
			<tr>
				<th>源地址</th>
				<th>源端口</th>
				<th>目标地址</th>
				<th>目标端口</th>
				<th>
					<button type="button" class="btn btn-xs btnAdd">
						<i class="ts-plus"></i>
					</button>
				</th>
			</tr>
		</thead>
		<tbody class="tbMain">
			{{?it.config && it.config.length>0}} {{~it.config:config:index}}
			<tr>
				<td>
					<input type="text" class="input-medium txtSIp" value="{{=config.sourceip||''}}">
				</td>
				<td>
					<input type="text" class="input-medium txtSPort" value="{{=config.sourceport||''}}">
				</td>
				<td>
					<input type="text" class="input-medium txtTIp" value="{{=config.targetip||''}}">
				</td>
				<td>
					<input type="text" class="input-medium txtTPort" value="{{=config.targetport||''}}">
				</td>
				<td>
					<button type="button" class="btn btn-xs btnMinus">
						<i class="ts-minus"></i>
					</button>
				</td>
			</tr>
			{{~}} {{?}}
		</tbody>
	</table>
	<script class="xdotScript">
		var fn = {
			'.btnAdd' : {
				'click' : function() {
					var that = this.root;
					var tr = $('<tr><td><input type="text" class="input-medium txtSIp"></td><td><input type="text" class="input-medium txtSPort"></td><td><input type="text" class="input-medium txtTIp"></td><td><input type="text" class="input-medium txtTPort"></td><td><button type="button" class="btn btn-xs btnMinus"><i class="ts-minus"></i></button></td></tr>');
					$('.tbMain', that).append(tr);
					$('.btnMinus', tr).on('click', function() {
						$(this).closest('tr').remove();
					});
				}
			},
			'.btnMinus' : {
				'click' : function() {
					$(this).closest('tr').remove();
				}
			},
			'this' : {
				'getData' : function() {
					var that = this;
					var dataObj = new Array();
					$('.txtSIp', that).each(function(i, k) {
						var obj = {};
						obj['sourceip'] = $.trim($(this).val());
						obj['sourceport'] = $.trim($('.txtSPort:eq(' + i + ')', that).val());
						obj['targetip'] = $.trim($('.txtTIp:eq(' + i + ')', that).val());
						obj['targetport'] = $.trim($('.txtTPort:eq(' + i + ')', that).val());
						dataObj.push(obj);
					});
					return dataObj;
				},
				'getValue' : function() {
					var that = this;
					var dataObj = new Array();
					$('.txtSIp', that).each(function(i, k) {
						dataObj.push($(this).val());
					});
					return dataObj;
				}
			}
		}
	</script>
</form>