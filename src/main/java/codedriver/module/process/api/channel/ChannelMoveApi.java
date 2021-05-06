/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.channel;

import codedriver.framework.lrcode.constvalue.MoveType;
import codedriver.framework.lrcode.exception.MoveTargetNodeIllegalException;
import codedriver.framework.process.exception.channel.ChannelNameRepeatException;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.exception.catalog.CatalogNotFoundException;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.process.auth.CATALOG_MODIFY;

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
