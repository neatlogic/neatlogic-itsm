package codedriver.module.process.globalsearch.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.common.util.StringUtil;
import codedriver.framework.globalsearch.document.core.DocumentHandlerBase;
import codedriver.framework.globalsearch.dto.DocumentVo;
import codedriver.framework.globalsearch.dto.FieldVo;
import codedriver.framework.globalsearch.enums.DocumentType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskVo;

@Service
public class ProcessTaskDocumentHandler extends DocumentHandlerBase<ProcessTaskVo> {
	private final static int maxWord = 100;
	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Override
	public String getType() {
		return DocumentType.PROCESS_TASK.getName();
	}

	@Override
	public String getName() {
		return "流程工单";
	}

	@Override
	protected void myRebuildDocument() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void myMakeupDocument(DocumentVo documentVo) {
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(Long.parseLong(documentVo.getTargetId()));
		if (processTaskVo != null) {
			documentVo.setTitle(processTaskVo.getTitle());

			List<ProcessTaskStepContentVo> contentList = processTaskMapper.getProcessTaskStepContentProcessTaskId(processTaskVo.getId());
			StringBuilder sb = new StringBuilder();
			for (ProcessTaskStepContentVo processTaskStepContentVo : contentList) {
				if (StringUtils.isNotBlank(sb.toString())) {
					sb.append("|");
				}
				ProcessTaskContentVo contentVo = processTaskMapper.getProcessTaskContentByHash(processTaskStepContentVo.getContentHash());
				sb.append(StringUtil.removeHtml(contentVo.getContent()));
			}
			String c = getShortcut(documentVo, "c", StringUtil.removeHtml(sb.toString()), maxWord);
			c = c.replace("|", "<p>");
			documentVo.setContent(c);
			documentVo.setTargetUrl("/task/getTaskDetail.do?taskId=" + documentVo.getTargetId());
		}
	}

	@Override
	protected DocumentVo myCreateDocument(ProcessTaskVo target) {
		if (target != null && target.getId() != null) {
			ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(target.getId());
			if (processTaskVo != null) {
				DocumentVo documentVo = new DocumentVo(this.getType());
				List<FieldVo> fieldList = new ArrayList<>();
				documentVo.setDocumentFieldList(fieldList);
				documentVo.setTargetId(processTaskVo.getId().toString());
				FieldVo field = new FieldVo();
				field.setField("t");
				field.setContent(processTaskVo.getTitle());
				fieldList.add(field);
				List<ProcessTaskStepContentVo> contentList = processTaskMapper.getProcessTaskStepContentProcessTaskId(processTaskVo.getId());
				field = new FieldVo();
				field.setField("c");
				StringBuilder sb = new StringBuilder();
				for (ProcessTaskStepContentVo processTaskStepContentVo : contentList) {
					if (StringUtils.isNotBlank(sb.toString())) {
						sb.append("|");
					}
					ProcessTaskContentVo contentVo = processTaskMapper.getProcessTaskContentByHash(processTaskStepContentVo.getContentHash());
					sb.append(StringUtil.removeHtml(contentVo.getContent()));

				}
				field.setContent(sb.toString());
				fieldList.add(field);
				return documentVo;
			}
		}
		return null;
	}

}
