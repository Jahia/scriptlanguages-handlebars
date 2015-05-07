package org.jahia.services.render.scripting.handlebars;

import javax.script.Bindings;
import javax.script.ScriptContext;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by loom on 04.05.15.
 */
public class HandlebarsContext {

    Map<String,Object> variables = new HashMap<String, Object>();
    Locale locale;

    public HandlebarsContext(ScriptContext scriptContext) {
        Bindings globalBindings = scriptContext.getBindings(ScriptContext.GLOBAL_SCOPE);
        Bindings engineBindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
        variables.putAll(globalBindings);
        variables.putAll(engineBindings);
        locale = (Locale) variables.get("currentLocale");
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public Locale getLocale() {
        return locale;
    }

}
