(function() {

	var mobilisninecards = {

		NS : {
			SERVICE : "http://mobilis.inf.tu-dresden.de#services/MobilisNineCardsService",
			CONFIGUREGAME : "mobilisninecards:iq:configuregame",
			JOINGAME : "mobilisninecards:iq:joingame"
		},

		ELEMENTS : {
			MessageWrapper : function MessageWrapper(IsSystemMessage, MessageString, MessageType) {
				if (arguments[0] instanceof jQuery) {
					var _MessageWrapper = this;

					arguments[0].children().each(function() {
						switch($(this).prop("tagName")) {
							case "IsSystemMessage": _MessageWrapper.IsSystemMessage = $(this).text(); break;
							case "MessageString": _MessageWrapper.MessageString = $(this).text(); break;
							case "MessageType": _MessageWrapper.MessageType = $(this).text(); break;
						}
					});
				} else {
					this.IsSystemMessage = IsSystemMessage;
					this.MessageString = MessageString;
					this.MessageType = MessageType;
				}
			},
			StartGameMessage : function StartGameMessage() {
			},
			PlayCardMessage : function PlayCardMessage(PlayersName, PlayersJID, CardID) {
				if (arguments[0] instanceof jQuery) {
					var _PlayCardMessage = this;

					arguments[0].children().each(function() {
						switch($(this).prop("tagName")) {
							case "PlayersName": _PlayCardMessage.PlayersName = $(this).text(); break;
							case "PlayersJID": _PlayCardMessage.PlayersJID = $(this).text(); break;
							case "CardID": _PlayCardMessage.CardID = $(this).text(); break;
						}
					});
				} else {
					this.PlayersName = PlayersName;
					this.PlayersJID = PlayersJID;
					this.CardID = CardID;
				}
			},
			RoundCompleteMessage : function RoundCompleteMessage(RoundID, RoundWinnersName, RoundWinnersJID, PlayerInfos, EndOfGame) {
				if (arguments[0] instanceof jQuery) {
					var _RoundCompleteMessage = this;
					_RoundCompleteMessage.PlayerInfos = [];

					arguments[0].children().each(function() {
						switch($(this).prop("tagName")) {
							case "RoundID": _RoundCompleteMessage.RoundID = $(this).text(); break;
							case "RoundWinnersName": _RoundCompleteMessage.RoundWinnersName = $(this).text(); break;
							case "RoundWinnersJID": _RoundCompleteMessage.RoundWinnersJID = $(this).text(); break;
							case "PlayerInfo": _RoundCompleteMessage.PlayerInfos.push(new Mobilis.mobilisninecards.ELEMENTS.PlayerInfo($(this))); break;
							case "EndOfGame": _RoundCompleteMessage.EndOfGame = $(this).text(); break;
						}
					});
				} else {
					this.RoundID = RoundID;
					this.RoundWinnersName = RoundWinnersName;
					this.RoundWinnersJID = RoundWinnersJID;
					this.PlayerInfos = PlayerInfos;
					this.EndOfGame = EndOfGame;
				}
			},
			PlayerLeavingMessage : function PlayerLeavingMessage(LeavingJID) {
				if (arguments[0] instanceof jQuery) {
					var _PlayerLeavingMessage = this;

					arguments[0].children().each(function() {
						switch($(this).prop("tagName")) {
							case "LeavingJID": _PlayerLeavingMessage.LeavingJID = $(this).text(); break;
						}
					});
				} else {
					this.LeavingJID = LeavingJID;
				}
			},
			PlayerInfo : function PlayerInfo(PlayersName, PlayersJID, PlayersWins, PlayersUsedCards) {
				if (arguments[0] instanceof jQuery) {
					var _PlayerInfo = this;
					_PlayerInfo.PlayersUsedCards = [];

					arguments[0].children().each(function() {
						switch($(this).prop("tagName")) {
							case "PlayersName": _PlayerInfo.PlayersName = $(this).text(); break;
							case "PlayersJID": _PlayerInfo.PlayersJID = $(this).text(); break;
							case "PlayersWins": _PlayerInfo.PlayersWins = $(this).text(); break;
							case "Card": _PlayerInfo.PlayersUsedCards.push(new Mobilis.mobilisninecards.ELEMENTS.Card($(this))); break;
						}
					});
				} else {
					this.PlayersName = PlayersName;
					this.PlayersJID = PlayersJID;
					this.PlayersWins = PlayersWins;
					this.PlayersUsedCards = PlayersUsedCards;
				}
			},
			Card : function Card(Value, AlreadyUsed) {
				if (arguments[0] instanceof jQuery) {
					var _Card = this;

					arguments[0].children().each(function() {
						switch($(this).prop("tagName")) {
							case "Value": _Card.Value = $(this).text(); break;
							case "AlreadyUsed": _Card.AlreadyUsed = $(this).text(); break;
						}
					});
				} else {
					this.Value = Value;
					this.AlreadyUsed = AlreadyUsed;
				}
			},
			ConfigureGameResponse : function ConfigureGameResponse() {
			},
			ConfigureGameRequest : function ConfigureGameRequest(GameName, MaxPlayers, NumberOfRounds) {
				if (arguments[0] instanceof jQuery) {
					var _ConfigureGameRequest = this;

					arguments[0].children().each(function() {
						switch($(this).prop("tagName")) {
							case "GameName": _ConfigureGameRequest.GameName = $(this).text(); break;
							case "MaxPlayers": _ConfigureGameRequest.MaxPlayers = $(this).text(); break;
							case "NumberOfRounds": _ConfigureGameRequest.NumberOfRounds = $(this).text(); break;
						}
					});
				} else {
					this.GameName = GameName;
					this.MaxPlayers = MaxPlayers;
					this.NumberOfRounds = NumberOfRounds;
				}
			},
			JoinGameResponse : function JoinGameResponse(ChatRoom, ChatPassword) {
				if (arguments[0] instanceof jQuery) {
					var _JoinGameResponse = this;

					arguments[0].children().each(function() {
						switch($(this).prop("tagName")) {
							case "ChatRoom": _JoinGameResponse.ChatRoom = $(this).text(); break;
							case "ChatPassword": _JoinGameResponse.ChatPassword = $(this).text(); break;
						}
					});
				} else {
					this.ChatRoom = ChatRoom;
					this.ChatPassword = ChatPassword;
				}
			},
			JoinGameRequest : function JoinGameRequest() {
			}
		},

		DECORATORS : {
			ConfigureGameHandler : function(_callback, _return) {
				return function(_iq) {
					var $iq = $(_iq);

					_callback.apply(this, [new Mobilis.mobilisninecards.ELEMENTS.ConfigureGameResponse($iq.children())]);

					return _return;
				};
			},
			JoinGameHandler : function(_callback, _return) {
				return function(_iq) {
					var $iq = $(_iq);

					_callback.apply(this, [new Mobilis.mobilisninecards.ELEMENTS.JoinGameResponse($iq.children())]);

					return _return;
				};
			},
			ConfigureGameFaultHandler : function(_callback) {
				return function(_iq) {
					var $iq = $(_iq).children();
					var $error = $iq.children("error");

					_callback.apply(this, [new Mobilis.mobilisninecards.ELEMENTS.ConfigureGameRequest($iq), {
						type : $error.attr("type"),
						condition : $error.children().prop("tagName"),
						message : $error.find("text").text()
					}]);
				}
			},
			JoinGameFaultHandler : function(_callback) {
				return function(_iq) {
					var $iq = $(_iq).children();
					var $error = $iq.children("error");

					_callback.apply(this, [new Mobilis.mobilisninecards.ELEMENTS.JoinGameRequest($iq), {
						type : $error.attr("type"),
						condition : $error.children().prop("tagName"),
						message : $error.find("text").text()
					}]);
				}
			}
		},

		sendConfigureGame : function(ConfigureGameRequest, onResult, onError, onTimeout) {
			var _iq = Mobilis.utils.createMobilisServiceIq(Mobilis.mobilisninecards.NS.SERVICE, {
				type : "set"
			});
			_iq.c("ConfigureGame", {
				xmlns : Mobilis.mobilisninecards.NS.CONFIGUREGAME
			});
			Mobilis.utils.appendElement(_iq, ConfigureGameRequest);
			Mobilis.core.sendIQ(_iq, Mobilis.mobilisninecards.DECORATORS.ConfigureGameHandler(onResult, false), Mobilis.mobilisninecards.DECORATORS.ConfigureGameFaultHandler(onError), onTimeout);
		},

		sendJoinGame : function(JoinGameRequest, onResult, onError, onTimeout) {
			var _iq = Mobilis.utils.createMobilisServiceIq(Mobilis.mobilisninecards.NS.SERVICE, {
				type : "set"
			});
			_iq.c("JoinGame", {
				xmlns : Mobilis.mobilisninecards.NS.JOINGAME
			});
			Mobilis.utils.appendElement(_iq, JoinGameRequest);
			Mobilis.core.sendIQ(_iq, Mobilis.mobilisninecards.DECORATORS.JoinGameHandler(onResult, false), Mobilis.mobilisninecards.DECORATORS.JoinGameFaultHandler(onError), onTimeout);
		},

		addConfigureGameHandler : function(handler) {
			Mobilis.core.addHandler(Mobilis.mobilisninecards.DECORATORS.ConfigureGameHandler(handler, true), Mobilis.mobilisninecards.NS.CONFIGUREGAME, "result");
		},

		addJoinGameHandler : function(handler) {
			Mobilis.core.addHandler(Mobilis.mobilisninecards.DECORATORS.JoinGameHandler(handler, true), Mobilis.mobilisninecards.NS.JOINGAME, "result");
		}
	}

	Mobilis.extend("mobilisninecards", mobilisninecards);

})();