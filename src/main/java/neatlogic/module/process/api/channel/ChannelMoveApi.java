/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.api.channel;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.lrcode.constvalue.MoveType;
import neatlogic.framework.lrcode.exception.MoveTargetNodeIllegalException;
import neatlogic.framework.process.auth.CATALOG_MODIFY;
import neatlogic.framework.process.dao.mapper.CatalogMapper;
import neatlogic.framework.process.dao.mapper.ChannelMapper;
import neatlogic.framework.process.dto.CatalogVo;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.process.exception.catalog.CatalogNotFoundException;
import neatlogic.framework.process.exception.channel.ChannelNameRepeatException;
import neatlogic.framework.process.exception.channel.ChannelNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = CATALOG_MODIFY.class)
public class ChannelMoveApi extends PrivateApiComponentBase {

    @Resource
    private ChannelMapper channelMapper;

    @Resource
    private CatalogMapper catalogMapper;

    @Override
    public String getToken() {
        return "process/channel/move";
    }

    @Override
    public String getName() {
        return "服务通道移动位置接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "被移动的服务通道uuid"),
            @Param(name = "targetUuid", type = ApiParamType.STRING, isRequired = true, desc = "目标节点uuid"),
            @Param(name = "moveType", type = ApiParamType.ENUM, rule = "inner,prev,next", isRequired = true, desc = "移动类型")
    })
    @Description(desc = "服务通道移动位置接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
        //目标节点uuid
        String targetUuid = jsonObj.getString("targetUuid");
        if (uuid.equals(targetUuid)) {
            throw new MoveTargetNodeIllegalException();
        }
        ChannelVo moveChannel = channelMapper.getChannelByUuid(uuid);
        //判断被移动的服务通道是否存在
        if (moveChannel == null) {
            throw new ChannelNotFoundException(uuid);
        }
        int oldSort = moveChannel.getSort();
        int newSort;
        String oldParentUuid = moveChannel.getParentUuid();
        String newParentUuid;
        MoveType moveType = MoveType.getMoveType(jsonObj.getString("moveType"));
        if (MoveType.INNER == moveType) {
            //通道只能移进目录里面，不能移进通道里面
            CatalogVo target = catalogMapper.getCatalogByUuid(targetUuid);
            if (target == null) {
                throw new CatalogNotFoundException(targetUuid);
            }
            newParentUuid = targetUuid;
            int maxSort = channelMapper.getMaxSortByParentUuid(newParentUuid);
            if (oldParentUuid.equals(newParentUuid)) {
                //相同目录下移动
                newSort = maxSort;
                if (newSort == oldSort) {
                    return null;
                } else {
                    //旧目录，被移动目录后面的兄弟节点序号减一
                    channelMapper.updateSortDecrement(oldParentUuid, oldSort + 1, null);
                }
            } else {
                //不同目录下移动
                newSort = maxSort + 1;
                //旧目录，被移动目录后面的兄弟节点序号减一
                channelMapper.updateSortDecrement(oldParentUuid, oldSort + 1, null);
                //新目录，目标目录后面的兄弟节点序号加一
                channelMapper.updateSortIncrement(newParentUuid, newSort, null);
            }
        } else {
            //目标节点可能是目录或通道
            CatalogVo targetCatalog = catalogMapper.getCatalogByUuid(targetUuid);
            if (targetCatalog != null) {
                //不管是移动到目录的前面还是后面，移动后排在最前面
                newSort = 1;
                newParentUuid = targetCatalog.getParentUuid();
                if (oldParentUuid.equals(newParentUuid)) {
                    //相同目录下移动
                    if (newSort == oldSort) {
                        return null;
                    } else {
                        channelMapper.updateSortIncrement(oldParentUuid, newSort, oldSort - 1);
                    }
                } else {
                    //不同目录下移动
                    //旧目录，被移动目录后面的兄弟节点序号减一
                    channelMapper.updateSortDecrement(oldParentUuid, oldSort + 1, null);
                    //新目录，目标目录后面的兄弟节点序号加一
                    channelMapper.updateSortIncrement(newParentUuid, newSort, null);
                }
            } else {
                ChannelVo targetChannel = channelMapper.getChannelByUuid(targetUuid);
                if (targetChannel != null) {
                    //移动到其他服务的前面或后面
                    newParentUuid = targetChannel.getParentUuid();
                    if (oldParentUuid.equals(newParentUuid)) {
                        //相同目录下移动
                        if (oldSort > targetChannel.getSort()) {
                            //往前移动
                            if (MoveType.PREV == moveType) {
                                newSort = targetChannel.getSort();
                            } else {
                                newSort = targetChannel.getSort() + 1;
                            }
                            if (newSort == oldSort) {
                                return null;
                            }
                            channelMapper.updateSortIncrement(oldParentUuid, newSort, oldSort - 1);
                        } else {
                            //往后移动
                            if (MoveType.PREV == moveType) {
                                newSort = targetChannel.getSort() - 1;
                            } else {
                                newSort = targetChannel.getSort();
                            }
                            if (newSort == oldSort) {
                                return null;
                            }
                            channelMapper.updateSortDecrement(oldParentUuid, oldSort + 1, newSort);
                        }
                    } else {
                        //不同目录下移动
                        if (MoveType.PREV == moveType) {
                            newSort = targetChannel.getSort();
                        } else {
                            newSort = targetChannel.getSort() + 1;
                        }
                        //旧目录，被移动目录后面的兄弟节点序号减一
                        channelMapper.updateSortDecrement(oldParentUuid, oldSort + 1, null);
                        //新目录，目标目录后面的兄弟节点序号加一
                        channelMapper.updateSortIncrement(newParentUuid, newSort, null);
                    }

                } else {
                    throw new ChannelNotFoundException(targetUuid);
                }
            }
        }
        moveChannel.setSort(newSort);
        moveChannel.setParentUuid(newParentUuid);
        //判断移动后相同目录下是否有同名服务
        if (channelMapper.checkChannelNameIsRepeat(moveChannel) > 0) {
            throw new ChannelNameRepeatException(moveChannel.getName());
        }
        channelMapper.updateChannelForMove(moveChannel);
        return null;
    }

}
