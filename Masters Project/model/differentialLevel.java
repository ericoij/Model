package model;

public class differentialLevel {
	
	double dudt;
	double dvdt;
	double dTdt;
	double dphidt;
	
	public differentialLevel(double dudt, double dvdt, double dTdt){
		this.dudt = dudt;
		this.dvdt = dvdt;
		this.dTdt = dTdt;
	}
	
	public differentialLevel getDifferentials(){
		
		return this;
		
		
	}
	
	
	
	
	
}
