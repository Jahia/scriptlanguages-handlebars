package org.jahia.services.render.scripting.handlebars;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.helper.I18nHelper;
import org.jahia.services.render.scripting.bundle.Configurable;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by loom on 04.05.15.
 */
public class HandlebarsScriptEngineFactory implements ScriptEngineFactory, Configurable {

    private Handlebars handlebars = new Handlebars();
    private Map<String, Helper<?>> helpers = new HashMap<>();
    private ServiceTracker<Helper, Helper> helperServiceTracker;

    public HandlebarsScriptEngineFactory() {
        HandlebarsI18nSource i18nSource = new HandlebarsI18nSource();
        I18nHelper.i18n.setSource(i18nSource);
        I18nHelper.i18nJs.setSource(i18nSource);
    }


    @Override
    public String getEngineName() {
        return "Handlebars";
    }

    @Override
    public String getEngineVersion() {
        return "1.0";
    }

    @Override
    public List<String> getExtensions() {
        return Collections.singletonList("hbs");
    }

    @Override
    public List<String> getMimeTypes() {
        return Collections.singletonList("text/x-handlebars-template");
    }

    @Override
    public List<String> getNames() {
        return Collections.singletonList("handlebars");
    }

    @Override
    public String getLanguageName() {
        return "handlebars";
    }

    @Override
    public String getLanguageVersion() {
        return "4.0.4";
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

    @Override
    public void configurePreRegistration(final Bundle bundle) {
        final BundleContext bundleContext = bundle.getBundleContext();

        ServiceTrackerCustomizer<Helper, Helper> serviceTrackerCustomizer = new ServiceTrackerCustomizer<Helper, Helper>() {
            @Override
            public Helper addingService(ServiceReference reference) {
                Helper<?> addedHelper = (Helper<?>) bundleContext.getService(reference);
                registerHelper(getHelperName(reference, addedHelper), addedHelper);
                return addedHelper;
            }

            @Override
            public void modifiedService(ServiceReference reference, Helper service) {
            }

            @Override
            public void removedService(ServiceReference reference, Helper service) {
                if (service != null) {
                    removeHelper(getHelperName(reference, service));
                }
            }
        };

        helperServiceTracker = new ServiceTracker<>(bundleContext, Helper.class, serviceTrackerCustomizer);
        helperServiceTracker.open();
    }

    @Override
    public void destroy(Bundle bundle) {
        helperServiceTracker.close();
    }

    private void removeHelper(String helperName) {
        helpers.remove(helperName);
        resetHandlebars(); // we need to reset as Handlebars doesn't provide any means to deregister helpers
    }

    private void resetHandlebars() {
        handlebars = new Handlebars();
        for (Map.Entry<String, Helper<?>> entry : helpers.entrySet()) {
            handlebars.registerHelper(entry.getKey(), entry.getValue());
        }
    }

    private void registerHelper(String helperName, Helper<?> addedHelper) {
        helpers.put(helperName, addedHelper);
        handlebars.registerHelper(helperName, addedHelper);
    }


    @Override
    public void configurePreScriptEngineCreation() {
        // nothing to do here
    }

    private String getHelperName(ServiceReference serviceReference, Helper<?> service) {
        final String name = (String) serviceReference.getProperty("name");
        return name != null ? name : service.getClass().getSimpleName();
    }
}
