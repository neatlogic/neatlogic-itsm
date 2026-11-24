package neatlogic.module.process.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/** 审计意见使用真实 HTML 解析器验证，兼容富文本结构和纯文本操作名称。 */
public class SubtaskReplyOpinionTest {
    /** 空段落、实体与 Unicode 空白不能生成多余句号或空意见。 */
    @Test
    public void emptyRepliesAndButtonsDoNotAddSeparators() {
        for (String empty : Arrays.asList(null, "", " \t\n ", "<p><br></p>", "<p>&nbsp;&#160;&#x2003;&ThinSpace;</p>",
                "<p>\u00a0\u2007\u202f\ufeff</p>", "<!-- comment --><script>x</script><style>x</style>")) {
            assertNull(SubtaskReplyOpinion.build(empty, " \t\u00a0 ", "combined"));
            assertNull(SubtaskReplyOpinion.build(empty, "同意", "separate"));
            assertEquals("同意", SubtaskReplyOpinion.build(empty, "同意", "combined"));
        }
        assertEquals("<p>原文</p>", SubtaskReplyOpinion.build("<p>原文</p>", null, "combined"));
        assertEquals("<p>原文</p>", SubtaskReplyOpinion.build("<p>原文</p>", "同意", "unknown"));
    }

    /** 段落内的格式和链接保留，操作永远是文本而不是 HTML。 */
    @Test
    public void paragraphPrefixUsesLiteralTextAndKeepsFormatting() {
        String button = "<b>同意 & Approve \"A\" 'B'</b>";
        String content = "<p><strong>需按规定</strong><a href=\"/detail\">流程</a>发起放款</p>";
        String result = SubtaskReplyOpinion.build(content, button, "combined");
        Element body = Jsoup.parseBodyFragment(result).body();
        assertEquals(1, body.childrenSize());
        assertEquals(button + "。需按规定流程发起放款", body.text());
        assertEquals(0, body.select("b").size());
        assertEquals("需按规定", body.select("strong").text());
        assertEquals("/detail", body.select("a").attr("href"));
        assertEquals(result, SubtaskReplyOpinion.build(content, button, "combined"));
    }

    /** 列表和表格前插入段落，图片等无文字的有效内容不会丢失。 */
    @Test
    public void structuralAndMediaContentRemainIntact() {
        for (String content : Arrays.asList("<ul><li>一</li></ul>", "<table><tbody><tr><td>意见</td></tr></tbody></table>")) {
            Element body = Jsoup.parseBodyFragment(SubtaskReplyOpinion.build(content, "同意", "combined")).body();
            assertEquals("p", body.child(0).normalName());
            assertEquals("同意。", body.child(0).text());
            assertEquals(Jsoup.parseBodyFragment(content).body().child(0).outerHtml(), body.child(1).outerHtml());
        }
        for (String content : Arrays.asList("<p><img src=\"image.png\"></p>", "<video src=\"a.mp4\"></video>",
                "<audio src=\"a.mp3\"></audio>", "<iframe src=\"about:blank\"></iframe>", "<svg></svg>", "<hr>")) {
            assertEquals(content, SubtaskReplyOpinion.build(content, null, "separate"));
            assertTrue(SubtaskReplyOpinion.build(content, "同意", "combined").contains("同意。"));
        }
    }

    /** 长意见完整保存，不影响前端既有摘要、折叠和延迟展开。 */
    @Test
    public void longContentRemainsComplete() {
        String content = "<p>" + "长内容".repeat(10000) + "</p>";
        assertEquals("<p>同意。" + "长内容".repeat(10000) + "</p>", SubtaskReplyOpinion.build(content, "同意", "combined"));
    }
}
