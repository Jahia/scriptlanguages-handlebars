package org.jahia.services.render.scripting.handlebars;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.helper.I18nHelper;
import org.springframework.beans.factory.InitializingBean;
import com.github.jknack.handlebars.Handlebars;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.*;

/**
 * Created by loom on 04.05.15.
 */
public class HandlebarsScriptEngineFactory implements ScriptEngineFactory, InitializingBean {

    final Handlebars handlebars = new Handlebars();
    private List<String> extensions = new ArrayList<String>();
    private List<String> names = new ArrayList<String>();
    private List<String> mimeTypes = new ArrayList<String>();
    private String engineName = "Handlebars";
    private String engineVersion = "1.0";
    private String languageName = "handlebars";
    private String languageVersion = "2.1.0";
    private HandlebarsI18nSource i18nSource = new HandlebarsI18nSource();

    private Map<String,Helper<?>> helpers = new HashMap<String,Helper<?>>();

    public HandlebarsScriptEngineFactory() {
        extensions.add("hbs");
        names.add("handlebars");
    }

    public void setExtensions(List<String> extensions) {
        this.extensions = extensions;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public void setMimeTypes(List<String> mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    public void setEngineName(String engineName) {
        this.engineName = engineName;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public void setLanguageVersion(String languageVersion) {
        this.languageVersion = languageVersion;
    }

    public Handlebars getHandlebars() {
        return handlebars;
    }

    public Map<String, Helper<?>> getHelpers() {
        return helpers;
    }

    public void setHelpers(Map<String, Helper<?>> helpers) {
        this.helpers = helpers;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initializeTemplateEngine();
    }

    @Override
    public String getEngineName() {
        return engineName;
    }

    @Override
    public String getEngineVersion() {
        return engineVersion;
    }

    @Override
    public List<String> getExtensions() {
        return extensions;
    }

    @Override
    public List<String> getMimeTypes() {
        return mimeTypes;
    }

    @Override
    public List<String> getNames() {
        return names;
    }

    @Override
    public String getLanguageName() {
        return languageName;
    }

    @Override
    public String getLanguageVersion() {
        return languageVersion;
    }

    @Override
    public Object getParameter(String key) {
        return null;
    }

    @Override
    public String getMethodCallSyntax(String obj, String m, String... args) {
        return null;
    }

    @Override
    public String getOutputStatement(String toDisplay) {
        return null;
    }

    @Override
    public String getProgram(String... statements) {
        return null;
    }

    @Override
    public ScriptEngine getScriptEngine() {
        return new HandlebarsScriptEngine(this, handlebars);
    }

    public void initializeTemplateEngine() {
        I18nHelper.i18n.setSource(i18nSource);
        I18nHelper.i18nJs.setSource(i18nSource);

        for (Map.Entry<String,Helper<?>> helperEntry : helpers.entrySet()) {
            handlebars.registerHelper(helperEntry.getKey(), helperEntry.getValue());
        }

    }

}
