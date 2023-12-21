/**
 * Copyright (c) 2023 Sebastian Thomschke and contributors.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * based on https://github.com/sebthom/extra-syntax-highlighting-eclipse-plugin/blob/main/plugin/updater
 */
package updater.utils;

import org.yaml.snakeyaml.DumperOptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactoryBuilder;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

/**
 * @author Sebastian Thomschke
 */
public abstract class ObjectMappers {

	public static final ObjectMapper JSON = new ObjectMapper() //
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	public static final ObjectMapper YAML;
	static {
		YAML = new ObjectMapper(new YAMLFactoryBuilder(new YAMLFactory()) //
				.dumperOptions(new DumperOptions() {
					{
						// :-( https://github.com/FasterXML/jackson-dataformats-text/issues/4
						setDefaultFlowStyle(DumperOptions.FlowStyle.AUTO);
					}
				}) //
				.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES) //
				.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER) //
				.build()) //
						.setSerializationInclusion(JsonInclude.Include.NON_EMPTY) //
						.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
	}
}
