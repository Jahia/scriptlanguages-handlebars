package org.jahia.services.render.scripting.handlebars;

import com.github.jknack.handlebars.Helper;
import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.jahia.services.render.scripting.ScriptFactory;
import org.jahia.services.render.scripting.bundle.BundleJSR223ScriptFactory;
import org.jahia.services.render.scripting.bundle.BundleScriptResolver;
import org.jahia.utils.ScriptEngineUtils;
import org.ops4j.pax.swissbox.extender.BundleURLScanner;
import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is mostly a hack to register the script engine into the global ScriptEngineManager. Also it doesn't support
 * unregistration since the ScriptEngineManager doesn't support it.
 *
 * Possibly a better solution would be to implement inside Jahia's core the following use of the OsgiScriptEngineManager
 * as illustrated here :
 * http://svn.apache.org/repos/asf/felix/trunk/mishell/src/main/java/org/apache/felix/mishell/OSGiScriptEngineManager.java
 * but this requires modifying the ScriptEngineUtils class inside Jahia.
 *
 * Also, this hack doesn't support unregistering the ScriptEngineFactory so it is not clear what will happen if we
 * re-register the same factory multiple times. We should also de-register it normally when the module is stopped.
 */
public class Activator implements InitializingBean, BundleContextAware, DisposableBean, BundleListener {

    ScriptEngineFactory scriptEngineFactory;
    BundleScriptResolver bundleScriptResolver;
    BundleJSR223ScriptFactory bundleJSR223ScriptFactory;
    BundleContext bundleContext;
    BundleURLScanner bundleURLScanner;
    ServiceTracker<Helper,Helper> helperServiceTracker;

    Map<Bundle,List<URL>> allBundleScripts = new HashMap<Bundle,List<URL>>();

    public void setScriptEngineFactory(ScriptEngineFactory scriptEngineFactory) {
        this.scriptEngineFactory = scriptEngineFactory;
    }

    public void setBundleScriptResolver(BundleScriptResolver bundleScriptResolver) {
        this.bundleScriptResolver = bundleScriptResolver;
    }

    public void setBundleJSR223ScriptFactory(BundleJSR223ScriptFactory bundleJSR223ScriptFactory) {
        this.bundleJSR223ScriptFactory = bundleJSR223ScriptFactory;
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ScriptEngineUtils scriptEngineUtils = ScriptEngineUtils.getInstance();
        Field scriptEngineManagerField = scriptEngineUtils.getClass().getDeclaredField("scriptEngineManager");
        scriptEngineManagerField.setAccessible(true);
        ScriptEngineManager scriptEngineManager = (ScriptEngineManager) scriptEngineManagerField.get(scriptEngineUtils);
        for (String extension : scriptEngineFactory.getExtensions()) {
            scriptEngineManager.registerEngineExtension(extension, scriptEngineFactory);
        }
        for (String mimeType : scriptEngineFactory.getMimeTypes()) {
            scriptEngineManager.registerEngineMimeType(mimeType, scriptEngineFactory);
        }
        for (String name : scriptEngineFactory.getNames()) {
            scriptEngineManager.registerEngineName(name, scriptEngineFactory);
        }
        if (bundleScriptResolver != null) {

            List<String> scriptExtensionsOrdering = bundleScriptResolver.getScriptExtensionsOrdering();
            for (String extension : scriptEngineFactory.getExtensions()) {
                if (!scriptExtensionsOrdering.contains(extension)) {
                    scriptExtensionsOrdering.add(extension);
                }
            }
            bundleScriptResolver.setScriptExtensionsOrdering(scriptExtensionsOrdering);

            Field scriptFactoryMapField = bundleScriptResolver.getClass().getDeclaredField("scriptFactoryMap");
            scriptFactoryMapField.setAccessible(true);
            Map<String,ScriptFactory> scriptFactoryMap = (Map<String,ScriptFactory>) scriptFactoryMapField.get(bundleScriptResolver);
            for (String extension : scriptEngineFactory.getExtensions()) {
                scriptFactoryMap.put(extension, bundleJSR223ScriptFactory);
            }

            // now we need to activate the bundle script scanner inside of newly deployed or existing bundles
            // register view script observers
            bundleURLScanner = new BundleURLScanner("/", "*.hbs", true);
            addBundleScripts(bundleContext.getBundle());

            // as we are starting up we insert all the bundle scripts for all the deployed bundles.
            for (Bundle bundle : bundleContext.getBundles()) {
                if ((bundle.getBundleContext() != null && !bundle.getBundleContext().equals(bundleContext)) || (bundle.getBundleContext() == null) ) {
                    addBundleScripts(bundle);
                }
            }

        }

        ServiceTrackerCustomizer<Helper,Helper> serviceTrackerCustomizer = new ServiceTrackerCustomizer<Helper, Helper>() {
            @Override
            public Helper addingService(ServiceReference reference) {
                Helper<?> addedHelper = (Helper<?>) bundleContext.getService(reference);
                ((HandlebarsScriptEngineFactory)scriptEngineFactory).getHelpers().put(getHelperName(reference, addedHelper), addedHelper);
                ((HandlebarsScriptEngineFactory)scriptEngineFactory).initializeTemplateEngine();
                return addedHelper;
            }

            @Override
            public void modifiedService(ServiceReference reference, Helper service) {

            }

            @Override
            public void removedService(ServiceReference reference, Helper service) {
                if (service != null) {
                    ((HandlebarsScriptEngineFactory) scriptEngineFactory).getHelpers().remove(getHelperName(reference, service));
                }
                ((HandlebarsScriptEngineFactory)scriptEngineFactory).initializeTemplateEngine();
            }
        };

        helperServiceTracker = new ServiceTracker<Helper, Helper>(bundleContext, Helper.class, serviceTrackerCustomizer);
        helperServiceTracker.open();

    }

    private void addBundleScripts(Bundle bundle) {
        List<URL> bundleScripts = bundleURLScanner.scan(bundle);
        allBundleScripts.put(bundle, bundleScripts);
        bundleScriptResolver.addBundleScripts(bundle, bundleScripts);
    }

    private void removeBundleScripts(Bundle bundle) {
        List<URL> bundleScripts = allBundleScripts.get(bundle);
        if (bundleScripts != null && bundleScripts.size() > 0) {
            bundleScriptResolver.removeBundleScripts(bundle, bundleScripts);
        }
    }

    @Override
    public void destroy() throws Exception {

        helperServiceTracker.close();

        for (Bundle bundle : allBundleScripts.keySet()) {
            removeBundleScripts(bundle);
        }

        List<String> scriptExtensionsOrdering = bundleScriptResolver.getScriptExtensionsOrdering();
        for (String extension : scriptEngineFactory.getExtensions()) {
            if (scriptExtensionsOrdering.contains(extension)) {
                scriptExtensionsOrdering.remove(extension);
            }
        }
        bundleScriptResolver.setScriptExtensionsOrdering(scriptExtensionsOrdering);

        Field scriptFactoryMapField = bundleScriptResolver.getClass().getDeclaredField("scriptFactoryMap");
        scriptFactoryMapField.setAccessible(true);
        Map<String,ScriptFactory> scriptFactoryMap = (Map<String,ScriptFactory>) scriptFactoryMapField.get(bundleScriptResolver);
        for (String extension : scriptEngineFactory.getExtensions()) {
            scriptFactoryMap.remove(extension);
        }

    }

    @Override
    public void bundleChanged(BundleEvent event) {
        final Bundle bundle = event.getBundle();
        switch (event.getType()) {
            case BundleEvent.STARTED :
                addBundleScripts(bundle);
                break;
            case BundleEvent.STOPPED :
                removeBundleScripts(bundle);
                break;
        }
    }

    public String getHelperName(ServiceReference serviceReference, Helper<?> service) {
        if (serviceReference.getProperty("name") != null) {
            return (String) serviceReference.getProperty("name");
        }
        return service.getClass().getSimpleName();
    }
}
