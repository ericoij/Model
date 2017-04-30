package model;

public class WindVector {
	private double u, v;

	public WindVector(double ws, double wd) {
		this.u = -Math.abs(ws) * Math.sin(Math.PI / 180 * wd) * 0.514444;
		this.v = -Math.abs(ws) * Math.cos(Math.PI / 180 * wd) * 0.514444;
	}
	
	public WindVector(double u, double v, boolean b) {
		this.u = u;
		this.v = v;
		
		// TODO Auto-generated constructor stub
	}

	public void setU(double u) {
		this.u = u;
	}

	public void setV(double v) {
		this.v = v;
	}

	public double getU() {
		return this.u;
	}

	public double getV() {
		return this.v;
	}

	public WindVector getWindVector() {
		WindVector wv = new WindVector(this.u, this.v, true);
		return wv;
	}
	
	public void setWindVector(double u, double v){
		this.u = u;
		this.v = v;
	}

}
