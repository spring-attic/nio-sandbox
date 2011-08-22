package org.springframework.async;

import java.util.Map;

import groovy.util.BuilderSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class HttpBuilder extends BuilderSupport {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public Object get() {
		log.info("get");
		return this;
	}

	public Object bind(Integer port) {
		return this;
	}

	@Override protected void setParent(Object parent, Object child) {
		log.info("setParent/2 parent=" + parent + ", child=" + child);
	}

	@Override protected Object createNode(Object name) {
		log.info("createNode/1 " + name);
		if ("call".equals(name)) {
			return "call ";
		} else {
			return this;
		}
	}

	@Override protected Object createNode(Object name, Object value) {
		log.info("createNode/2 " + name + " " + value);
		return this;
	}

	@Override protected Object createNode(Object name, Map attributes) {
		log.info("createNode/2(Map) " + name + " " + attributes);
		return this;
	}

	@Override protected Object createNode(Object name, Map attributes, Object value) {
		log.info("createNode/3 " + name + " " + attributes + " " + value);
		return this;
	}

	@Override protected Object doInvokeMethod(String methodName, Object name, Object args) {

		log.info("doInvokeMethod/3 " + methodName + " " + name + " " + args);
		return super.doInvokeMethod(methodName, name, args);
	}

}
