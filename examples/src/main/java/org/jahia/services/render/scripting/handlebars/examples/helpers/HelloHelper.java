package org.jahia.services.render.scripting.handlebars.examples.helpers;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

import java.io.IOException;
/**
 * Created by loom on 07.05.15.
 */
public class HelloHelper implements Helper<String> {

    public HelloHelper() {
    }

    @Override
    public CharSequence apply(String context, Options options) throws IOException {
        String message = "Hello " + context;
        return message;
    }
}
