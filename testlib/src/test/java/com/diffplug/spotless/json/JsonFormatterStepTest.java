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

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;

import com.diffplug.spotless.FormatterStep;
import com.diffplug.spotless.SerializableEqualityTester;
import com.diffplug.spotless.StepHarness;
import com.diffplug.spotless.TestProvisioner;

public class JsonFormatterStepTest {

	private static final int INDENT = 4;

	private final FormatterStep step = JsonFormatterStep.create(INDENT, TestProvisioner.mavenCentral());
	private final StepHarness stepHarness = StepHarness.forStep(step);

	@Test
	public void cannotProvidedNullProvisioner() {
		assertThatThrownBy(() -> JsonFormatterStep.create(INDENT, null)).isInstanceOf(NullPointerException.class).hasMessage("provisioner cannot be null");
	}

	@Test
	public void handlesSingletonObject() throws Exception {
		doWithResource(stepHarness, "singletonObject");
	}

	@Test
	public void handlesSingletonObjectWithArray() throws Exception {
		doWithResource(stepHarness, "singletonObjectWithArray");
	}

	@Test
	public void handlesNestedObject() throws Exception {
		doWithResource(stepHarness, "nestedObject");
	}

	@Test
	public void handlesSingletonArray() throws Exception {
		doWithResource(stepHarness, "singletonArray");
	}

	@Test
	public void handlesEmptyFile() throws Exception {
		doWithResource(stepHarness, "empty");
	}

	@Test
	public void handlesComplexNestedObject() throws Exception {
		doWithResource(stepHarness, "cucumberJsonSample");
	}

	@Test
	public void handlesObjectWithNull() throws Exception {
		doWithResource(stepHarness, "objectWithNull");
	}

	@Test
	public void handlesInvalidJson() {
		assertThatThrownBy(() -> doWithResource(stepHarness, "invalidJson")).isInstanceOf(AssertionError.class).hasMessage("Invalid JSON file provided");
	}

	@Test
	public void handlesNotJson() {
		assertThatThrownBy(() -> doWithResource(stepHarness, "notJson")).isInstanceOf(AssertionError.class).hasMessage("Invalid JSON file provided");
	}

	@Test
	public void canSetCustomIndentationLevel() throws Exception {
		FormatterStep step = JsonFormatterStep.create(6, TestProvisioner.mavenCentral());
		StepHarness stepHarness = StepHarness.forStep(step);

		String before = "json/singletonArrayBefore.json";
		String after = "json/singletonArrayAfter6Spaces.json";
		stepHarness.testResource(before, after);
	}

	@Test
	public void canSetIndentationLevelTo0() throws Exception {
		FormatterStep step = JsonFormatterStep.create(0, TestProvisioner.mavenCentral());
		StepHarness stepHarness = StepHarness.forStep(step);

		String before = "json/singletonArrayBefore.json";
		String after = "json/singletonArrayAfter0Spaces.json";
		stepHarness.testResource(before, after);
	}

	@Test
	public void indentAsNullUsesTabs() throws Exception {
		FormatterStep step = JsonFormatterStep.create(null, TestProvisioner.mavenCentral());
		StepHarness stepHarness = StepHarness.forStep(step);

		String before = "json/singletonArrayBefore.json";
		String after = "json/singletonArrayAfterTabs.json";
		stepHarness.testResource(before, after);
	}

	@Test
	public void equality() {
		new SerializableEqualityTester() {
			int spaces = 0;

			@Override
			protected void setupTest(API api) {
				// no changes, are the same
				api.areDifferentThan();

				// with different spacing
				spaces = 1;
				api.areDifferentThan();
			}

			@Override
			protected FormatterStep create() {
				return JsonFormatterStep.create(spaces, TestProvisioner.mavenCentral());
			}
		}.testEquals();
	}

	private static void doWithResource(StepHarness stepHarness, String name) throws Exception {
		String before = String.format("json/%sBefore.json", name);
		String after = String.format("json/%sAfter.json", name);
		stepHarness.testResource(before, after);
	}
}
