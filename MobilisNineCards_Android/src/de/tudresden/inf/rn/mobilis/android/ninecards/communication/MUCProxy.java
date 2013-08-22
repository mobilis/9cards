package de.tudresden.inf.rn.mobilis.android.ninecards.communication;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.Card;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.PlayCardMessage;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.PlayerInfo;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.PlayerInfosMessage;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.PlayerLeavingMessage;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.RoundCompleteMessage;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.StartGameMessage;
import de.tudresden.inf.rn.mobilis.android.ninecards.service.BackgroundService;
import de.tudresden.inf.rn.mobilis.mxa.ConstMXA.MessageItems;
import de.tudresden.inf.rn.mobilis.mxa.IXMPPService;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPMessage;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo;

/*******************************************************************************
 * Copyright (C) 2013 Technische Universität Dresden
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: https://github.com/mobilis
 ******************************************************************************/
public class MUCProxy {
	
	/** The application service running in the background. */
	private BackgroundService bgService;

	/** The XMPP service provided by MXA. */
	private IXMPPService iXMPPService;
	
	/** The XML Pull Parser used for the fromXML() methods. */
	private XmlPullParser xmlParser;
	
	/**
	 * 
	 * @param appContext
	 * @param iXMPPService
	 */
	public MUCProxy(BackgroundService bgService, IXMPPService iXMPPService) {
		this.bgService = bgService;
		this.iXMPPService = iXMPPService;
		
		try {
			XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
			parserFactory.setNamespaceAware(true);
			xmlParser = parserFactory.newPullParser();
		} catch (Exception e) {
			Log.e(this.getClass().getSimpleName(), "Failed to instantiate XML Parser!");
		}
	}
	
	
	/**
	 * Connect to user chat.
	 * 
	 * @param roomID the room id of the chat
	 * @param password the password of the chat
	 * @return true, if successful
	 * @throws RemoteException the remote exception if something goes wrong
	 */
	public boolean connectToMUC(String roomID, String password) throws RemoteException {
		// Initialize the chat service of the MXA
		if (bgService.getMXAProxy().isConnectedToXMPPServer()) {

			if(roomID != null && password != null) {
				iXMPPService.getMultiUserChatService().joinRoom(roomID, password);

				// use beginning (node) of jid as nick in chat
				String nickname = bgService.getMXAProxy().isConnectedToXMPPServer()
						? iXMPPService.getUsername().substring(0, iXMPPService.getUsername().indexOf("@"))
								: "nick n/a";
						
				iXMPPService.getMultiUserChatService().changeNickname(roomID, nickname);
			}

			return true;
		}
		return false;
	}
	

	/**
	 * Register an observer for incoming chat messages.
	 * 
	 * @param activity the activity which want to be notified if a chat messges arrives
	 * @param resultHandler the result handler
	 * @param filter the filter
	 */
	public void registerIncomingMessageObserver(Activity activity, final Handler userMessageHandler) {
		// create a cursor for the chat messages which arrives in MXA
		final Cursor msgCursor = activity.getContentResolver().query(
				MessageItems.contentUri, null, null, null,
				MessageItems.DEFAULT_SORT_ORDER);
		activity.startManagingCursor(msgCursor);

		// observe the cursor for changes
		ContentObserver co = new ContentObserver(userMessageHandler) {
			@Override
			public void onChange(boolean selfChange) {
				msgCursor.requery();
				msgCursor.moveToLast();

				String sender = msgCursor.getString(msgCursor.getColumnIndex(MessageItems.SENDER));
				String body = msgCursor.getString(msgCursor.getColumnIndex(MessageItems.BODY));
				
				// display message if it's a user chat message
				if(!isSystemMessage(sender, body)) {
					Message msg = new Message();
					msg.obj = sender + ": \"" + body + "\"";
					userMessageHandler.sendMessage(msg);							
				}

				// else handle it internally
				else {
					try {
						// reconstruct message and handle it depending on its type
						String msgType = body.substring(body.indexOf("<MessageType>") +13, body.indexOf("</MessageType>"));
						String msgContent = body.substring(body.indexOf("<MessageString>") +15, body.indexOf("</MessageString>"));

						XMPPInfo xmppInfo = null;
						xmlParser.setInput(new StringReader(msgContent));

						if (msgType.equals("PlayerInfos"))
							xmppInfo = new PlayerInfosMessage();

						else if (msgType.equals("StartGame"))
							xmppInfo = new StartGameMessage();

						//else if (msgType.equals("RoundComplete"))
						//	xmppInfo = new RoundCompleteMessage();

						else if (msgType.equals("PlayerLeaving"))
							xmppInfo = new PlayerLeavingMessage();

						else if (msgType.equals("PlayCard"))
							xmppInfo = new PlayCardMessage();

						if (xmppInfo != null) {
							xmppInfo.fromXML(xmlParser);
							bgService.processMucMessage(xmppInfo);
						}
						
else if (msgType.equals("RoundComplete")) {
	
	xmppInfo = new RoundCompleteMessage();
	String s1 = msgContent.substring(msgContent.indexOf("<RoundID>") +9, msgContent.indexOf("</RoundID>"));
	String s2 = msgContent.substring(msgContent.indexOf("<RoundWinnersName>") +18, msgContent.indexOf("</RoundWinnersName>"));
	String s3 = msgContent.substring(msgContent.indexOf("<RoundWinnersJID>") +17, msgContent.indexOf("</RoundWinnersJID>"));
	String s4 = msgContent.substring(msgContent.indexOf("<EndOfGame>") +11, msgContent.indexOf("</EndOfGame>"));
	((RoundCompleteMessage) xmppInfo).setRoundID(Integer.parseInt(s1));
	((RoundCompleteMessage) xmppInfo).setRoundWinnersName(s2);
	((RoundCompleteMessage) xmppInfo).setRoundWinnersJID(s3);
	((RoundCompleteMessage) xmppInfo).setEndOfGame(Boolean.parseBoolean(s4));
	
	List<PlayerInfo> list = new ArrayList<PlayerInfo>();
	String plrInfos = msgContent.substring(msgContent.indexOf("<PlayerInfo>"), msgContent.lastIndexOf("</PlayerInfo>") +13);
	String[] infos = plrInfos.split("</PlayerInfo>");
	for(int i=0; i<infos.length; i++) {
		PlayerInfo info = new PlayerInfo();
		String s11 = infos[i].substring(infos[i].indexOf("<PlayersName>") +13, infos[i].indexOf("</PlayersName>"));
		String s22 = infos[i].substring(infos[i].indexOf("<PlayersJID>") +12, infos[i].indexOf("</PlayersJID>"));
		String s33 = infos[i].substring(infos[i].indexOf("<PlayersWins>") +13, infos[i].indexOf("</PlayersWins>"));
		info.setPlayersName(s11);
		info.setPlayersJID(s22);
		info.setPlayersWins(Integer.parseInt(s33));
		
		List<Card> cards = new ArrayList<Card>();
		String crdInfos = infos[i].substring(infos[i].indexOf("<Card>"), infos[i].lastIndexOf("</Card>") +7);
		String[] crds = crdInfos.split("</Card>");

		for(int j=0; j<crds.length; j++) {
			Card card = new Card();
			String s111 = crds[j].substring(crds[j].indexOf("<Value>") +7, crds[j].indexOf("</Value>"));
			String s222 = crds[j].substring(crds[j].indexOf("<AlreadyUsed>") +13, crds[j].indexOf("</AlreadyUsed>"));
			card.setValue(Integer.parseInt(s111));
			card.setAlreadyUsed(Boolean.parseBoolean(s222));
			cards.add(card);
		}
		
		info.setPlayersUsedCards(cards);
		list.add(info);
	}
	((RoundCompleteMessage) xmppInfo).setPlayerInfos(list);
	
	bgService.processMucMessage(xmppInfo);
}
						
					} catch (Exception e) {
						Log.w(getClass().getSimpleName(), "Failed to parse message! (" + e.getMessage() + ")");
					}
				}

				super.onChange(selfChange);
			}
		};

System.out.println("msgCursor: " + (msgCursor != null));
System.out.println("co: " + (co != null));
		msgCursor.registerContentObserver(co);
	}
	
	
	/**
	 * 
	 * @param sender
	 * @param body
	 * @return
	 */
	private boolean isSystemMessage(String sender, String body) {

		if(body.contains("<IsSystemMessage>true</IsSystemMessage>"))
			return true;

		return false;
	}
	
	
	/**
	 * Send the Message to the XMPP-Service
	 */
	public void sendMessageToMuc(String body) {
		
		// Create a new XMPPMessage
		XMPPMessage xMsg = new XMPPMessage();
		
		// Set the type of the message to GROUPCHAT
		xMsg.type = XMPPMessage.TYPE_GROUPCHAT;
		
		// Fill the body of the message
		xMsg.body = body;

		if(bgService.getMXAProxy().isConnectedToXMPPServer()) {
			try {
				// Send the Message to the MXAController
				iXMPPService.getMultiUserChatService().sendGroupMessage(bgService.getMucRoomId(), xMsg);

			} catch (RemoteException e) {
				Log.e(this.getClass().getSimpleName(), "sendMessageToMuc() failed (" + e.getMessage() + ")");
			}
		}
		
		else {
			// If XMPP isn't connected notify the user
			Toast.makeText(bgService.getApplicationContext(),
					"Currently no connection to server. Couldn't send, retry later",
					Toast.LENGTH_LONG).show();
		}
	}
}
