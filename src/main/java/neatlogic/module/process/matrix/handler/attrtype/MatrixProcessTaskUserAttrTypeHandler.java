/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.process.matrix.handler.attrtype;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.matrix.core.IMatrixAttrType;
import neatlogic.framework.matrix.dto.MatrixAttributeVo;
import neatlogic.framework.process.constvalue.ProcessTaskGroupSearch;
import neatlogic.framework.process.constvalue.ProcessUserType;
import neatlogic.module.process.convalue.matrix.MatrixAttributeType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class MatrixProcessTaskUserAttrTypeHandler implements IMatrixAttrType {

    @Override
    public String getHandler() {
        return MatrixAttributeType.PROCESSTASKUSER.getValue();
    }

    @Override
    public void getTextByValue(MatrixAttributeVo matrixAttribute, Object valueObj, JSONObject resultObj) {
        String value = valueObj.toString();
        String text = ProcessUserType.getText(value.replace(ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue() + "#", StringUtils.EMPTY));
        if (StringUtils.isNotBlank(text)) {
            resultObj.put("text", text);
        }
    }
}
