package jEvAdoption_v1;

import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfaceShape;

import java.awt.Color;



import repast.simphony.visualization.gis3D.style.SurfaceShapeStyle;

/**
 * Style for ZoneAgents.
 * 
 * @author Lisha Sun
 *
 */
public class ParcelStyle implements SurfaceShapeStyle<Parcel>{
	
	public static final Color DefinedColor = new Color(255,255,255);

	@Override
	public SurfaceShape getSurfaceShape(Parcel object, SurfaceShape shape) {
		return new SurfacePolygon();
	}

	@Override
	public Color getFillColor(Parcel zone) {
		if (zone.getLandCode().equals("R")) 
			return Color.cyan;
		else if (zone.getTypeID().equals("34")||zone.getTypeID().equals("36")||zone.getTypeID().equals("46")||zone.getTypeID().equals("48")||zone.getTypeID().equals("77"))
			return Color.MAGENTA;
		else if (zone.getTypeID().equals("7")||zone.getTypeID().equals("9")||zone.getTypeID().equals("17")||zone.getTypeID().equals("65")||zone.getTypeID().equals("66")||zone.getTypeID().equals("74"))
			return DefinedColor;//getHSBColor(255, 102, 0); //light orange
		else if (zone.getLandCode().equals("G"))
			return Color.yellow;
		else
			return Color.gray;
	}

	@Override
	public double getFillOpacity(Parcel obj) {
		return 0.25;
	}

	/**
	 * If the zone has water then indicate with a BLUE outline.
	 */
	
	@Override
	public Color getLineColor(Parcel zone) {
		if (zone.getLandCode().equals("R")) 
			return Color.lightGray;
		else if (zone.getTypeID().equals("34")||zone.getTypeID().equals("36")||zone.getTypeID().equals("46")||zone.getTypeID().equals("48")||zone.getTypeID().equals("77"))
			return Color.lightGray;
		else if (zone.getLandCode().equals("E") || zone.getLandCode().equals("V"))
			return Color.LIGHT_GRAY;
		else if (zone.getLandCode().equals("G"))
			return Color.lightGray;
		else
			return Color.gray;
	}

	@Override
	public double getLineOpacity(Parcel obj) {
		return 1.0;
	}

	@Override
	public double getLineWidth(Parcel obj) {
		return 3;
	}
}