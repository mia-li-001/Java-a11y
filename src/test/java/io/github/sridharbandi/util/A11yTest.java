package io.github.sridharbandi.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Template;
import io.github.sridharbandi.a11y.AxeTag;
import io.github.sridharbandi.a11y.Engine;
import io.github.sridharbandi.a11y.HTMLCS;
import io.github.sridharbandi.ftl.FtlConfig;
import io.github.sridharbandi.modal.htmlcs.Issues;
import io.github.sridharbandi.modal.htmlcs.Params;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.matches;

@ExtendWith(MockitoExtension.class)
public class A11yTest {

    @InjectMocks
    FtlConfig ftlConfig = FtlConfig.getInstance();
    @Mock
    JavascriptExecutor javascriptExecutor;
    @Mock
    WebDriver driver;
    @InjectMocks
    A11y a11y = new A11y(driver);

    private static final String URL_CONFIGURATION_TEST_SCRIPT = "return axeData('{\"standard\":null,\"ignoreCodes\":null,\"pageTitle\":null,\"rules\":{},\"tags\":[\"wcag2aa\",\"section508\"],\"scriptURL\":\"https://my-other-script-url\"}');async function axeData(params) {\n" + "\tconst obj = JSON.parse(params);\n" + "\tawait injectAxeScript(obj.scriptURL);\n" + "\tvar rules = obj.rules;\n" + "\tvar tags = obj.tags;\n" + "\tvar results = await runAxe(tags, rules);\n" + "\tresults.id = 'id_' + (Date.now().toString(36) + Math.random().toString(36).substr(2, 5));\n" + "\tresults.title = obj.pageTitle == null ? document.title : obj.pageTitle;\n" + "\tresults.dimension = window.innerWidth + ' X ' + window.innerHeight;\n" + "\tresults.device = device();\n" + "\tresults.browser = getBrowser();\n" + "\tresults.date = getFormattedDate();\n" + "\n" + "\tflattenTargetArrays(results.violations);\n" + "\tflattenTargetArrays(results.incomplete);\n" + "\tflattenTargetArrays(results.inapplicable);\n" + "\treturn results;\n" + "}\n" + "\n" + "function getBrowser() {\n" + "\tconst userAgent = navigator.userAgent;\n" + "\tvar browser = \"Unkown\";\n" + "\t// Detect browser name\n" + "\tbrowser = (/ucbrowser/i).test(userAgent) ? 'UCBrowser' : browser;\n" + "\tbrowser = (/edg/i).test(userAgent) ? 'Edge' : browser;\n" + "\tbrowser = (/googlebot/i).test(userAgent) ? 'GoogleBot' : browser;\n" + "\tbrowser = (/chromium/i).test(userAgent) ? 'Chromium' : browser;\n" + "\tbrowser = (/firefox|fxios/i).test(userAgent) && !(/seamonkey/i).test(userAgent) ? 'Firefox' : browser;\n" + "\tbrowser = (/; msie|trident/i).test(userAgent) && !(/ucbrowser/i).test(userAgent) ? 'IE' : browser;\n" + "\tbrowser = (/chrome|crios/i).test(userAgent) && !(/opr|opera|chromium|edg|ucbrowser|googlebot/i).test(userAgent) ? 'Chrome' : browser;\n" + "\tbrowser = (/safari/i).test(userAgent) && !(/chromium|edg|ucbrowser|chrome|crios|opr|opera|fxios|firefox/i).test(userAgent) ? 'Safari' : browser;\n" + "\tbrowser = (/opr|opera/i).test(userAgent) ? 'Opera' : browser;\n" + "\n" + "\t// detect browser version\n" + "\tswitch (browser) {\n" + "\t\tcase 'UCBrowser':\n" + "\t\t\treturn browser + ' ' + browserVersion(userAgent, /(ucbrowser)\\/([\\d\\.]+)/i);\n" + "\t\tcase 'Edge':\n" + "\t\t\treturn browser + ' ' + browserVersion(userAgent, /(edge|edga|edgios|edg)\\/([\\d\\.]+)/i);\n" + "\t\tcase 'GoogleBot':\n" + "\t\t\treturn browser + ' ' + browserVersion(userAgent, /(googlebot)\\/([\\d\\.]+)/i);\n" + "\t\tcase 'Chromium':\n" + "\t\t\treturn browser + ' ' + browserVersion(userAgent, /(chromium)\\/([\\d\\.]+)/i);\n" + "\t\tcase 'Firefox':\n" + "\t\t\treturn browser + ' ' + browserVersion(userAgent, /(firefox|fxios)\\/([\\d\\.]+)/i);\n" + "\t\tcase 'Chrome':\n" + "\t\t\treturn browser + ' ' + browserVersion(userAgent, /(chrome|crios)\\/([\\d\\.]+)/i);\n" + "\t\tcase 'Safari':\n" + "\t\t\treturn browser + ' ' + browserVersion(userAgent, /(safari)\\/([\\d\\.]+)/i);\n" + "\t\tcase 'Opera':\n" + "\t\t\treturn browser + ' ' + browserVersion(userAgent, /(opera|opr)\\/([\\d\\.]+)/i);\n" + "\t\tcase 'IE':\n" + "\t\t\tconst version = browserVersion(userAgent, /(trident)\\/([\\d\\.]+)/i);\n" + "\t\t\t// IE version is mapped using trident version\n" + "\t\t\t// IE/8.0 = Trident/4.0, IE/9.0 = Trident/5.0\n" + "\t\t\treturn version ? browser + ' ' + parseFloat(version) + 4.0 : browser + ' ' + 7.0;\n" + "\t\tdefault:\n" + "\t\t\treturn 'Unknown 0.0.0.0';\n" + "\t}\n" + "}\n" + "\n" + "function browserVersion(userAgent, regex) {\n" + "\treturn userAgent.match(regex) ? userAgent.match(regex)[2] : null;\n" + "}\n" + "\n" + "function device() {\n" + "\tvar width = window.innerWidth;\n" + "\tif (width < 768) {\n" + "\t\treturn 'Phone';\n" + "\t} else if (width < 992) {\n" + "\t\treturn 'Tablet';\n" + "\t} else if (width < 1200) {\n" + "\t\treturn 'Small Laptop';\n" + "\t} else {\n" + "\t\treturn 'Laptop/Desktop';\n" + "\t}\n" + "}\n" + "\n" + "function getFormattedDate() {\n" + "\tvar date = new Date();\n" + "\tvar formattedDate = date.getDate() + \"/\" + (date.getMonth() + 1) + \"/\" + date.getFullYear() + \" \" + date.getHours() + \":\" + date.getMinutes() + \":\" + date.getSeconds();\n" + "\treturn formattedDate;\n" + "}\n" + "\n" + "function runAxe(tags, rules) {\n" + "\treturn new Promise(function(resolve, reject) {\n" + "\t\taxe.run(document, {\n" + "\t\t\trunOnly: {\n" + "\t\t\t\ttype: 'tag',\n" + "\t\t\t\tvalues: tags\n" + "\t\t\t},\n" + "\t\t\tresultTypes: ['violations', 'incomplete', 'inapplicable'],\n" + "\t\t\trules: rules,\n" + "\t\t\treporter: 'v2'\n" + "\t\t}).then(results => resolve(results));\n" + "\t});\n" + "}\n" + "\n" + "function injectAxeScript(scriptURL) {\n" + "\treturn new Promise((resolve, reject) => {\n" + "\t\tconst script = document.createElement('script');\n" + "\t\tscript.src = scriptURL ?? \"https://cdn.jsdelivr.net/npm/axe-core@latest/axe.min.js\";\n" + "\t\tscript.addEventListener('load', resolve);\n" + "\t\tscript.addEventListener('error', e => reject(e.error));\n" + "\t\tdocument.head.appendChild(script);\n" + "\t});\n" + "}\n" + "\n" + "function flattenTargetArrays(resultsArray) {\n" + "\tif (Array.isArray(resultsArray)) {\n" + "\t\tresultsArray.forEach(result => {\n" + "\t\t\tif (result.nodes && Array.isArray(result.nodes)) {\n" + "\t\t\t\tresult.nodes.forEach(node => {\n" + "\t\t\t\t\tif (node.target && Array.isArray(node.target)) {\n" + "\t\t\t\t\t\tnode.target = node.target.flat();\n" + "\t\t\t\t\t}\n" + "\t\t\t\t});\n" + "\t\t\t}\n" + "\t\t});\n" + "\t}\n" + "}";
    private static final String EXECUTE_TEST_SCRIPT = "return getData('{\"standard\":\"WCAG2AA\",\"ignoreCodes\":null,\"pageTitle\":null,\"rules\":{},\"tags\":[\"wcag2aa\",\"section508\"],\"scriptURL\":null}');async function getData(params) {\n" + "    const obj = JSON.parse(params);\n" + "    const codes = obj.ignoreCodes;\n" + "\n" + "    await injectScript(obj.scriptURL);\n" + "    const results = await runHtmlCS(obj.standard, codes);\n" + "    const pageTitle = obj.pageTitle == null ? document.title : obj.pageTitle;\n" + "    return {\n" + "        errors: resultsCount(results, 1),\n" + "        warnings: resultsCount(results, 2),\n" + "        notices: resultsCount(results, 3),\n" + "        standard: obj.standard,\n" + "        date: getFormattedDate(),\n" + "        dimension: window.innerWidth + ' X ' + window.innerHeight,\n" + "        url: window.location.href,\n" + "        title: pageTitle,\n" + "        device: device(),\n" + "        browser: getBrowser(),\n" + "        results: results,\n" + "        id: 'id_' + (Date.now().toString(36) + Math.random().toString(36).substr(2, 5))\n" + "    };\n" + "}\n" + "\n" + "function resultsCount(results, type) {\n" + "    return results.filter(issue => issue.type == type).length;\n" + "}\n" + "\n" + "function getFormattedDate() {\n" + "    const date = new Date();\n" + "    const formattedDate = date.getDate() + \"/\" + (date.getMonth() + 1) + \"/\" + date.getFullYear() + \" \" + date.getHours() + \":\" + date.getMinutes() + \":\" + date.getSeconds();\n" + "    return formattedDate;\n" + "}\n" + "\n" + "function injectScript(scriptURL) {\n" + "    return new Promise((resolve, reject) => {\n" + "        const script = document.createElement('script');\n" + "        script.src = scriptURL ?? \"https://squizlabs.github.io/HTML_CodeSniffer/build/HTMLCS.js\";\n" + "        script.addEventListener('load', resolve);\n" + "        script.addEventListener('error', e => reject(e.error));\n" + "        document.head.appendChild(script);\n" + "    });\n" + "}\n" + "\n" + "function runHtmlCS(standard, codes) {\n" + "    return new Promise(function (resolve, reject) {\n" + "        window.HTMLCS.process(standard, window.document, function (error) {\n" + "            if (error) {\n" + "                return reject(error);\n" + "            }\n" + "            resolve(window.HTMLCS.getMessages().filter(item => !codes.includes(item.code)).map(processIssue));\n" + "        });\n" + "    });\n" + "}\n" + "\n" + "function processIssue(issue) {\n" + "    return {\n" + "        type: issue.type,\n" + "        code: issue.code,\n" + "        techniques: techniques(issue.code),\n" + "        msg: issue.msg,\n" + "        tag: issue.element.nodeName.toLowerCase(),\n" + "        element: htmlElement(issue.element)\n" + "    };\n" + "}\n" + "\n" + "function techniques(code) {\n" + "    if (code.includes('Section508')) {\n" + "        const split = code.split('.', 3);\n" + "        const para = split[1].toLowerCase();\n" + "        return ['1194.22 (' + para + ')'];\n" + "    }\n" + "    let result = code.match(/([A-Z]+[0-9]+(,[A-Z]+[0-9]+)*)/g) || [];\n" + "    if (result.length <= 1)\n" + "        return [];\n" + "    let list = result[1].split(',').map(linkTechnique);\n" + "    return list;\n" + "}\n" + "\n" + "function linkTechnique(technique) {\n" + "    //https://github.com/squizlabs/HTML_CodeSniffer/blob/aebdff845441ee99252a80d45d65f4ac27f998d7/Standards/WCAG2AAA/ruleset.js\n" + "    let prefix = '';\n" + "    if (technique.startsWith('ARIA')) {\n" + "        prefix = 'aria/';\n" + "    } else if (technique.startsWith('SCR')) {\n" + "        prefix = 'client-side-script/';\n" + "    } else if (technique.startsWith('C')) {\n" + "        prefix = 'css/';\n" + "    } else if (technique.startsWith('FLASH')) {\n" + "        prefix = 'flash/';\n" + "    } else if (technique.startsWith('F')) {\n" + "        prefix = 'failures/';\n" + "    } else if (technique.startsWith('G')) {\n" + "        prefix = 'general/';\n" + "    } else if (technique.startsWith('H')) {\n" + "        prefix = 'html/';\n" + "    } else if (technique.startsWith('PDF')) {\n" + "        prefix = 'pdf/';\n" + "    } else if (technique.startsWith('SVR')) {\n" + "        prefix = 'server-side-script/';\n" + "    } else if (technique.startsWith('SL')) {\n" + "        prefix = 'silverlight/';\n" + "    } else if (technique.startsWith('SM')) {\n" + "        prefix = 'smil/';\n" + "    } else if (technique.startsWith('T')) {\n" + "        prefix = 'text/';\n" + "    }\n" + "    return 'https://www.w3.org/WAI/WCAG21/Techniques/' + prefix + technique;\n" + "}\n" + "\n" + "function htmlElement(ele) {\n" + "    let a = \"\";\n" + "    if (ele.outerHTML) {\n" + "        const o = ele.cloneNode(!0);\n" + "        o.innerHTML = \"...\";\n" + "        a = o.outerHTML\n" + "    }\n" + "    return a;\n" + "}\n" + "\n" + "function getBrowser() {\n" + "    const userAgent = navigator.userAgent;\n" + "    let browser = \"Unkown\";\n" + "    // Detect browser name\n" + "    browser = (/ucbrowser/i).test(userAgent) ? 'UCBrowser' : browser;\n" + "    browser = (/edg/i).test(userAgent) ? 'Edge' : browser;\n" + "    browser = (/googlebot/i).test(userAgent) ? 'GoogleBot' : browser;\n" + "    browser = (/chromium/i).test(userAgent) ? 'Chromium' : browser;\n" + "    browser = (/firefox|fxios/i).test(userAgent) && !(/seamonkey/i).test(userAgent) ? 'Firefox' : browser;\n" + "    browser = (/; msie|trident/i).test(userAgent) && !(/ucbrowser/i).test(userAgent) ? 'IE' : browser;\n" + "    browser = (/chrome|crios/i).test(userAgent) && !(/opr|opera|chromium|edg|ucbrowser|googlebot/i).test(userAgent) ? 'Chrome' : browser;\n" + "    browser = (/safari/i).test(userAgent) && !(/chromium|edg|ucbrowser|chrome|crios|opr|opera|fxios|firefox/i).test(userAgent) ? 'Safari' : browser;\n" + "    browser = (/opr|opera/i).test(userAgent) ? 'Opera' : browser;\n" + "\n" + "    // detect browser version\n" + "    switch (browser) {\n" + "        case 'UCBrowser':\n" + "            return browser + ' ' + browserVersion(userAgent, /(ucbrowser)\\/([\\d\\.]+)/i);\n" + "        case 'Edge':\n" + "            return browser + ' ' + browserVersion(userAgent, /(edge|edga|edgios|edg)\\/([\\d\\.]+)/i);\n" + "        case 'GoogleBot':\n" + "            return browser + ' ' + browserVersion(userAgent, /(googlebot)\\/([\\d\\.]+)/i);\n" + "        case 'Chromium':\n" + "            return browser + ' ' + browserVersion(userAgent, /(chromium)\\/([\\d\\.]+)/i);\n" + "        case 'Firefox':\n" + "            return browser + ' ' + browserVersion(userAgent, /(firefox|fxios)\\/([\\d\\.]+)/i);\n" + "        case 'Chrome':\n" + "            return browser + ' ' + browserVersion(userAgent, /(chrome|crios)\\/([\\d\\.]+)/i);\n" + "        case 'Safari':\n" + "            return browser + ' ' + browserVersion(userAgent, /(safari)\\/([\\d\\.]+)/i);\n" + "        case 'Opera':\n" + "            return browser + ' ' + browserVersion(userAgent, /(opera|opr)\\/([\\d\\.]+)/i);\n" + "        case 'IE':\n" + "            const version = browserVersion(userAgent, /(trident)\\/([\\d\\.]+)/i);\n" + "            // IE version is mapped using trident version\n" + "            // IE/8.0 = Trident/4.0, IE/9.0 = Trident/5.0\n" + "            return version ? browser + ' ' + parseFloat(version) + 4.0 : browser + ' ' + 7.0;\n" + "        default:\n" + "            return 'Unknown 0.0.0.0';\n" + "    }\n" + "}\n" + "\n" + "function browserVersion(userAgent, regex) {\n" + "    return userAgent.match(regex) ? userAgent.match(regex)[2] : null;\n" + "}\n" + "\n" + "function device() {\n" + "    const width = window.innerWidth;\n" + "    if (width < 768) {\n" + "        return 'Phone';\n" + "    } else if (width < 992) {\n" + "        return 'Tablet';\n" + "    } else if (width < 1200) {\n" + "        return 'Small Laptop';\n" + "    } else {\n" + "        return 'Laptop/Desktop';\n" + "    }\n" + "}";
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    public void testScriptUrlConfiguration() throws IOException {
      when(javascriptExecutor.executeScript("return document.readyState")).thenReturn("complete");
      Params params = new Params();
      String alternativeScriptURL = "https://my-other-script-url";
      params.setScriptURL(alternativeScriptURL);
      when(javascriptExecutor.executeScript(URL_CONFIGURATION_TEST_SCRIPT)).thenReturn(null);
      a11y.execute(Engine.AXE, params);
      verify(javascriptExecutor).executeScript(matches(String.format("axeData\\(.*\"scriptURL\":\"%s\".*\\)", alternativeScriptURL)));
    }

    @Test
    public void testExecute() throws Exception {
        when(javascriptExecutor.executeScript("return document.readyState")).thenReturn("complete");
        Params params = new Params();
        params.setStandard(HTMLCS.WCAG2AA.name());
        when(javascriptExecutor.executeScript(EXECUTE_TEST_SCRIPT)).thenReturn(null);
        a11y.execute(Engine.HTMLCS, params);
        a11y.jsonReports(Engine.HTMLCS, Issues.class);
        assertTrue(FileUtils.deleteQuietly(Objects.requireNonNull(new File("./target/java-a11y/htmlcs/json").listFiles())[0]));
    }

    @Test
    public void testSave() throws IOException {
        Template template = ftlConfig.getTemplate("test.ftl");
        Map<String, Object> map = new HashMap<>();
        map.put("test", "a11y");
        a11y.save(template, map, "page", Engine.HTMLCS);
        Path path = Paths.get("./target/java-a11y/htmlcs/html/page.html");
        assertTrue(FileUtils.deleteQuietly(path.toFile()));
    }

    @Test
    public void testSerializeAxeTags() throws Exception {
        Params params = new Params();
        params.setTags(AxeTag.WCAG2AA, AxeTag.WCAG2A);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String serializedObject = mapper.writeValueAsString(params.getTags());
        String expectedObject = "[\"wcag2aa\",\"wcag2a\"]";
        assertEquals(serializedObject, expectedObject);
    }

    @Test
    public void testSerializeRules() throws Exception {
        Params params = new Params();
        params.disableRules("color-contrast, area-alt", "");
        params.enableRules("audio-caption");

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String serializedObject = mapper.writeValueAsString(params.getRules());
        String expectedObject = "{\"audio-caption\":{\"enabled\":true},\"color-contrast, area-alt\":{\"enabled\":false}}";
        assertEquals(serializedObject, expectedObject);
    }
}
