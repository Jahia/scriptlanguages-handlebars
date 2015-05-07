package org.jahia.services.render.scripting.handlebars;

import com.github.jknack.handlebars.helper.I18nSource;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by loom on 05.05.15.
 */
public class HandlebarsI18nSource implements I18nSource {

    private static final Object[] EMPTY_MESSAGE_PARAMETERS = new Object[0];

    @Override
    public String[] keys(String baseName, Locale locale) {
        // here we use reflection to bypass class loading issues on the LocalizationContext class
        ResourceBundle resourceBundle = HandlebarsScriptEngine.getTemplateResourceBundle().get();
        if (resourceBundle == null) {
            return new String[0];
        }
        return resourceBundle.keySet().toArray(new String[resourceBundle.keySet().size()]);
    }

    @Override
    public String message(String key, Locale locale, Object... args) {

        // here we use reflection to bypass class loading issues on the LocalizationContext class
        ResourceBundle resourceBundle = HandlebarsScriptEngine.getTemplateResourceBundle().get();
        if (resourceBundle == null) {
            return null;
        }

        final String messageValue = resourceBundle.getString(key);

        final MessageFormat messageFormat = new MessageFormat(messageValue, locale);
        String message = messageFormat.format((args != null ? args : EMPTY_MESSAGE_PARAMETERS));

        return message;
    }

}
