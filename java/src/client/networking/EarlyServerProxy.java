package client.networking;

import java.util.List;

import shared.definitions.AIType;
import shared.definitions.CatanColor;
import shared.definitions.ResourceType;
import shared.locations.EdgeLocation;
import shared.locations.HexLocation;
import shared.locations.VertexLocation;
import shared.networking.transport.NetGame;
import shared.networking.transport.NetGameModel;

public interface EarlyServerProxy
{
	/**
	 * Logs the specified user in and returns a User object if the user was
	 * successfully logged in. If not, a null user is returned
	 * @param username The username of the user to log in
	 * @param password The password of the user to log in
	 * @throws ServerProxyException if the user could not be logged in
	 * @return boolean true when the user could be logged in, false when he couldn't
	 */
	public boolean loginUser(String username, String password) throws ServerProxyException;
	
	/**
	 * Registers a user with the specified username and password
	 * @param username The username of the user to be registered
	 * @param password The password of the user to be registered
	 * @throws ServerProxyException if this user could not be registered
	 * @return boolean true when the user could be registered, false when he couldn't
	 */
	public boolean registerUser(String username, String password) throws ServerProxyException;
	
	/**
	 * Fetches a list of ongoing games
	 * @return a list of all ongoing games on the server
	 */
	public List<NetGame> listGames() throws ServerProxyException;
	
	
	/**
	 * Gets the current user's name
	 * @return
	 * @throws ServerProxyException if not logged in
	 */
	public String getUserName() throws ServerProxyException;
	
	/**
	 * Gets the player ID
	 * @return
	 */
	public int getUserId();
	
	/**
	 * Creates a game on the server
	 * @param randomTiles Whether the server should place random tiles
	 * @param randomNumbers Whether the server should place random number chits
	 * @param randomPorts Whether the server should place random ports
	 * @param name The name of the game to be created
	 * @return a ProxyGame object that represents the game that was created
	 * @throws ServerProxyException if something goes wrong
	 */
	public NetGame createGame(boolean randomTiles, boolean randomNumbers, boolean randomPorts, String name) throws ServerProxyException;
	
	/**
	 * 
	 * @param color the color the user wishes to represent him
	 * @throws ServerProxyException if something goes wrong
	 */
	public void joinGame(int id, CatanColor color) throws ServerProxyException; 

	/**
	 * Retrieves the game model from the server
	 * @return a NetGameModel object
	 * @throws ServerProxyException if something goes wrong
	 */
	public NetGameModel getGameModel() throws ServerProxyException;
	
	/**
	 * Adds an AI to the game
	 * @param aiType the type of AI the user wishes to add
	 * @throws ServerProxyException if there is no logged in user
	 */
	public void addAI(AIType aiType) throws ServerProxyException;
	
	/**
	 * 
	 * @return a list of supported AI player types
	 * @throws ServerProxyException if there is no logged in user
	 */
	public List<AIType> listAI() throws ServerProxyException;
	
	
	/**
	 * Sends a chat message from the specified user to the server
	 * @param content The content of the chat message
	 * @return A NetGameModel object
	 * @throws ServerProxyException if there is no logged in user
	 */
	public NetGameModel sendChat(String content) throws ServerProxyException;
	
	/**
	 * Reports the result of a dice roll to the server
	 * @param roll The result of the user's roll
	 * @return a NetGameModel object that reflects the current state of the Game
	 * @throws ServerProxyException if there is no logged in user
	 */
	public NetGameModel rollNumber(int roll) throws ServerProxyException;
	
	/**
	 * Notifies the server that the user has decided to rob another player
	 * @param victimIndex The index of the victim of the user's robbing
	 * @param location The new hex location of the robber
	 * @return a NetGameModel object that reflects the current state of the Game
	 * @throws ServerProxyException if there is no logged in user
	 */
	public NetGameModel robPlayer(int victimIndex, HexLocation location) throws ServerProxyException;
	
	/**
	 * Notifies the server that the user has finished his turn
	 * @param user The User who has finished his turn
	 * @return a NetGameModel object that reflects the current state of the Game
	 * @throws ServerProxyException if there is no logged in user
	 */
	public NetGameModel finishTurn() throws ServerProxyException;
	
	/**
	 * Notifies the server that the user has bought a development card
	 * @return a NetGameModel object that reflects the current state of the Game
	 * @throws ServerProxyException if there is no logged in user
	 */
	public NetGameModel buyDevCard() throws ServerProxyException;
	
	/**
	 * Notifies the server that the user has played a year of plenty card
	 * @param resource1 The first chosen resource
	 * @param resource2 The second chosen resource
	 * @return a NetGameModel object that reflects the current state of the Game
	 * @throws ServerProxyException if there is no logged in user
	 */
	public NetGameModel yearOfPlentyCard(ResourceType resource1, ResourceType resource2) throws ServerProxyException;
	
	/**
	 * Notifies the server that the user has played a road building card
	 * @param location1 The EdgeLocation location of the first road
	 * @param location2 The EdgeLocation location of the second card
	 * @return a NetGameModel object that reflects the current state of the Game
	 * @throws ServerProxyException if there is no logged in user
	 */
	public NetGameModel roadBuildingCard(EdgeLocation location1, EdgeLocation location2) throws ServerProxyException;
	
	/**
	 * Notifies the server that the user has played a soldier card
	 * @param victimIndex The index of the player who is being robbed
	 * @param hexLocation The new hex location of the robber
	 * @return a NetGameModel object that reflects the current state of the Game
	 * @throws ServerProxyException if there is no logged in user
	 */
	public NetGameModel soldierCard(int victimIndex, HexLocation hexLocation) throws ServerProxyException;
	
	/**
	 * Notifies the server that the user has played a monopoly card
	 * @param resource The resource that the player has chosen to have a monopoly on
	 * @return a NetGameModel object that reflects the current state of the Game
	 * @throws ServerProxyException if there is no logged in user
	 */
	public NetGameModel monopolyCard(ResourceType resource) throws ServerProxyException;
	
	/**
	 * Notifies the server that the user has played a monument card
	 * @return a NetGameModel object that reflects the current state of the Game
	 * @throws ServerProxyException if there is no logged in user
	 */
	public NetGameModel monumentCard() throws ServerProxyException;
	
	/**
	 * Notifies the server that the user has decided to build a road
	 * @param edgeLocation The edge location of the road
	 * @param free Whether this road was free (only true during the set up phases)
	 * @return a NetGameModel object that reflects the current state of the Game
	 * @throws ServerProxyException if there is no logged in user
	 */
	public NetGameModel buildRoad(EdgeLocation edgeLocation, boolean free) throws ServerProxyException;
	
	/**
	 * Notifies the server that the user has decided to build a settlement
	 * @param vertexLocation The vertex location of the settlement
	 * @param free Whether this settlement was free (only true during the set up phases)
	 * @return a NetGameModel object that reflects the current state of the Game
	 * @throws ServerProxyException if there is no logged in user
	 */
	public NetGameModel buildSettlement(VertexLocation vertexLocation, boolean free) throws ServerProxyException;
	
	/**
	 * Notifies the server that the user has decided to build a city
	 * @param vertexLocation The vertex location of the city
	 * @return a NetGameModel object that reflects the current state of the Game
	 * @throws ServerProxyException if there is no logged in user
	 */
	public NetGameModel buildCity(VertexLocation vertexLocation) throws ServerProxyException;
	
	/**
	 * Notifies the server that the user has decided to offer a trade to another player
	 * @param resourceList A list of the resources that the user wishes to trade, in this order: brick, ore, sheep,
	 * wheat, and wood. Negative values denotes that this user will give these resources, and positive values denote 
	 * the resources that will be received
	 * @param receiver The index of the player who will receive this trade offer
	 * @return a NetGameModel object that reflects the current state of the Game
	 * @throws ServerProxyException if there is no logged in user
	 */
	public NetGameModel offerTrade(List<Integer> resourceList, int receiver) throws ServerProxyException;
	
	/**
	 * Notifies the server whether this player has decided to accept or reject a trade
	 * @param willAccept true if the user will accept the trade, false if not
	 * @return a NetGameModel object that reflects the current state of the Game
	 * @throws ServerProxyException if there is no logged in user
	 */
	public NetGameModel acceptTrade(boolean willAccept) throws ServerProxyException;
	
	/**
	 * Notifies the server that the user has decided to initiate a maritime trade
	 * @param ratio The ratio of resources demanded by the harbor
	 * @param inputResource The resources traded away
	 * @param outputResource The resource received
	 * @return a NetGameModel object that reflects the current state of the Game
	 * @throws ServerProxyException if there is no logged in user
	 */
	public NetGameModel maritimeTrade(int ratio, ResourceType inputResource, ResourceType outputResource) throws ServerProxyException;
	
	/**
	 * Notifies the server that the user has discarded cards
	 * @param resourceList A list of integers that denotes how many of each resource the user will discard. The
	 * order is brick, ore, sheep, wheat, and wood
	 * @return a NetGameModel object that reflects the current state of the Game
	 * @throws ServerProxyException if there is no logged in user
	 */
	public NetGameModel discardCards(List<Integer> resourceList) throws ServerProxyException;
	
}
