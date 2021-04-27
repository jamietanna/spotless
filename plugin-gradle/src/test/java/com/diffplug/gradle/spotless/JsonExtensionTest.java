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
package com.diffplug.gradle.spotless;

import java.io.IOException;

import org.junit.Test;

public class JsonExtensionTest extends GradleIntegrationHarness {
	@Test
	public void formatsWhenTargetsAreSpecified() throws IOException {
		setFile("build.gradle").toLines(
				"buildscript { repositories { mavenCentral() } }",
				"plugins {",
				"    id 'java'",
				"    id 'com.diffplug.spotless'",
				"}",
				"spotless {",
				"    json {" +
						"    target 'examples/**/*.json'" +
						"}",
				"}");
		setFile("src/main/resources/example.json").toResource("json/nestedObjectBefore.json");
		setFile("examples/main/resources/example.json").toResource("json/nestedObjectBefore.json");
		gradleRunner().withArguments("spotlessApply").build();
		assertFile("src/main/resources/example.json").sameAsResource("json/nestedObjectBefore.json");
		assertFile("examples/main/resources/example.json").sameAsResource("json/nestedObjectAfter.json");
	}
}
