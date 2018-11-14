package org.vmol.app.util;

public class Atom {
	public String type;
	public int index;
	public double x;
	public double y;
	public double z;
	
	public Atom(String type, int index, double x, double y, double z) {
		this.type = type.toUpperCase();
		this.index = index;
		this.x = x;
		this.y = y;
		this.z = z;
	}
}
