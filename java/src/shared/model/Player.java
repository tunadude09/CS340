package shared.model;

import java.io.Serializable;

import shared.definitions.CatanColor;

/**
 * A player in the game. Can be an AI player or a human player
 * @author matthewcarlson, garrettegan
 *
 */
public class Player implements Serializable
{
	private static final long serialVersionUID = 3438289584063500359L;

	public String name;
	public CatanColor color;
	public Bank playerBank;
	private int playerIndex;
	private int playerID;
	private boolean isRobot = false;
	private int armySize;
	
	/**
	 * Creates a Player
	 * @param name the name of the player
	 * @param index the player's index
	 * @param playerColor the color of the player's game pieces
	 * @param isHuman whether the player is a human or computer player
	 */
	public Player(String name, int index, CatanColor playerColor, boolean isHuman)
	{
		this.isRobot = !isHuman;
		this.name = name;
		this.playerBank = new Bank();
		this.playerIndex = index;
		this.color = playerColor;
		this.playerID = -1;
		this.armySize = 0;
	}
	public Player(String name, int index, CatanColor playerColor, boolean isHuman, int ID)
	{
		this(name, index, playerColor, isHuman);
		this.playerID = ID;
	}
	
	/**
	 * Gets this player's army size
	 * @return the size of this player's army
	 */
	public int getArmySize()
	{
		return this.armySize;
	}
	
	/**
	 * Increments this player's army size
	 * @return this player's new army size
	 */
	public int incrementArmySize()
	{
		return ++armySize;
	}
	
	/**
	 * A simple function to determine if the player is controlled by AI
	 * @return true if is a robot
	 */
	public boolean isARobot()
	{
		return isRobot;
	}
	
	/**
	 * Returns the playerIndex so it can't be changed 
	 * @return 0 to 3
	 */
	public int playerIndex()
	{
		return playerIndex;
	}
	
	/**
	 * Returns the player ID
	 * @return
	 */
	public int playerID()
	{
		return playerID;
	}
	
	public int totalResources()
	{
		return this.playerBank.getResourceCount();
	}
	
	/**
	 * 
	 */
	public String toString()
	{
		return name + ":"+ color.name()+" "+playerID+"-"+playerIndex;
		
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((color == null) ? 0 : color.hashCode());
		result = prime * result + (isRobot ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + playerID;
		result = prime * result + playerIndex;
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Player))
			return false;
		Player other = (Player) obj;
		if (color != other.color)
			return false;
		if (isRobot != other.isRobot)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (playerID != other.playerID)
			return false;
		if (playerIndex != other.playerIndex)
			return false;
		return true;
	}
}
