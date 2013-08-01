var mobilisninecards = {

	HTTPBIND : "http://127.0.0.1:7070/http-bind/",

	createMessageWrapper : function(IsSystemMessage, MessageString, MessageType) {
		return new Mobilis.mobilisninecards.ELEMENTS.MessageWrapper(IsSystemMessage, MessageString, MessageType);
	},
	createStartGameMessage : function() {
		return new Mobilis.mobilisninecards.ELEMENTS.StartGameMessage();
	},
	createPlayCardMessage : function(PlayersName, PlayersJID, CardID) {
		return new Mobilis.mobilisninecards.ELEMENTS.PlayCardMessage(PlayersName, PlayersJID, CardID);
	},
	createRoundCompleteMessage : function(RoundID, RoundWinnersName, RoundWinnersJID, PlayerInfos, EndOfGame) {
		return new Mobilis.mobilisninecards.ELEMENTS.RoundCompleteMessage(RoundID, RoundWinnersName, RoundWinnersJID, PlayerInfos, EndOfGame);
	},
	createPlayerLeavingMessage : function(LeavingJID) {
		return new Mobilis.mobilisninecards.ELEMENTS.PlayerLeavingMessage(LeavingJID);
	},
	createPlayerInfo : function(PlayersName, PlayersJID, PlayersWins, PlayersUsedCards) {
		return new Mobilis.mobilisninecards.ELEMENTS.PlayerInfo(PlayersName, PlayersJID, PlayersWins, PlayersUsedCards);
	},
	createCard : function(Value, AlreadyUsed) {
		return new Mobilis.mobilisninecards.ELEMENTS.Card(Value, AlreadyUsed);
	},
	createConfigureGameRequest : function(GameName, MaxPlayers, NumberOfRounds) {
		return new Mobilis.mobilisninecards.ELEMENTS.ConfigureGameRequest(GameName, MaxPlayers, NumberOfRounds);
	},
	createConfigureGameResponse : function() {
		return new Mobilis.mobilisninecards.ELEMENTS.ConfigureGameResponse();
	},
	createJoinGameRequest : function() {
		return new Mobilis.mobilisninecards.ELEMENTS.JoinGameRequest();
	},
	createJoinGameResponse : function(ChatRoom, ChatPassword) {
		return new Mobilis.mobilisninecards.ELEMENTS.JoinGameResponse(ChatRoom, ChatPassword);
	},

	onConfigureGame : function(ConfigureGameResponse) {
		//TODO: Auto-generated ConfigureGameHandler
	},
	onJoinGame : function(JoinGameResponse) {
		//TODO: Auto-generated JoinGameHandler
	},

	addHandlers : function() {
	},

	connect : function(uFullJid, uPassword, mBareJid, onSuccess) {
		Mobilis.utils.trace("Trying to establish a connection to Mobilis");
		Mobilis.core.connect(uFullJid, uPassword, mBareJid, mobilisninecards.HTTPBIND, function() {
			mobilisninecards.addHandlers();
			onSuccess && onSuccess();
		});
	}
}