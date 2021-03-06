package server.commands.games;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import server.commands.CookieBuilder;
import server.commands.Factory;
import server.commands.ICommand;
import server.commands.ICommandBuilder;
import server.commands.ICommandDirector;
import server.commands.InvalidFactoryParameterException;
import server.model.GameException;
import shared.definitions.CatanColor;
import shared.networking.SerializationUtils;
import shared.networking.cookie.NetworkCookie;
import shared.networking.parameter.PCreateGame;
import shared.networking.parameter.PJoinGame;
import shared.networking.parameter.PLoadGame;
import shared.networking.parameter.PSaveGame;

/**
 * Creates games (notice the s) command objects.
 * @author Jonathan Sadler
 *
 */
public class GamesCommandFactory extends Factory 
{
	private Map<String, ICommandDirector> directors;
	
	/**
	 * Creates a GamesCommandFactory.
	 */
	public GamesCommandFactory() 
	{
		directors = new HashMap<String, ICommandDirector>(5);
		
		directors.put("CREATE", new CreateDirector());
		directors.put("JOIN", new JoinDirector());
		directors.put("LIST", new ListDirector());
		directors.put("LOAD", new LoadDirector());
		directors.put("SAVE", new SaveDirector());
	}

	@Override
	public ICommand GetCommand(StringBuilder param, NetworkCookie cookie, String object) throws InvalidFactoryParameterException 
	{
		String key = PopToken(param);
		
		if (!directors.containsKey(key))
		{
			InvalidFactoryParameterException e = new InvalidFactoryParameterException("Key doesn't exist: " + key);
			Logger.getLogger("CatanServer").throwing("GamesCommandFactory", "GetCommand", e);
			throw e;
		}
		
		try
		{
			CookieBuilder builder = (CookieBuilder)directors.get(key).GetBuilder();
			builder.SetData(object);
			builder.SetCookie(cookie);
			return builder.BuildCommand();
		}
		catch (GameException e)
		{
			InvalidFactoryParameterException e1 = new InvalidFactoryParameterException("Invalid cookie", e);
			Logger.getLogger("CatanServer").throwing("GamesCommandFactory", "GetCommand", e1);
			throw e1;
		}
	}
	
	private class CreateDirector implements ICommandDirector
	{
		@Override
		public ICommandBuilder GetBuilder() 
		{
			return new CreateBuilder();
		}
	}
	
	private class JoinDirector implements ICommandDirector
	{
		@Override
		public ICommandBuilder GetBuilder() 
		{
			return new JoinBuilder();
		}
	}
	
	private class ListDirector implements ICommandDirector
	{
		@Override
		public ICommandBuilder GetBuilder() 
		{
			return new ListBuilder();
		}
	}
	
	private class LoadDirector implements ICommandDirector
	{
		@Override
		public ICommandBuilder GetBuilder() 
		{
			return new LoadBuilder();
		}
	}
	
	private class SaveDirector implements ICommandDirector
	{
		@Override
		public ICommandBuilder GetBuilder() 
		{
			return new SaveBuilder();
		}
	}
	
	private class CreateBuilder extends CookieBuilder
	{
		private boolean randomTiles;
		private boolean randomNumbers;
		private boolean randomPorts;
		private String name;
		
		@Override
		public ICommand BuildCommand() 
		{
			return new GamesCreateCommand(randomTiles, randomNumbers, randomPorts, name);
		}

		@Override
		public void SetData(String object) 
		{
			PCreateGame creategame = SerializationUtils.deserialize(object, PCreateGame.class);
			randomTiles = creategame.isRandomTiles();
			randomNumbers = creategame.isRandomNumbers();
			randomPorts = creategame.isRandomPorts();
			name = creategame.getName();			
		}
	}
	
	private class JoinBuilder extends CookieBuilder
	{
		private int gameID;
		private CatanColor color;
		
		@Override
		public ICommand BuildCommand() 
		{
			return new GamesJoinCommand(cookie, gameID, color);
		}

		@Override
		public void SetData(String object) 
		{
			PJoinGame join = SerializationUtils.deserialize(object, PJoinGame.class);
			gameID = join.getId();
			color = join.getColor();
		}
	}
	
	private class ListBuilder extends CookieBuilder
	{
		@Override
		public ICommand BuildCommand() 
		{
			return new GamesListCommand();
		}

		@Override
		public void SetData(String object) 
		{
			return;
		}
	}
	
	private class LoadBuilder extends CookieBuilder
	{
		private String name;
		
		@Override
		public ICommand BuildCommand()
		{
			return new GamesLoadCommand(name);
		}

		@Override
		public void SetData(String object) 
		{
			PLoadGame lg = SerializationUtils.deserialize(object, PLoadGame.class);
			name = lg.getName();
			
		}
	}
	
	private class SaveBuilder extends CookieBuilder
	{
		private int id;
		private String name;
		
		@Override
		public ICommand BuildCommand() 
		{
			return new GamesSaveCommand(id, name);
		}

		@Override
		public void SetData(String object) 
		{
			PSaveGame sg = SerializationUtils.deserialize(object, PSaveGame.class);
			id = sg.getGameID();
			name = sg.getName();
			
		}
	}
}
