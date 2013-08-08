(function() {

	var mobilisninecards = {

		NS : {
			SERVICE : "http://mobilis.inf.tu-dresden.de#services/MobilisNineCardsService",
			CONFIGUREGAME : "mobilisninecards:iq:configuregame",
			JOINGAME : "mobilisninecards:iq:joingame"
		},

        settings: {},
        
        handlers: {},


        createServiceInstance: function (name, resultcallback, errorcallback) {
            var customIq = $iq({
                to: Mobilis.core.SERVICES[Mobilis.core.NS.COORDINATOR].jid,
                type: 'set'                
            })
            .c('createNewServiceInstance', {xmlns: Mobilis.core.NS.COORDINATOR} )
            .c('serviceNamespace').t(Mobilis.core.NS.SERVICE).up()
            .c('serviceName').t(name);

            Mobilis.core.sendIQ(customIq, resultcallback, errorcallback);
        },


        ConfigureGame: function(gameJID, GameName, MaxPlayers, NumberOfRounds, resultcallback, errorcallback) {
            if (!resultcallback) 
                resultcallback = Mobilis.core.defaultcallback; 
            if (!errorcallback) 
                errorcallback = Mobilis.core.defaulterrorback;

            var customIq = $iq({
                to: gameJID,
                type: 'set'
            })
            .c('CreateGameRequest', {xmlns : Mobilis.xhunt.NS.CREATEGAME})
            .c('GameName').t(GameName).up()
            .c('MaxPlayers').t(gamepassword).up()
            .c('NumberOfRounds').t(NumberOfRounds).up();

            console.log(customIq);
            Mobilis.core.sendIQ(customIq, resultcallback, errorcallback);
        }, 




        joinGame: function(gameJID, playerName, resultcallback, errorcallback) {
            if (!resultcallback) 
                resultcallback = Mobilis.core.defaultcallback; 
            if (!errorcallback) 
                errorcallback = Mobilis.core.defaulterrorback;
                 
            if (gameJID){ 
                Mobilis.xhunt.gameJID = gameJID;
                var customiq = $iq({
                    to: gameJID,
                    type: 'set'
                })
                .c('JoinGameRequest' , {xmlns : Mobilis.xhunt.NS.JOINGAME})
                .c('GamePassword').t('null').up();
                if (playerName) 
                    customiq.c('PlayerName').t(playerName).up().c('IsSpectator').t('false');
            } else {
                errorcallback(null, 'Game JID not defined');
            }

            Mobilis.core.sendIQ(customiq, resultcallback, errorcallback);
        }



	}

	Mobilis.extend("mobilisninecards", mobilisninecards);

})();