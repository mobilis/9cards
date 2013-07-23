package de.tudresden.inf.rn.mobilis.android.ninecards.communication;

import android.app.Activity;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.RemoteException;
import android.widget.Toast;
import de.tudresden.inf.rn.mobilis.android.ninecards.service.BackgroundService;
import de.tudresden.inf.rn.mobilis.mxa.ConstMXA.MessageItems;
import de.tudresden.inf.rn.mobilis.mxa.IXMPPService;

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
	
	
	/**
	 * 
	 * @param appContext
	 * @param iXMPPService
	 */
	public MUCProxy(BackgroundService bgService, IXMPPService iXMPPService) {
		this.bgService = bgService;
		this.iXMPPService = iXMPPService;
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
			iXMPPService.getMultiUserChatService();

			if(roomID != null && password != null) {
				iXMPPService.getMultiUserChatService().joinRoom(roomID, password);

				// use beginning (node) of jid as nick in chat
				String jid = bgService.getMXAProxy().isConnectedToXMPPServer() ? iXMPPService.getUsername() : "nick n/a";
				String nickname = jid.substring(0, jid.indexOf("@"));
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
	public void registerIncomingMessageObserver(Activity activity) {
		// create a cursor for the chat messages which arrives in MXA
		final Cursor msgCursor = activity.getContentResolver().query(
				MessageItems.contentUri, null, null, null,
				MessageItems.DEFAULT_SORT_ORDER);
		activity.startManagingCursor(msgCursor);

		// observe the cursor for changes
		ContentObserver co = new ContentObserver(null) {
			@Override
			public void onChange(boolean selfChange) {
				msgCursor.requery();
				msgCursor.moveToLast();

				String sender = msgCursor.getString(msgCursor.getColumnIndex(MessageItems.SENDER));
				String body = msgCursor.getString(msgCursor.getColumnIndex(MessageItems.BODY));

				// display message if it's a user chat message
				if(!isSystemMessage(sender, body)) {
					Toast.makeText(
							bgService.getApplicationContext(),
							sender + ": \"" + body + "\"",
							Toast.LENGTH_LONG).show();
				}
				// else handle it internally
				else {
					bgService.processMucMessage(sender, body);
				}

				super.onChange(selfChange);
			}
		};

		msgCursor.registerContentObserver(co);
	}
	
	
	//TODO
	private boolean isSystemMessage(String sender, String body) {
		return true;
	}
}
