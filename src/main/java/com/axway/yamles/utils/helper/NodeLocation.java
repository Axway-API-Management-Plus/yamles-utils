package com.axway.yamles.utils.helper;

import java.util.Objects;

public class NodeLocation implements Comparable<NodeLocation> {
	public static final String ROOT = "/";

	private final String location;

	public static NodeLocation root() {
		return new NodeLocation();
	}

	private NodeLocation() {
		this.location = ROOT;
	}

	private NodeLocation(NodeLocation parent, String childName) {
		Objects.requireNonNull(parent, "parent of YAML location is null");
		Objects.requireNonNull(childName, "child name is null");
		if (childName.contains("/"))
			throw new IllegalArgumentException("child name contains '/' character: " + childName);

		if (parent.isRoot()) {
			this.location = parent.location + childName;
		} else {
			this.location = parent.location + "/" + childName;
		}
	}

	public boolean isRoot() {
		return this.location.equals(ROOT);
	}

	public NodeLocation child(String name) {
		return new NodeLocation(this, name);
	}

	@Override
	public int compareTo(NodeLocation o) {
		Objects.requireNonNull(o);
		return this.location.compareTo(o.location);
	}

	@Override
	public int hashCode() {
		return Objects.hash(location);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeLocation other = (NodeLocation) obj;
		return Objects.equals(location, other.location);
	}

	@Override
	public String toString() {
		return this.location;
	}
}
