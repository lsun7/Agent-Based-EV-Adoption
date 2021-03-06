package jEvAdoption_v1;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.BasicWWTexture;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.PatternFactory;
import gov.nasa.worldwind.render.WWTexture;
import repast.simphony.visualization.gis3D.PlaceMark;
import repast.simphony.visualization.gis3D.style.MarkStyle;

/**
 * Style for ResidentEVs. 
 * 
 * @author Lisha Sun
 *
 */
public class ResidentEVStyle implements MarkStyle<ResidentEV>{
	
	private Offset labelOffset;
	
	private Map<String, WWTexture> textureMap;
	
	public ResidentEVStyle(){
		

		labelOffset = new Offset(1.2d, 0.6d, AVKey.FRACTION, AVKey.FRACTION);
		

		textureMap = new HashMap<String, WWTexture>();
		
		BufferedImage image = PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE, 
				new Dimension(50, 50), 0.7f,  Color.red);
		
		textureMap.put("red circle", new BasicWWTexture(image));
		
		image = PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE, 
				new Dimension(50, 50), 0.7f,  Color.green);
		
		textureMap.put("green circle", new BasicWWTexture(image));
	}
	
	/**
	 * The PlaceMark is a WWJ PointPlacemark implementation with a different 
	 *   texture handling mechanism.  All other standard WWJ PointPlacemark 
	 *   attributes can be changed here.  PointPlacemark label attributes could be
	 *   set here, but are also available through the MarkStyle interface.
	 *   
	 *   @see gov.nasa.worldwind.render.PointPlacemark for more info.
	 */
	@Override
	public PlaceMark getPlaceMark(ResidentEV agent, PlaceMark mark) {
		
		// PlaceMark is null on first call.
		if (mark == null)
			mark = new PlaceMark();
		

		mark.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
		mark.setLineEnabled(false);
		
		return mark;
	}
	

	@Override
	public double getElevation(ResidentEV agent) {
			return 0;
	}
	
	/**
	 * Here we set the appearance of the ResidentEV.  
	 */
	@Override
	public WWTexture getTexture(ResidentEV agent, WWTexture texture) {
	
		// WWTexture is null on first call.
		
		if (agent.getHomecharger() == 1){
			return textureMap.get("red circle");
		}
		else{
			return textureMap.get("red circle");
		}
	}
	
	/**
	 * Scale factor for the mark size.
	 */
	@Override
	public double getScale(ResidentEV agent) {
		return 0.2;

	}

	@Override
	public double getHeading(ResidentEV agent) {
		return 0;
	}
	
	/**
	 * The agent on-screen label.  Return null instead of empty string "" for better
	 *   performance.
	 */
	@Override
	public String getLabel(ResidentEV agent) {
//		return "" + agent.getWaterRate();
		return null;
	}

	@Override
	public Color getLabelColor(ResidentEV agent) {
		
		if (agent.getHomecharger() == 1){
			return Color.red;
		}
		else{
			return Color.red;
		}
	}
	
	/**
	 * Return an Offset that determines the label position relative to the mark 
	 * position.  @see gov.nasa.worldwind.render.Offset
	 * 
	 */
	@Override
	public Offset getLabelOffset(ResidentEV agent) {
		return labelOffset;
	}

	@Override
	public Font getLabelFont(ResidentEV obj) {
		return null;
	}

	/** Width of the line that connects an elevated mark with the surface.  Use
	 *    a value of 0 to disable line drawing.
	 *   
	 */
	@Override
	public double getLineWidth(ResidentEV agent) {
		return 0;
//		if (agent.getPhCount() == 3){
//			return 2;
//		}
//		else{
//			return 0;
//		}
	}

	@Override
	public Material getLineMaterial(ResidentEV obj, Material lineMaterial) {
		if (lineMaterial == null){
			lineMaterial = new Material(Color.RED);
		}
		
		return lineMaterial;
	}

	@Override
	public Offset getIconOffset(ResidentEV obj) {
		return Offset.CENTER;
	}
}
