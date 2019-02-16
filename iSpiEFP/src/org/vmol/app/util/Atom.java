package org.vmol.app.util;

public class Atom {
	public String type;
	public int index;
	public double charge;
	public double x;
	public double y;
	public double z;
	
	public Atom(String type, int index, int charge, double x, double y, double z) {
		this.type = type.toUpperCase();
		this.charge = Double.valueOf(charge);
		this.index = index;
		this.x = x;
		this.y = y;
		this.z = z;
	}
}
