# Agent-Based-EV-Adoption
Repast Simphony Agent Based EV Adoption Model

Agents in the model: EV, ICE, Land Parcels, Charging Stations, EV Market

Run Priority:
1. EVMarket.Java
2. Parcel.Java
3. ResidentEV.Java
4. Resident.Java

ParcelStation.Java is for charging station agent

Node.Java is for distirbution feeder model

Contextbuilder.Java include GIS projection, initiate agents and butch runs 

createxcel.Java export excel results

Note that GIS and feeder files are removed since these data are prioritory. 

Files you will need in order to run the study: 
- Nodes.shp for electricity grid nodes
- Parcels.shp for land parcels 
