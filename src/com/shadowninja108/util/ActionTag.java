package com.shadowninja108.util;

import java.lang.reflect.Method;

import org.jdom2.Element;

import com.shadowninja108.interpret.Interpreter;

public class ActionTag {
	public boolean ready;
	public Method m;
	public Element node;
	public Interpreter interpreter;

	public static ActionTag makeTag(String method, Element node, Interpreter interpreter) {
		ActionTag tag = new ActionTag();
		try {
			tag.m = Interpreter.class.getMethod(method, new Class<?>[] { Element.class });
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		tag.ready = false;
		tag.node = node;
		tag.interpreter = interpreter;
		return tag;
	}
}
