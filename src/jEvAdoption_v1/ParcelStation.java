/**
 * 
 */
package jEvAdoption_v1;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;

/**
 * @author Lisha
 * 
 */
public class ParcelStation {

	private String LandCode;
	private String Node_ID;
	private double EV_count;
	private double charger_relied;
	
	
	public ParcelStation(String LandCode, String Node_ID, double EV_count, double charger_relied){
		this.LandCode = LandCode;
		this.Node_ID = Node_ID;
		this.EV_count=EV_count;
		this.charger_relied=charger_relied;
	}
	
	public String getLandCode() {
		return LandCode;
	}

	public void setLandCode(String ID) {
		this.LandCode = LandCode;
	}
	
	public double getEVcount() {
		return EV_count;
	}
	
	public double getrelied() {
		return charger_relied;
	}
	
//	@ScheduledMethod(start = 1, interval = 1, priority = 3.0)
//	public void run0() {
//		Context context = ContextUtils.getContext(this);
//		Geography geography = (Geography)context.getProjection("geography");
//		Geometry geom = geography.getGeometry(this);
//		
//		// try to use getObjectsWithin to accelerate the process
//		Coordinate coordinates = geom.getCoordinates()[0];
//		
//		// create envelope around Agent, 111km is 1 degree of latitude or longitude
//		// number of km per degree = ~111km (111.32 in google maps, but range varies between 110.567km at the equator and 111.699km at the poles)
//		// 1km in degree = 1 / 111.32km = 0.0089  // 1m in degree = 0.0089 / 1000 = 0.0000089
//		double DistMeter = 5000.0/2.0;
//		double tf = 0.0089/1000.0;
//		double Dist = DistMeter*tf;
//		Envelope envelope = new Envelope(coordinates.x + Dist, coordinates.x - Dist, coordinates.y + Dist, coordinates.y - Dist);
//		
//		double EV_count_temp = 0;
//		double charger_relied_temp = 0;
//		for(Object obj: geography.getObjectsWithin(envelope)) {
//			if (obj instanceof ResidentEV){
//				EV_count_temp = EV_count_temp +1;
//				if (((ResidentEV) obj).getHomecharger()==0){
//					charger_relied_temp = charger_relied_temp+1;
//				}
//			}
//		}
//		EV_count = EV_count_temp;
//		charger_relied = charger_relied_temp;
//		
//		System.out.printf("EV customers nearby %f%n" , EV_count);
//		System.out.printf("EV customers relied sole on the station %f%n" , charger_relied);
//	}
	
}
