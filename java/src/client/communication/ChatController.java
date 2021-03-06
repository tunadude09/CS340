package client.communication;

import java.util.ArrayList;
import java.util.List;

import client.base.Controller;
import client.model.ClientGame;
import client.model.ClientGameManager;
import shared.definitions.CatanColor;
import shared.definitions.ModelNotification;
import shared.model.GameManager;
import shared.model.ModelObserver;
import shared.model.chat.ChatBox;
import shared.model.chat.ChatMessage;


/**
 * Chat controller implementation
 * @author parkerridd
 */
public class ChatController extends Controller implements IChatController, ModelObserver {

	public ChatController(IChatView view) {
		super(view);
		ClientGame.getGame().startListening(this, ModelNotification.CHAT);
	}

	@Override
	public IChatView getView() {
		return (IChatView)super.getView();
	}

	@Override
	public void sendMessage(String message) {
		ClientGame.getGame().SendChat(message);
	}
	
	private void updateFromModel()
	{
		//get needed objects
		GameManager mng = ClientGame.getGame();
		ChatBox chat = mng.getChat();
		
		//go through messages and make a list of LogEntry objects
		//to pass to the view
		List<LogEntry> entries = new ArrayList<LogEntry>();
		int numChats = chat.size();
		
		for(int i = 0; i < numChats; i++)
		{
			ChatMessage tempChat = chat.get(i);
			int playerIndex = tempChat.getPlayerId();
			CatanColor col =  mng.getPlayerColorByIndex(playerIndex);
			LogEntry tempEntry = new LogEntry(col, tempChat.getMessage());
			entries.add(tempEntry);
		}
		
		this.getView().setEntries(entries);
	}

	@Override
	public void alert()
	{
		ClientGameManager game = ClientGame.getGame();
		if(game.GetVersion() != -1)
			System.out.println("Refreshing chat...");
			this.updateFromModel();
	}

}

