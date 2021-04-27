/*
 * Copyright 2021 DiffPlug
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diffplug.spotless.json;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import com.diffplug.spotless.FormatterFunc;
import com.diffplug.spotless.FormatterStep;
import com.diffplug.spotless.JarState;
import com.diffplug.spotless.Provisioner;

public final class JsonFormatterStep {
	private static final String MAVEN_COORDINATE = "org.json:json:";
	private static final String DEFAULT_VERSION = "20210307";

	public static FormatterStep create(Integer indent, Provisioner provisioner) {
		Objects.requireNonNull(provisioner, "provisioner cannot be null");
		return FormatterStep.createLazy("json", () -> new State(indent, provisioner), State::toFormatter);
	}

	private static final class State implements Serializable {
		private static final long serialVersionUID = 1L;

		private final int indentSpaces;
		private final JarState jarState;

		private State(Integer indent, Provisioner provisioner) throws IOException {
			this.indentSpaces = indent;
			this.jarState = JarState.from(MAVEN_COORDINATE + DEFAULT_VERSION, provisioner);
		}

		FormatterFunc toFormatter() {
			Method objectToString;
			Method arrayToString;
			Constructor<?> objectConstructor;
			Constructor<?> arrayConstructor;
			try {
				ClassLoader classLoader = jarState.getClassLoader();
				Class<?> jsonObject = classLoader.loadClass("org.json.JSONObject");
				Class<?>[] constructorArguments = new Class[]{String.class};
				objectConstructor = jsonObject.getConstructor(constructorArguments);
				objectToString = jsonObject.getMethod("toString", int.class);

				Class<?> jsonArray = classLoader.loadClass("org.json.JSONArray");
				arrayConstructor = jsonArray.getConstructor(constructorArguments);
				arrayToString = jsonArray.getMethod("toString", int.class);

			} catch (Exception e) {
				throw new IllegalStateException("There was a problem preparing org.json dependencies", e);
			}

			return s -> {
				String prettyPrinted = null;
				if (s.isEmpty()) {
					prettyPrinted = s;
				}
				if (s.startsWith("{")) {
					try {
						Object parsed = objectConstructor.newInstance(s);
						prettyPrinted = objectToString.invoke(parsed, indentSpaces) + "\n";
					} catch (InvocationTargetException ignored) {
						// ignore if we cannot convert to JSON string
					}
				}
				if (s.startsWith("[")) {
					try {
						Object parsed = arrayConstructor.newInstance(s);
						prettyPrinted = arrayToString.invoke(parsed, indentSpaces) + "\n";
					} catch (InvocationTargetException ignored) {
						// ignore if we cannot convert to JSON string
					}
				}

				if (prettyPrinted == null) {
					throw new AssertionError("Invalid JSON file provided");
				}

				return prettyPrinted;
			};
		}
	}

	private JsonFormatterStep() {
		// cannot be directly instantiated
	}
}
