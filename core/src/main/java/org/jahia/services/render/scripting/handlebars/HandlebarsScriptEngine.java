package org.jahia.services.render.scripting.handlebars;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.context.MethodValueResolver;
import org.apache.commons.io.IOUtils;

import javax.script.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ResourceBundle;

/**
 * Created by loom on 04.05.15.
 */
public class HandlebarsScriptEngine extends AbstractScriptEngine {

    HandlebarsScriptEngineFactory factory;
    Handlebars handlebars;
    static ThreadLocal<ResourceBundle> templateResourceBundle = new ThreadLocal<ResourceBundle>();

    public HandlebarsScriptEngine(HandlebarsScriptEngineFactory factory, Handlebars handlebars) {
        this.factory = factory;
        this.handlebars = handlebars;
    }

    public HandlebarsScriptEngine(Bindings n, HandlebarsScriptEngineFactory factory, Handlebars handlebars) {
        super(n);
        this.factory = factory;
        this.handlebars = handlebars;
    }

    public static ThreadLocal<ResourceBundle> getTemplateResourceBundle() {
        return templateResourceBundle;
    }

    @Override
    public Object eval(String script, ScriptContext scriptContext) throws ScriptException {
        HandlebarsContext templateContext = new HandlebarsContext(scriptContext);
        String result = null;
        try {
            Template template = handlebars.compileInline(script);
            templateResourceBundle.set(getResourceBundle(scriptContext));
            Context context = Context
                    .newBuilder(templateContext)
                    .resolver(
                            MapValueResolver.INSTANCE,
                            JavaBeanValueResolver.INSTANCE,
                            FieldValueResolver.INSTANCE,
                            MethodValueResolver.INSTANCE
                    ).build();
            result = template.apply(context);
            templateResourceBundle.remove();
            scriptContext.getWriter().append(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Object eval(Reader reader, ScriptContext scriptContext) throws ScriptException {
        HandlebarsContext templateContext = new HandlebarsContext(scriptContext);
        StringBuilder script = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(reader);
        try {
            while (bufferedReader.ready()) {
                script.append(bufferedReader.readLine());
                script.append("\n");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            IOUtils.closeQuietly(bufferedReader);
        }
        String result = null;
        try {
            Template template = handlebars.compileInline(script.toString());
            templateResourceBundle.set(getResourceBundle(scriptContext));
            Context context = Context
                    .newBuilder(templateContext)
                    .resolver(
                            MapValueResolver.INSTANCE,
                            JavaBeanValueResolver.INSTANCE,
                            FieldValueResolver.INSTANCE,
                            MethodValueResolver.INSTANCE
                    ).build();
            result = template.apply(context);
            templateResourceBundle.remove();
            scriptContext.getWriter().append(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public ResourceBundle getResourceBundle(ScriptContext scriptContext) {
        Object localizationContextObject = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE).get("javax.servlet.jsp.jstl.fmt.localizationContext" + ".request");
        try {
            Method getResourceBundleMethod = localizationContextObject.getClass().getMethod("getResourceBundle", null);
            ResourceBundle resourceBundle = (ResourceBundle) getResourceBundleMethod.invoke(localizationContextObject, null);
            return resourceBundle;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Bindings createBindings() {
        return new SimpleBindings();
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return factory;
    }
}
