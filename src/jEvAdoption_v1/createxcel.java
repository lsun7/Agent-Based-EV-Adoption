package jEvAdoption_v1;

import java.io.*;
import java.lang.*;
import java.util.*;

public class createxcel {
	
	private Formatter x;
	public void openFile(int year, String path){
		try{
			FileWriter f = new FileWriter(path+year+".txt", true);
//			FileWriter f = new FileWriter("Q:\\My Drive\\Research\\EV Study\\Output\\Repast\\year\\year"+year+".txt", true);
			x=new Formatter(f);
//			System.out.println("file created");
		}
		catch(Exception e){
		System.out.println("you have an error");
		}
	}
	
	public void addRecords(double ID){
//		x.format("%.0f%n", ID);     
		x.format("%n%.2f%n", ID);  
	}
	
	public void addRecordsString(String ID){
//		x.format("%.0f%n", ID);     
		x.format("%n%s%n", ID);  
	}
	
	public void addCustomer(String ID, int Homecharger){
//		x.format("%d%n", Homecharger);     
		x.format("%n%s %d%n", ID, Homecharger);  
	}

	
	
	
	public void addStation(String ID, int station_count){
//		x.format("%.0f%n", ID);     
		x.format("%n%s %d%n", ID, station_count);  
	}
	
	public void adddouble(double ID){    
		x.format("%f ", ID);  
	}
	
	public void addint(int ID){    
		x.format("%d ", ID);  
	}
	
	public void addlong(long ID){    
		x.format("%d ", ID);  
	}
	
	public void addstring(String ID){    
		x.format("%s ", ID);  
	}
	
	public void addintend(int ID){    
		x.format("%d%n", ID);  
	}
	
	public void closeFile(){
		x.close();
	}
	
}
