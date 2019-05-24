package org.codehaus.mojo.versions.change;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * Represents a change of an artifact's version.
 *
 * @author Stephen Connolly
 * @since 15-Sep-2010 14:48:10
 */
public final class VersionChange {
	private final String groupId;

	private final String artifactId;

	private final String oldVersion;

	private final String newVersion;

	public VersionChange(String groupId, String artifactId, String oldVersion, String newVersion) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.oldVersion = oldVersion;
		this.newVersion = newVersion;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getOldVersion() {
		return oldVersion;
	}

	public String getNewVersion() {
		return newVersion;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		VersionChange versionChange = (VersionChange) o;

		return compareAttributes(versionChange);
	}
	
	private boolean compareAttributes(VersionChange versionChange) {
		boolean toReturn = true;
		
		if (artifactId != null ? !artifactId.equals(versionChange.artifactId) : versionChange.artifactId != null) {
			toReturn = false;
		}
		else if (groupId != null ? !groupId.equals(versionChange.groupId) : versionChange.groupId != null) {
			toReturn = false;
		}
		else if (newVersion != null ? !newVersion.equals(versionChange.newVersion) : versionChange.newVersion != null) {
			toReturn = false;
		}
		else if (oldVersion != null ? !oldVersion.equals(versionChange.oldVersion) : versionChange.oldVersion != null) {
			toReturn = false;
		}
		return toReturn;
	}

	public int hashCode() {
		int result = groupId != null ? groupId.hashCode() : 0;
		result = 31 * result + (artifactId != null ? artifactId.hashCode() : 0);
		result = 31 * result + (oldVersion != null ? oldVersion.hashCode() : 0);
		result = 31 * result + (newVersion != null ? newVersion.hashCode() : 0);
		return result;
	}

	public String toString() {
		return "VersionChange(" + groupId + ':' + artifactId + ":" + oldVersion + "-->" + newVersion + ')';
	}
}
