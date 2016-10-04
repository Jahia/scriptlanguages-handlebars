# scriptlanguages-handlebars

A Jahia Digital Experience Manager (DX) module that provides Handlebars scripting language support. This module 
should mostly be considered as a proof-of-concept. It is not recommended to use this code in a production environment.
For more information about Jahia DX, please go to: https://www.jahia.com/platform/digital-experience-manager

## Features
- Makes it possible to use Handlebars as a scripting languages for Jahia views
- All standard Jahia variables are exposed as Handlebars variables, you can display node contents, etc.
- Custom helpers may be added inside custom modules !
- Full integration with Jahia's resource bundle system for messages

## Requirements
- Jahia 7.0.0.0 or more recent
- Maven 3.0+ for module compilation
- JDK 7 (maybe JDK 8 also works but not tested)

## Usage

1. Compile the whole project using : mvn clean install
2. Deploy the core/target/scriptlanguages-handlebars*.jar and examples/target/scriptlanguages-handlebars-examples*.jar modules
3. In Edit mode, add a Basic Content -> HandlebarsNode content node, and enter a value for the handlebarsText property.
The value of this property will be displayed by the node template written in Handlebars.

## Example template

You will find an example template in the following directory : examples/src/main/resources/jnt_handlebarsNode/html/handlebarsNode.hbs
with the following content :

    <div>
        <p>{{variables.currentNode.propertiesAsString.handlebarsText}}</p>
        <p>{{i18n "home.welcome"}}</p>
        <p>{{hello "World"}}</p>
    </div>

As you can see this will simply display the node property as well as display a message coming from the module's resource
bundle file located in examples/src/main/resources/resources/scriptlanguages-handlebars-examples.properties. It also supports custom helpers!

## Custom helpers

There is initial support for additional helpers implemented in other modules. In order for these to be recognized,
they must be registered as OSGi services using the com.github.jknack.handlebars.Helper interface. Here is an example using
a Spring descriptor file. Note that you must also provide a service property "name" that will be the name of the
helper used in Handlebar templates.

    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xmlns:osgi="http://www.springframework.org/schema/osgi"
           xsi:schemaLocation="http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans.xsd
           http://www.springframework.org/schema/osgi
           http://www.springframework.org/schema/osgi/spring-osgi.xsd">

        <bean id="helloHelper" class="org.jahia.services.render.scripting.handlebars.examples.helpers.HelloHelper" />

        <osgi:service id="helloHelperOsgiService" ref="helloHelper" interface="com.github.jknack.handlebars.Helper">
            <osgi:service-properties>
                <entry key="name" value="hello"/>
            </osgi:service-properties>
        </osgi:service>

    </beans>

You will this file inside the examples project in: examples/src/main/resources/META-INF/spring/mod-scriptlanguages-handlebars-examples.xml

## TODO
- Remove all hacks by modifying Jahia core to enable dynamic deployment of new scripting language support
- Remove hacks to access localization context in HandlebarsScriptEngine
- Test and improve overall performance, as not much caching is present
- Test, test, test :)
