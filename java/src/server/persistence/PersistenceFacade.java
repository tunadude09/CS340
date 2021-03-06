package server.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import server.commands.ICommand;
import server.model.*;
import shared.networking.SerializationUtils;

/**
 * Handles saving server data for persistence purposes.
 * @author Jonathan Sadler
 *
 */
public class PersistenceFacade 
{
	private static PersistenceFacade facade;
	
	/**
	 * Initializes the persistence setup.
	 * @param type Specifies the persistence method to use.
	 * @throws PersistenceException Thrown if unable to load persistence method.
	 */
	public static void Initialize(String type, int commandLength) throws PersistenceException
	{
		if (facade != null)
			throw new PersistenceException("Persistence already initialized");
		
		facade = new PersistenceFacade(type, commandLength);
	}
	
	/**
	 * Gets the persistence facade.
	 * @return The persistence facade.
	 */
	public static PersistenceFacade GetPersistence()
	{
		return facade;
	}
	
	private PersistenceHandler handler;
	private int commandLength;
	
	private PersistenceFacade(String type, int commandLength) throws PersistenceException
	{
		this.handler = new PersistenceHandler(type);
		this.commandLength = commandLength;
	}
	
	/**
	 * Adds a command to be saved.
	 * @param gameID The gameID of the command.
	 * @param command The command to be saved.
	 * @return True if adding was successful. Else false (in this case, attempt updating the game).
	 * @throws PersistenceException Thrown if issues occur adding the command
	 */
	public boolean AddCommand(int gameID, ICommand command) throws PersistenceException 
	{
		IPersistenceProvider provider = handler.GetPlugin();
		
		boolean success = false;
		try
		{
			provider.StartTransaction();
			
			ICommandDAO commandDAO = provider.GetCommandDAO();
			
			if (commandDAO.GetCommandCount(gameID) <= commandLength)
			{
				String serializedCommand = Serialize(command);
				
				provider.GetCommandDAO().AddCommand(gameID, serializedCommand);
				success = true;
			}
			else
			{
				success = false;
			}
			
			provider.EndTransaction(true);
		}
		catch (PersistenceException | IOException e)
		{
			provider.EndTransaction(false);
			throw new PersistenceException("Error saving command", e);
		}
		
		return success;
	}
	
	/**
	 * Adds a game to be saved.
	 * @param sgm The server game manager to add. 
	 * @throws PersistenceException Thrown if issues occur adding the game.
	 */
	public void AddGame(ServerGameManager sgm) throws PersistenceException
	{
		IPersistenceProvider provider = handler.GetPlugin();
		
		try
		{
			provider.StartTransaction();
			
			int gameID = sgm.GetGameID();
			
			String blob = Serialize(sgm);
			provider.GetGameDAO().AddGame(gameID, blob);
			
			provider.EndTransaction(true);
		}
		catch (PersistenceException | IOException e)
		{
			provider.EndTransaction(false);
			throw new PersistenceException("Error saving game", e);
		}
	}
	
	/**
	 * Adds a user to be be saved.
	 * @param player The player to save.
	 * @throws PersistenceException Thrown if issues occur adding the user.
	 */
	public void AddUser(ServerPlayer player) throws PersistenceException
	{
		IPersistenceProvider provider = handler.GetPlugin();
		
		try
		{
			provider.StartTransaction();
			
			int id = player.GetID();
			String username = player.GetName();
			String password = player.GetPassword();
			
			provider.GetUserDAO().AddUser(id, username, password);
			
			provider.EndTransaction(true);
		}
		catch (PersistenceException e)
		{
			provider.EndTransaction(false);
			throw e;
		}
	}
	
	/**
	 * Get all the commands that haven't been applied.
	 * @return The commands for all the games.
	 * @throws PersistenceException Thrown if issues occur getting the commands.
	 */
	public List<ICommand> GetAllCommands() throws PersistenceException
	{
		IPersistenceProvider provider = handler.GetPlugin();
		
		List<String> commands;
		try
		{
			provider.StartTransaction();
			commands = provider.GetCommandDAO().GetCommands();
			provider.EndTransaction(true);
		}
		catch (PersistenceException e)
		{
			provider.EndTransaction(false);
			throw e;
		}
			
		List<ICommand> convertedCommands = new ArrayList<ICommand>(commands.size());
		for (String command : commands)
		{
			try
			{
				ICommand convertedCommand = Deserialize(command, ICommand.class);
				convertedCommands.add(convertedCommand);
			}
			catch (IOException | ClassNotFoundException e)
			{
				e.printStackTrace();
			}
		}
		
		return convertedCommands;
	}
	
	/**
	 * Gets the saved games.
	 * @return A list of the saved games.
	 * @throws PersistenceException Thrown if issues occur getting the games.
	 */
	public List<ServerGameManager> GetAllGames() throws PersistenceException
	{
		IPersistenceProvider provider = handler.GetPlugin();
		
		List<String> games;
		try
		{
			provider.StartTransaction();
			games = provider.GetGameDAO().GetAllGames(); 
			provider.EndTransaction(true);
		}
		catch (PersistenceException e)
		{
			provider.EndTransaction(false);
			throw e;
		}
		
		List<ServerGameManager> convertedGames = new ArrayList<ServerGameManager>(games.size());
		for (String game : games)
		{
			try
			{
				ServerGameManager convertedGame = Deserialize(game, RealServerGameManager.class);
				convertedGames.add(convertedGame);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return convertedGames;
	}
	
	/**
	 * Gets the list of users.
	 * @return The list of saved users.
	 * @throws PersistenceException Thrown if issues occur getting the users.
	 */
	public List<ServerPlayer> GetAllUsers() throws PersistenceException
	{
		IPersistenceProvider provider = handler.GetPlugin();
		
		try
		{
			provider.StartTransaction();
			
			List<ServerPlayer> players = provider.GetUserDAO().GetAllUsers();
			
			provider.EndTransaction(true);
			
			return players;
		}
		catch (PersistenceException e)
		{
			provider.EndTransaction(false);
			throw e;
		}
	}
	
	/**
	 * Updates the currently saved game.
	 * @param sgm The server game manager to update.
	 * @throws PersistenceException Thron if errors occur updating.
	 */
	public void UpdateGame(ServerGameManager sgm) throws PersistenceException
	{
		IPersistenceProvider provider = handler.GetPlugin();
		
		try
		{
			provider.StartTransaction();
			
			int gameID = sgm.GetGameID();
			provider.GetCommandDAO().DeleteCommands(gameID);
			
			String blob = Serialize(sgm);
			provider.GetGameDAO().UpdateGame(gameID, blob);
			
			provider.EndTransaction(true);
		}
		catch (PersistenceException e)
		{
			provider.EndTransaction(false);
			throw e;
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			provider.EndTransaction(false);
			throw new PersistenceException("Unable to serialize");
		
		}
	}
	
	private String Serialize(Object object) throws IOException
	{
		ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
		ObjectOutputStream ooStream = new ObjectOutputStream(baoStream);
		ooStream.writeObject(object);
		ooStream.close();
		
		return Base64.getEncoder().encodeToString(baoStream.toByteArray());
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Serializable> T Deserialize(String object, java.lang.Class<T> objClass) throws IOException, ClassNotFoundException
	{
		byte[] data = Base64.getDecoder().decode(object); 
		ByteArrayInputStream baiStream = new ByteArrayInputStream(data);
		ObjectInputStream oiStream = new ObjectInputStream(baiStream);
		oiStream.close();
		
		 return (T)oiStream.readObject();
	}
}
