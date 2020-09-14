/*
 * Copyright 2020 Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.graalvm.type;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.graalvm.domain.init.InitializationDescriptor;

public class CompilationHint {
	private String targetType;
	private Map<String, AccessDescriptor> specificTypes = new LinkedHashMap<>();
	public boolean follow = false;
	public boolean skipIfTypesMissing = false;
	private List<ProxyDescriptor> proxyDescriptor = new ArrayList<>();
	private List<ResourcesDescriptor> resourceDescriptors = new ArrayList<>();
	private List<InitializationDescriptor> initializationDescriptors = new ArrayList<>();
	private boolean applyToFunctional = true; // True as per default for this attribute in NativeImageHint
	
	public CompilationHint() {
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CompilationHint");
		if (targetType != null) {
			sb.append(" for ").append(targetType);
		}
		sb.append(":");
		sb.append(specificTypes);
		return sb.toString();
	}

	public void setTargetType(String targetTypename) {
		this.targetType = targetTypename;
	}

	public String getTargetType() {
		return targetType;
	}
	
	public Map<String, AccessDescriptor> getDependantTypes() {
		return specificTypes;
	}

	public void addDependantType(String className, Integer accessBits,List<MethodDescriptor> mds, List<FieldDescriptor> fds) {
		specificTypes.put(className, new AccessDescriptor(accessBits,mds,fds));
	}

	public void addDependantType(Class<?> clazz, Integer accessBits, List<MethodDescriptor> mds, List<FieldDescriptor> fds) {
		specificTypes.put(clazz.getName(), new AccessDescriptor(accessBits,mds,fds));
	}

	public void setAbortIfTypesMissing(Boolean b) {
		skipIfTypesMissing = b;
	}

	public boolean isAbortIfTypesMissing() {
		return skipIfTypesMissing;
	}

	public void setFollow(Boolean b) {
		follow = b;
	}

	public void addProxyDescriptor(ProxyDescriptor pd) {
		proxyDescriptor.add(pd);
	}
	
	public List<ProxyDescriptor> getProxyDescriptors() {
		return proxyDescriptor;
	}
	
	public void addResourcesDescriptor(ResourcesDescriptor rd) {
		resourceDescriptors.add(rd);
	}

	public void addInitializationDescriptor(InitializationDescriptor id) {
		initializationDescriptors.add(id);
	}
	
	public List<ResourcesDescriptor> getResourcesDescriptors() {
		return resourceDescriptors;
	}
	
	public void setApplyToFunctional(boolean b) {
		applyToFunctional = b;
	}

	public boolean applyToFunctional() {
		return applyToFunctional;
	}

	public List<InitializationDescriptor> getInitializationDescriptors() {
		return initializationDescriptors;
	}

}