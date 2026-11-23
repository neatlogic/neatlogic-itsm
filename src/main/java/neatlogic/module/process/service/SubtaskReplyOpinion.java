package neatlogic.module.process.service;

import neatlogic.framework.process.constvalue.TaskReplyDisplayMode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

/** 在业务层生成当次审计展示快照，原始回复仍独立保存供编辑、通知和后续动作使用。 */
public final class SubtaskReplyOpinion {
    private SubtaskReplyOpinion() {
    }

    static String build(String content, String button, String mode) {
        Document document = Jsoup.parseBodyFragment(content == null ? "" : content);
        document.outputSettings().prettyPrint(false);
        Element body = document.body();
        boolean hasContent = hasContent(body);
        if (!TaskReplyDisplayMode.COMBINED.getValue().equals(TaskReplyDisplayMode.normalize(mode))) {
            return hasContent ? content : null;
        }
        if (!hasText(button)) {
            return hasContent ? content : null;
        }
        if (!hasContent) {
            body.empty();
        }
        // 按钮使用文本节点转义，仅两项都有有效内容时插入中文句号。
        TextNode prefix = new TextNode(button + (hasContent ? "。" : ""));
        Node first = body.childNodes().stream()
                .filter(node -> node instanceof Element || (node instanceof TextNode && hasText(((TextNode) node).getWholeText())))
                .findFirst().orElse(null);
        if (first instanceof Element && "p".equals(((Element) first).normalName())) {
            // 普通段落插入首段，保留加粗、链接等现有段内结构。
            ((Element) first).prependChild(prefix);
        } else if (first instanceof Element && ((Element) first).normalName().matches("address|article|aside|blockquote|div|dl|figure|h[1-6]|hr|ol|pre|section|table|ul")) {
            // 列表与表格前增加普通段落，不把文本写进结构化容器。
            Element paragraph = new Element("p");
            paragraph.appendChild(prefix);
            first.before(paragraph);
        } else {
            body.prependChild(prefix);
        }
        return body.html();
    }

    private static boolean hasContent(Element body) {
        // 空段落、换行、空白实体不算意见；图片和其他媒体即使没有文字也保留。
        Element visible = body.clone();
        visible.select("script, style").remove();
        return !visible.select("img, video, audio, iframe, svg, canvas, hr, object, embed").isEmpty()
                || hasText(visible.wholeText());
    }

    /** 服务与审计线程共用文本判空，避免不换行空格生成空操作明细。 */
    public static boolean hasText(String text) {
        // Character.isWhitespace 不覆盖不换行空格，补充 isSpaceChar 与 BOM 对齐前端判空。
        return text != null && text.codePoints().anyMatch(codePoint -> !Character.isWhitespace(codePoint)
                && !Character.isSpaceChar(codePoint) && codePoint != 0xfeff);
    }
}
