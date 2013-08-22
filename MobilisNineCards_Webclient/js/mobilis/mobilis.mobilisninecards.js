(function() {

	var ninecards = {

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
            .c('serviceNamespace').t(Mobilis.ninecards.NS.SERVICE).up()
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
            .c('ConfigureGameRequest', {xmlns : Mobilis.ninecards.NS.CONFIGUREGAME})
            .c('GameName').t(GameName).up()
            .c('MaxPlayers').t(MaxPlayers).up()
            .c('NumberOfRounds').t(NumberOfRounds).up();

            Mobilis.core.sendIQ(customIq, resultcallback, errorcallback);
        }, 




        joinGame: function(gameJid, resultcallback, errorcallback) {
            if (!resultcallback) 
                resultcallback = Mobilis.core.defaultcallback; 
            if (!errorcallback) 
                errorcallback = Mobilis.core.defaulterrorback;
                 
            if (gameJid){ 
                Mobilis.ninecards.gameJID = gameJid;
                var customiq = $iq({
                    to: gameJid,
                    type: 'set'
                })
                .c('JoinGameRequest' , {xmlns : Mobilis.ninecards.NS.JOINGAME}).up();
            } else {
                errorcallback(null, 'Game JID not defined');
            }

            Mobilis.core.sendIQ(customiq, resultcallback, errorcallback);
        }



	}

	Mobilis.extend("ninecards", ninecards);

})();