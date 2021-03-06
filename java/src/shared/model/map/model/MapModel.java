package shared.model.map.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import shared.definitions.*;
import shared.model.map.*;
import shared.model.map.handlers.*;
import shared.model.map.objects.*;

/**
 * The Map Model stores all information about the map. This data includes information
 * about hexes, edges, vertices, and the robber. Location data is stored in a X, Y grid
 * format.
 * @author Jonathan Sadler
 *
 */
public class MapModel implements IMapModel 
{	
	private static final long serialVersionUID = 5280325974057938585L;
	
	private boolean setup;
	
	private Map<Integer, List<Hex>> values;
	
	private HexHandler hexes;
	private EdgeHandler edges;
	private VertexHandler vertices;	
	private PortHandler ports;
	
	private CatanColor longestRoadColor;
	
	private Robber robber;
	
	/**
	 * Creates a new Map Model object.
	 */
	MapModel()
	{
		setup = false;
		
		values = new HashMap<Integer, List<Hex>>();
		
		hexes = new HexHandler();
		edges = new EdgeHandler();
		vertices = new VertexHandler();
		ports = new PortHandler();
		
		longestRoadColor = null;
	}
	
	public boolean IsSetup()
	{
		return setup;
	}
	
	@Override
	public void SetupPhase(boolean setup)
	{
		this.setup = setup;
	}
	
	@Override
	public boolean IsRobberInitialized()
	{
		return robber != null;
	}
	
	@Override
	public boolean LongestRoadExists()
	{
		return longestRoadColor != null;
	}
	
	@Override
	public boolean ContainsEdge(Coordinate p1, Coordinate p2)
	{
		return edges.ContainsEdge(p1, p2);
	}

	@Override
	public boolean ContainsVertex(Coordinate point)
	{
		return vertices.ContainsVertex(point);
	}
	
	@Override
	public boolean ContainsHex(Coordinate point)
	{
		return hexes.ContainsHex(point);
	}
	
	@Override
	public boolean CanPlaceRoad(Coordinate p1, Coordinate p2, CatanColor color)
	{	
		try
		{
			//Edge doesn't exist
			if (!edges.ContainsEdge(p1, p2))
				return false;
			
			Edge edge = edges.GetEdge(p1, p2);
			
			//Road already placed
			if (edge.doesRoadExists())
				return false;
			
			//Village satisfies end
			if (VillagesSatisfyRoadPlacement(edge, color))
				return true;
			
			if (setup)
				return false;
			
			//Road satisfies end
			return RoadsSatisfyRoadPlacement(edge, color);
		} 
		catch (MapException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public boolean CanPlaceSettlement(Coordinate point, CatanColor color)
	{
		try
		{
			//Invalid vertex
			if (!vertices.ContainsVertex(point))
				return false;
			
			Vertex vertex = vertices.GetVertex(point);
			
			//Vertex contains a piece already
			if (vertex.getType() != PieceType.NONE)
				return false;
			
			Iterator<Vertex> neighbors = GetVertices(vertex);
			
			boolean roadSatisfied = false;
			while(neighbors.hasNext())
			{
				Vertex neighbor = neighbors.next();
				
				//Vertex has a neighbor
				if (neighbor.getType() != PieceType.NONE)
					return false;
				
				Edge edge = edges.GetEdge(point, neighbor.getPoint());
				
				//Marks if the settlement is on a road.
				if (!setup && edge.doesRoadExists() && edge.getColor() == color)
					roadSatisfied = true;
				//The settlement is not supposed to be on a road, yet it is.
				else if (setup && edge.doesRoadExists())
					return false;
			}
			
			//The method won't get to this point if other conditions aren't satisfied.
			//The final factor is if the road is satisfied.
			return roadSatisfied || setup;
		} 
		catch (MapException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public boolean CanPlaceCity(Coordinate point, CatanColor color)
	{	
		if (!vertices.ContainsVertex(point))
			return false;
		
		try
		{
			Vertex vertex = vertices.GetVertex(point);
			
			return vertex.getType() == PieceType.SETTLEMENT && 
					vertex.getColor() == color;
		}
		catch (MapException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public boolean CanPlaceRobber(Coordinate point)
	{	
		if (!hexes.ContainsHex(point))
			return false;
		
		try
		{
			Hex hex = hexes.GetHex(point);
			
			if (robber.isOnHex(hex))
				return false;
			
			return hex.getType() != HexType.WATER;
		}
		catch (MapException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public boolean CanPlacePip(Coordinate point)
	{
		if(!hexes.ContainsHex(point))
			return false;
		
		try
		{
			Hex hex = hexes.GetHex(point);
			
			return hex.getType() != HexType.WATER && hex.getType() != HexType.DESERT;
		}
		catch (MapException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public void PlaceHex(HexType type, Coordinate point) throws MapException
	{
		hexes.AddHex(new Hex(type, point));
	}
	
	@Override
	public void PlaceRoad(Coordinate p1, Coordinate p2, CatanColor color) throws MapException
	{	
		if (CanPlaceRoad(p1, p2, color))
			edges.AddRoad(p1, p2, color);
		else
			throw new MapException("Attempt to place road where not allowed");
		
		longestRoadColor = new RoadCounter(this).Count();
	}
	
	@Override
	public void PlaceSettlement(Coordinate point, CatanColor color) throws MapException
	{
		if (CanPlaceSettlement(point, color))
			vertices.SetSettlement(point, color);
		else
			throw new MapException("Attempt to place settlement where not allowed");
		
		longestRoadColor = new RoadCounter(this).Count();
	}
	
	@Override
	public void PlaceCity(Coordinate point, CatanColor color) throws MapException
	{
		if (CanPlaceCity(point, color))
			vertices.SetCity(point, color);
		else
			throw new MapException("Attempt to place city where not allowed");
	}
	
	@Override
	public void PlacePort(PortType type, Coordinate hexCoordinate, 
			Coordinate edgeStart, Coordinate edgeEnd) throws MapException
	{
		try 
		{
			Hex hex = hexes.GetHex(hexCoordinate);
			Edge edge = edges.GetEdge(edgeStart, edgeEnd);
			
			ports.AddPort(type, edge, hex);
		} 
		catch (MapException e)
		{
			throw new MapException("Attempt to add port to non-existent vertex", e);
		}
	}
	
	@Override
	public void PlaceRobber(Coordinate point) throws MapException
	{
		Hex hex = hexes.GetHex(point);
		
		if (hex.getType() == HexType.WATER)
			throw new MapException("Don't drown Trogdor!");
		
		if (robber == null)
			robber = new Robber(hex);
		else
			robber.setRobber(hex);
	}
	
	@Override
	public void PlacePip(int value, Coordinate point) throws MapException
	{
		//Ignore any invalid roles. The provided server gives us -1 for the
		//desert hex.
		if (value < 2 || value > 12)
			return;
		
		Hex hex = hexes.GetHex(point);
		
		if (values.containsKey(value))
		{
			//If a hex contains a value, we are simply changing the value.
			if (values.get(value).contains(hex))
			{
				values.get(value).remove(hex);
				PlacePip(value, point);
			}
			else
			{
				values.get(value).add(hex);
			}
		}
		else
		{
			List<Hex> tempList = new ArrayList<Hex>();
			tempList.add(hex);
			values.put(value, tempList);
		}
	}
	
	@Override
	public Hex GetHex(Coordinate point) throws MapException
	{
		return hexes.GetHex(point);
	}
	
	@Override
	public Iterator<Hex> GetHexes()
	{
		return hexes.GetAllHexes();
	}
	
	@Override
	public Edge GetEdge(Coordinate p1, Coordinate p2) throws MapException
	{
		return edges.GetEdge(p1, p2);
	}
	
	@Override
	public Iterator<Edge> GetEdges()
	{
		return edges.GetAllEdges();
	}
	
	@Override
	public Vertex GetVertex(Coordinate point) throws MapException
	{
		return vertices.GetVertex(point);
	}
	
	@Override
	public Iterator<Vertex> GetVertices()
	{
		return vertices.GetVerticies();
	}
	
	@Override
	public Iterator<Vertex> GetVertices(Hex hex)
	{
		List<Vertex> verticiesAlongHex = new ArrayList<Vertex>(6);
		
		try
		{
			if (vertices.ContainsVertex(hex.getTopLeftCoordinate()))
				verticiesAlongHex.add(vertices.GetVertex(hex.getTopLeftCoordinate()));
			if (vertices.ContainsVertex(hex.getLeftCoordinate()))
				verticiesAlongHex.add(vertices.GetVertex(hex.getLeftCoordinate()));
			if (vertices.ContainsVertex(hex.getBottomLeftCoordinate()))
				verticiesAlongHex.add(vertices.GetVertex(hex.getBottomLeftCoordinate()));
			if (vertices.ContainsVertex(hex.getBottomRightCoordinate()))
				verticiesAlongHex.add(vertices.GetVertex(hex.getBottomRightCoordinate()));
			if (vertices.ContainsVertex(hex.getRightCoordinate()))
				verticiesAlongHex.add(vertices.GetVertex(hex.getRightCoordinate()));
			if (vertices.ContainsVertex(hex.getTopRightCoordinate()))
				verticiesAlongHex.add(vertices.GetVertex(hex.getTopRightCoordinate()));
		}
		catch (MapException e)
		{
			e.printStackTrace();
		}
		
		return java.util.Collections.unmodifiableList(verticiesAlongHex).iterator();
	}
	
	@Override
	public Iterator<Vertex> GetVertices(Vertex vertex)
	{
		List<Vertex> neighbors = new ArrayList<Vertex>(3);
		
		try
		{
			if (vertices.ContainsVertex(vertex.getPoint().GetNorth()))
				neighbors.add(vertices.GetVertex(vertex.getPoint().GetNorth()));
			if (vertices.ContainsVertex(vertex.getPoint().GetSouth()))
				neighbors.add(vertices.GetVertex(vertex.getPoint().GetSouth()));
			
			Coordinate sideNeighbor;
			if (vertex.getPoint().isRightHandCoordinate())
				sideNeighbor = vertex.getPoint().GetEast();
			else
				sideNeighbor = vertex.getPoint().GetWest();
			
			if (vertices.ContainsVertex(sideNeighbor))
				neighbors.add(vertices.GetVertex(sideNeighbor));
		}
		catch (MapException e)
		{
			//This shouldn't occur since we are checking.
			e.printStackTrace();
		}
		
		return java.util.Collections.unmodifiableList(neighbors).iterator();
	}
	
	@Override
	public Iterator<CatanColor> GetOccupiedVertices(Coordinate hexPoint)
	{
		Set<CatanColor> color = new HashSet<CatanColor>();
		
		try 
		{
			Hex hex = GetHex(hexPoint);
			
			Iterator<Vertex> vertices = GetVertices(hex);
			while (vertices.hasNext())
			{
				Vertex vertex = vertices.next();
				
				if (vertex.getType() != PieceType.NONE)
					color.add(vertex.getColor());
			}
		}
		catch (MapException e) 
		{
			//Shouldn't occur
			e.printStackTrace();
		}
		
		return java.util.Collections.unmodifiableSet(color).iterator();
	}
	
	@Override
	public Iterator<Entry<Edge, Hex>> GetPorts()
	{
		return ports.GetPorts();
	}
	
	@Override
	public Iterator<PortType> GetPorts(CatanColor color)
	{
		List<PortType> portTypes = new ArrayList<PortType>(4);
		
		try
		{
			Iterator<Entry<Edge, Hex>> portList = ports.GetPorts();
			
			while (portList.hasNext())
			{
				Entry<Edge, Hex> port = portList.next();
				
				Edge edge = port.getKey();
				Vertex v1 = vertices.GetVertex(edge.getStart());
				Vertex v2 = vertices.GetVertex(edge.getEnd());
				
				if (v1.getType() != PieceType.NONE && v1.getColor() == color)
					portTypes.add(port.getValue().getPort());
				else if (v2.getType() != PieceType.NONE && v2.getColor() == color)
					portTypes.add(port.getValue().getPort());
			}
		}
		catch (MapException e)
		{
			e.printStackTrace();
			//Shouldn't occur
		}
		
		return java.util.Collections.unmodifiableList(portTypes).iterator();
	}
	
	@Override
	public Hex GetRobberLocation()
	{
		return robber.GetHex();
	}
	
	@Override
	public Iterator<HexType> GetResources(Coordinate point)
	{
		List<HexType> resources = new ArrayList<HexType>();
		
		try
		{
			if (point.isRightHandCoordinate())
			{
				if (hexes.ContainsHex(point.GetWest()))
					resources.add(hexes.GetHex(point.GetWest()).getType());
				if (hexes.ContainsHex(point.GetNorth()))
					resources.add(hexes.GetHex(point.GetNorth()).getType());
				if (hexes.ContainsHex(point.GetSouth()))
					resources.add(hexes.GetHex(point.GetSouth()).getType());
			}
			else
			{
				if (hexes.ContainsHex(point))
					resources.add(hexes.GetHex(point).getType());
				if (hexes.ContainsHex(point.GetNorthWest()))
					resources.add(hexes.GetHex(point.GetNorthWest()).getType());
				if (hexes.ContainsHex(point.GetSouthWest()))
					resources.add(hexes.GetHex(point.GetSouthWest()).getType());
			}
		}
		catch (MapException e)
		{
			e.printStackTrace();
		}
		
		return java.util.Collections.unmodifiableList(resources).iterator();
	}
	
	@Override
	public Iterator<Entry<Integer, List<Hex>>> GetPips()
	{
		return java.util.Collections.unmodifiableSet(values.entrySet()).iterator();
	}
	
	@Override
	public CatanColor GetLongestRoadColor() throws MapException
	{
		if (LongestRoadExists())
			return longestRoadColor;
		else
			throw new MapException("Longest road doesn't exist.");
	}
	
	@Override
	public Iterator<Transaction> GetTransactions(int role)
	{
		List<Transaction> transactions = new ArrayList<Transaction>();
		
		try
		{
			Iterator<Hex> hexes = GetHex(role);
			while (hexes.hasNext())
			{
				Hex hex = hexes.next();
				
				Iterator<Vertex> vertices = GetVertices(hex);
				while (vertices.hasNext())
				{
					Vertex vertex = vertices.next();
					
					if (vertex.getType() == PieceType.NONE)
						continue;
					
					HexType hexType = hex.getType();
					PieceType pieceType = vertex.getType();
					CatanColor color = vertex.getColor();
					Transaction transaction = new Transaction(hexType, pieceType, color);
					
					transactions.add(transaction);
				}
			}
		}
		catch (MapException e)
		{
			//Don't need to do anything.
			//Simply means the role didn't exist, so we don't form any
			//transactions.
		}
		
		return java.util.Collections.unmodifiableList(transactions).iterator();
	}
	
	/**
	 * Gets all the hexes associated with the dice role.
	 * @param role The combined value of the dice.
	 * @return The associated hex.
	 * @throws MapException Thrown if the value doesn't exist.
	 */
	private Iterator<Hex> GetHex(int role) throws MapException
	{
		if (!values.containsKey(role))
			throw new MapException("Role value does not exist.");
		else
			return java.util.Collections.unmodifiableList(values.get(role)).iterator();
	}
	
	/**
	 * Gets the edges surrounding a vertex.
	 * @param vertex The vertex.
	 * @return The surrounding edges.
	 */
	private Iterator<Edge> GetEdges(Vertex vertex)
	{
		List<Edge> associatedEdges = new ArrayList<Edge>(3);
		
		Iterator<Vertex> vertices = GetVertices(vertex);
		while(vertices.hasNext())
		{
			Vertex neighbor = vertices.next();
			
			Coordinate mainPoint = vertex.getPoint();
			Coordinate neighborPoint = neighbor.getPoint();
			try
			{
				if (edges.ContainsEdge(mainPoint, neighborPoint))
					associatedEdges.add(edges.GetEdge(mainPoint, neighborPoint));
			}
			catch (MapException e)
			{
				//Shouldn't happen
				e.printStackTrace();
			}
		}
		
		return java.util.Collections.unmodifiableList(associatedEdges).iterator();
	}
	
	private boolean RoadsSatisfyRoadPlacement(Edge edge, CatanColor color) throws MapException
	{
		Vertex vStart = vertices.GetVertex(edge.getStart());
		if (vStart.getType() == PieceType.NONE || vStart.getColor() == color)
		{
			Iterator<Edge> startEdges = GetEdges(vStart);
			while(startEdges.hasNext())
			{
				Edge edgeToCheck = startEdges.next();
				if (edgeToCheck.doesRoadExists() && edgeToCheck.getColor() == color)
					return true;
			}
		}
		
		Vertex vEnd = vertices.GetVertex(edge.getEnd());
		if (vEnd.getType() == PieceType.NONE || vEnd.getColor() == color)
		{
			Iterator<Edge> endEdges = GetEdges(vEnd);
			while(endEdges.hasNext())
			{
				Edge edgeToCheck = endEdges.next();
				if (edgeToCheck.doesRoadExists() && edgeToCheck.getColor() == color)
					return true;
			}
		}
		
		return false;
	}
	
	private boolean VillagesSatisfyRoadPlacement(Edge edge, CatanColor color) throws MapException
	{
		Vertex vStart = vertices.GetVertex(edge.getStart());
		if (vStart.getType() != PieceType.NONE && vStart.getColor() == color)
		{
			if (setup && !IsGoodSetup(vStart))
				return false;
			
			return true;
		}
		
		Vertex vEnd = vertices.GetVertex(edge.getEnd());
		if (vEnd.getType() != PieceType.NONE && vEnd.getColor() == color)
		{
			if (setup && !IsGoodSetup(vEnd))
				return false;
			
			return true;
		}
		
		return false;
	}

	private boolean IsGoodSetup(Vertex vertex)
	{
		try
		{
			Iterator<Vertex> surrounding = GetVertices(vertex);
			while (surrounding.hasNext())
			{
				Vertex end = surrounding.next();
				
				Edge edge = GetEdge(vertex.getPoint(), end.getPoint());
				
				if (edge.doesRoadExists())
					return false;
			}
			
			return true;
		}
		catch (MapException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + edges.hashCode();
		result = prime * result + hexes.hashCode();
		result = prime * result + ((longestRoadColor == null) ? 0 : longestRoadColor.hashCode());
		result = prime * result + ports.hashCode();
		result = prime * result + ((robber == null) ? 0 : robber.hashCode());
		result = prime * result + values.hashCode();
		result = prime * result + vertices.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof MapModel))
			return false;
		
		MapModel other = (MapModel) obj;
		
		if (!edges.equals(other.edges))
			return false;
		if (!hexes.equals(other.hexes))
			return false;
		if (longestRoadColor != other.longestRoadColor)
			return false;
		if (!ports.equals(other.ports))
			return false;
		if (robber == null)
		{
			if (other.robber != null)
				return false;
		}
		else if (!robber.equals(other.robber))
			return false;
		if (!values.equals(other.values))
			return false;
		if (!vertices.equals(other.vertices))
			return false;
		
		return true;
	}

	
}
