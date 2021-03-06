package client.turntracker;

import client.base.Controller;
import client.model.ClientGame;
import client.model.ClientGameManager;
import shared.definitions.CatanColor;
import shared.definitions.GameRound;
import shared.definitions.ModelNotification;
import shared.definitions.TurnState;
import shared.model.ModelObserver;
import shared.model.VictoryPointManager;


import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Implementation for the turn tracker controller
 */
public class TurnTrackerController extends Controller implements ITurnTrackerController, ModelObserver 
{	
	private int isInitializedTo;

	public TurnTrackerController(ITurnTrackerView view) 
	{
		super(view);
		ClientGame.getGame().startListening(this, ModelNotification.STATE);
		ClientGame.getGame().startListening(this, ModelNotification.SCORE);
		ClientGame.getGame().startListening(this, ModelNotification.PLAYERS);
		ClientGame.getGame().startListening(chatObserver, ModelNotification.CHAT);
		
		isInitializedTo = 0;
	}
	
	@Override
	public ITurnTrackerView getView() 
	{	
		return (ITurnTrackerView)super.getView();
	}

	@Override
	public void endTurn() 
	{
		ClientGame.getGame().endTurn();
	}
	
	private void initializeTurns()
	{
		ClientGameManager game = ClientGame.getGame();
		int myIndex = game.myPlayerIndex();
		CatanColor myColor = game.getPlayerColorByIndex(myIndex);
		
		for(int i = 0; i < game.getNumberPlayers(); i++)
		{
			getView().initializePlayer(i, game.getPlayerNameByIndex(i), game.getPlayerColorByIndex(i));
		}
		if (myColor != null)
			getView().setLocalPlayerColor(myColor);
	}
	
	private void updateFromModel() 
	{
		ClientGameManager game = ClientGame.getGame();
		int myIndex = game.myPlayerIndex();
		VictoryPointManager vp = game.getVictoryPointManager();
		
		int currPlayerIndex = game.CurrentPlayersTurn();
		int currNumPlayers = game.getNumberPlayers();
		
		if(isInitializedTo < currNumPlayers )
		{
			initializeTurns();
			isInitializedTo = currNumPlayers;
		}
		
		//update view for each player; 

		for(int i = 0; i < game.getNumberPlayers();i++)
		{
			boolean highlight = false;
			//0. See if it is this player's turn and highlight if it is
			if(currPlayerIndex == i)
			{
				highlight = true;
			}
			
			//1. if the current player has the longest road or the largest army, 
			//display icon on turn tracker
			
			boolean largestArmy = false;
			boolean longestRoad = false;
			if(vp.getCurrentLargestArmyPlayer() == i)
			{
				largestArmy = true;
			}
			
			if(vp.getCurrentLongestRoadPlayer() == i)
			{
				longestRoad = true;
			}
			
			int points = vp.getVictoryPoints(i);
			getView().updatePlayer(i, points, highlight, largestArmy, longestRoad);
		}
		
		if (game.CurrentState() == GameRound.ROLLING && currPlayerIndex == myIndex)
		{
			attemptPlayNoise("yourTurn.wav");
		}

		//check first to see if the game is over
		// if so, don't let the player finish his turn
		// because he shouldn't be playing the game anymore
		if(game.getVictoryPointManager().anyWinner()){
			this.getView().updateGameState("Game Over!", false);			
		}
		else if(game.CanFinishTurn() && currPlayerIndex == myIndex)
		{
			this.getView().updateGameState("Finish Turn", true);
		}
		else if(game.getTurnState() == TurnState.DISCARDING)
		{
			this.getView().updateGameState("Discarding...", false);
		}
		else if(game.getTurnState() == TurnState.ROAD_BUILDER)
		{
			this.getView().updateGameState("Place First Free Road...", false);
		}
		else if(game.getTurnState() == TurnState.ROAD_BUILDER_SECOND)
		{
			this.getView().updateGameState("Place Second Free Road...", false);
		}
		else if(game.getTurnState() == TurnState.ROBBING)
		{
			this.getView().updateGameState("Robbing...", false);
		}
		else
		{
			this.getView().updateGameState("Waiting for other players", false);
		}
	}

	private boolean welcomeToJungleExtraCredit = false;
	@Override
	public void alert()
	{
		ClientGameManager game = ClientGame.getGame();
		
		//OJO if the version number wraps around to -1, THIS WILL NOT WORK
		if(game.hasGameStarted())
		{
			if (!this.welcomeToJungleExtraCredit)
			{
				welcomeToTheJungle();
				this.welcomeToJungleExtraCredit = true;	
			}
			this.updateFromModel();
		}
	}
	
	private void welcomeToTheJungle()
	{
		/*String soundName = "images"+File.separator+"welcomeToJungle.wav";    
		AudioInputStream audioInputStream;
		audioInputStream = null;
		try 
		{
			audioInputStream = AudioSystem.getAudioInputStream(new File(soundName).getAbsoluteFile());
			
			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();
		}
		catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) 
		{
			e.printStackTrace();
		}*/
	}
	private ModelObserver chatObserver = new ModelObserver()
	{
		@Override
		public void alert()
		{
			if (ClientGame.getGame().getChat().lastChatter() == ClientGame.getGame().myPlayerIndex()) 
				return;
			
			attemptPlayNoise("chat.wav");
			
		}
	};
	
	private String OSName = "";
	private boolean isWindows = false; 
	private void attemptPlayNoise(String noiseFile)
	{
		if (OSName.equals(""))
		{
			OSName = System.getProperty("os.name");
			isWindows = OSName.startsWith("Windows");
		}
		if (isWindows)
			return;
		String soundName = "images" + File.separator + noiseFile;    
		AudioInputStream audioInputStream;
		audioInputStream = null;
		try 
		{
			audioInputStream = AudioSystem.getAudioInputStream(new File(soundName).getAbsoluteFile());
			
			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();
		}
		catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) 
		{
			e.printStackTrace();
		}			
	}
	/*
	 * String soundName = "images"+File.separator+"yourTurn.wav";    
		AudioInputStream audioInputStream;
		audioInputStream = null;
		try 
		{
			audioInputStream = AudioSystem.getAudioInputStream(new File(soundName).getAbsoluteFile());
			
			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();
		}
		catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) 
		{
			e.printStackTrace();
		}	
	 */
}

