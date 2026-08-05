package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.FileUtil;
import neatlogic.framework.exception.file.FileNotFoundException;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.binarystream.PrivateBinaryStreamApiComponentBase;
import neatlogic.module.process.dao.mapper.ProcessTaskDataMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Transactional
@Component
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ImportProcessTaskDataApi extends PrivateBinaryStreamApiComponentBase {
    private final static Logger logger = LoggerFactory.getLogger(ImportProcessTaskDataApi.class);

    @Resource
    private ProcessTaskDataMapper processTaskDataMapper;

    @Resource
    private FileMapper fileMapper;

    @Override
    public String getToken() {
        return "processtask/data/import";
    }

    @Override
    public String getName() {
        return "nmpap.importprocesstaskdataapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "fileId", type = ApiParamType.LONG, isRequired = true, desc = "common.fileid")
    })
    @Output({})
    @Description(desc = "nmpap.importprocesstaskdataapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, List<Integer>> resultObj = new HashMap<>();
        Long fileId = paramObj.getLong("fileId");
        FileVo fileVo = fileMapper.getFileById(fileId);
        if (fileVo == null) {
            throw new FileNotFoundException(fileId);
        }
        byte[] buf = new byte[1024];
        try (ZipInputStream zipIs = new ZipInputStream(FileUtil.getData(fileVo.getPath()));
             ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            ZipEntry zipEntry = null;
            while ((zipEntry = zipIs.getNextEntry()) != null) {
                if (zipEntry.isDirectory()) {
                    continue;
                }
                out.reset();
                if (zipEntry.getName().endsWith(".json")) {
                    int len;
                    while ((len = zipIs.read(buf)) != -1) {
                        out.write(buf, 0, len);
                    }
                    JSONObject jsonObject = JSONObject.parseObject(new String(out.toByteArray(), StandardCharsets.UTF_8));
                    if (MapUtils.isNotEmpty(jsonObject)) {
                        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                            String tableName = entry.getKey();
                            Object value = entry.getValue();
                            if (value != null) {
                                if (value instanceof JSONObject) {
                                    JSONObject valueObject = (JSONObject) value;
                                    if (MapUtils.isNotEmpty(valueObject)) {
                                        List<String> columnNameList = new ArrayList<>();
                                        List<Object> columnValueList = new ArrayList<>();
                                        for (Map.Entry<String, Object> entry1 : valueObject.entrySet()) {
                                            columnNameList.add(entry1.getKey());
                                            columnValueList.add(entry1.getValue());
                                        }
                                        int count = processTaskDataMapper.replaceOne(tableName, columnNameList, columnValueList);
                                        resultObj.computeIfAbsent(tableName, k -> new ArrayList<>()).add(count);
                                    }
                                } else if (value instanceof JSONArray) {
                                    JSONArray valueArray = (JSONArray) value;
                                    if (CollectionUtils.isNotEmpty(valueArray)) {
                                        for (int i = 0; i < valueArray.size(); i++) {
                                            JSONObject valueObject = valueArray.getJSONObject(i);
                                            List<String> columnNameList = new ArrayList<>();
                                            List<Object> columnValueList = new ArrayList<>();
                                            for (Map.Entry<String, Object> entry1 : valueObject.entrySet()) {
                                                columnNameList.add(entry1.getKey());
                                                columnValueList.add(entry1.getValue());
                                            }
                                            int count = processTaskDataMapper.replaceOne(tableName, columnNameList, columnValueList);
                                            resultObj.computeIfAbsent(tableName, k -> new ArrayList<>()).add(count);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return resultObj;
    }
}
