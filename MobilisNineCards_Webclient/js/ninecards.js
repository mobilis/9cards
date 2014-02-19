var ninecards = {


	/* application functions */

	queryGames : function() {

		if ( MX.connection && MX.connection.connected ) {

			MX.core.discoverService(
				MX.NS.SERVICE,
				function(result) {
					$('#games-list').empty().listview();

					if ($(result).find('mobilisService').length){
						$(result).find('mobilisService').each( function() {
							$('#games-list').append(
								'<li><a class="available-game" id="'
								+ $(this).attr('jid')
								+ '" data-name="'
								+ $(this).attr('serviceName')
								+ '" href="#game" data-transition="slide">'
								+ $(this).attr('serviceName')
								+ ' (' + Strophe.getResourceFromJid($(this).attr('jid')) + ')'
								+ '</a></li>');
						});
					} else {
						$('#games-list').append('<li>No games found</li>');
					}
					$('#games-list').listview('refresh');
				},
				function(error) {
					console.error('queryGames error:',error);
				}
			);

		} else {

			var settings = jQuery.jStorage.get('settings');

			MX.core.connect(
				settings.gameserver,
				settings.jid,
				settings.password,
				function(status) {
					switch (status) {
						case Strophe.Status.CONNECTED:

							MX.connection.addHandler(
								ninecards.onIq, // handler
								null, // namespace
								'iq', // name
								'set' // type
							);
							ninecards.queryGames();
							break;

						case Strophe.Status.AUTHFAIL:
							ninecards.onAuthFail();
							break;
					}
				}
			);
		}
	},



	createGame : function(name) {

		MX.ninecards.createServiceInstance(
			name,
			function(createServiceInstanceResult){
				// console.log('createServiceInstanceResult',createServiceInstanceResult);
			},
			function(createServiceInstanceError){
				console.error('createServiceInstanceError',createServiceInstanceError);
			}

		);
	},



	requestConfiguration : function(jid,name) {

		MX.ninecards.getGameConfiguration(
			jid,
			function(getGameConfigurationResponse){

				game.serviceJid = jid;
				game.name = name;
				game.muc = $(getGameConfigurationResponse).find('muc').text();
				game.numPlayers = $(getGameConfigurationResponse).find('maxPlayers').text();
				game.numRounds = $(getGameConfigurationResponse).find('maxRounds').text();
				jQuery.jStorage.set('game',game);

				ninecards.joinGame(game.muc, game.name, function(joinGameResult){
					// console.log('joinGameResult',joinGameResult);
				});
			},
			function(getGameConfigurationError){
				console.error('getGameConfigurationResponse',getGameConfigurationError);
			}
		);

	},



	joinGame : function(muc, gameName, result){

		var res;
		MX.core.joinMuc(
			muc,
			ninecards.onMessage,
			ninecards.onPresence,
			ninecards.onRoster,
			function(result){

				$('#round-count span').hide();
				$('#game h1').append('<span id="hgn">'+gameName+'</span>');

				jQuery.mobile.changePage('#game', {
					transition: 'slide',
					changeHash: true
				});

				res = result;
			}
		);
		if (result) result(res);
	},



	startGame : function(){

		MX.core.buildMessage(null,'StartGameMessage', function(message){

			MX.core.sendDirectMessage(
				jQuery.jStorage.get('game').serviceJid,
				message
			);

		});
	},



	sendCard : function(card, result){

		var message = {
			'card': card,
			'round': ninecards.game.round
		};

		MX.core.buildMessage(
			message,
			'PlayCardMessage',
			function(message){
				MX.core.sendDirectMessage(
					jQuery.jStorage.get('game').serviceJid,
					message
				);
				result();
			}
		);

	},



	leaveGame : function(){
		MX.core.leaveMuc(
			jQuery.jStorage.get('chatroom'),
			'left Game',
			function(){

				jQuery.mobile.changePage(
					'#games', {
						transition: 'slide',
						reverse: true,
						changeHash: true
					}
				);

				$('#players-list').empty();
				$('#startgame-button').hide();
				$('#hgn').remove();
				$('#round-count span').hide();
				$('#numpad a').each(function(){
					$(this).addClass('ui-disabled');
				});
			}
		);
	},



	fillSettingsForm : function(settings){

		if (settings) {
			$.each(settings, function(key,value){
	            $('#settings-form #'+key).val(value);
	        });
		}

	},



	updateScore : function(message){
		$(message).find('playerinfo').each( function(index,playerInfo){

			var nick = Strophe.getResourceFromJid( $(playerInfo).find('id').text() );

			ninecards.players[nick].score = $(playerInfo).find('score').text();

			var usedCardsHTML = '';
			$(playerInfo).find('usedcards').each(function(index,usedCard){
				usedCardsHTML += '<span class="used-card">'+$(usedCard).text()+'</span>';
			});

			$('#'+ninecards.clearString(nick)+' p.ui-li-aside').html(usedCardsHTML).append(
				'<strong>/ '+ninecards.players[nick].score+'</strong>');

			$('#'+ninecards.clearString(nick)).buttonMarkup({ icon: 'clear' });
		});
	},



    onIq : function(iq) {

        switch (iq.firstChild.nodeName.toLowerCase()) {
			case 'sendnewserviceinstance' : ninecards.onSendNewServiceInstance(iq.firstChild); break;
		}

    },



	onMessage : function (message){

		var rawMessageBody = $(message).find('body').text();
		var parsedMessageHtml = $.parseHTML(rawMessageBody)[0];

		switch ( parsedMessageHtml.nodeName.toLowerCase() ) {
			case 'gamestartsmessage' : ninecards.onStartGameMessage(parsedMessageHtml); break;
			case 'cardplayedmessage' : ninecards.onCardPlayedMessage(parsedMessageHtml); break;
			case 'roundcompletemessage' : ninecards.onRoundCompleteMessage(parsedMessageHtml); break;
			case 'gameovermessage' : ninecards.onGameOverMessage(parsedMessageHtml); break;
			default : ninecards.onTextMessage(rawMessageBody); break;
		}

		return true;
	},



	onPresence : function (presence){
		
		if ($(presence).attr('type') == 'error') ninecards.onPresenceError(presence);
		return true;
	},



	onRoster : function (roster){

		ninecards.players = roster;
		$('#players-list').empty();
		$.each(ninecards.players, function(index,player){
			if (player.affiliation == 'owner'){
				jQuery.jStorage.set('serviceNick',player.nick);
			} else {
				$('#players-list').append(
					'<li id="' + ninecards.clearString(player.nick) + '" data-icon="clear"><a href="#">'
					+ player.nick +
					'<p class="ui-li-aside"></p></li>'
				).listview('refresh');
			}
		});
		return true;
	},



    onSendNewServiceInstance : function(iq){

		MX.addNamespace('SERVICE', $(iq).find('serviceNamespace').text() );

		var game = jQuery.jStorage.get('game');

		game.serviceJid = $(iq).find('jidOfNewService').text();

		jQuery.jStorage.set('game',game);

		MX.ninecards.configureGame(
			game.serviceJid, game.numPlayers, game.numRounds,
			function(configureGameResponse){

				game.muc = $(configureGameResponse).find('muc').text();
				jQuery.jStorage.set('game',game);

				ninecards.joinGame(game.muc, game.name, function(joinGameResult){
					console.log('joinGameResult',joinGameResult);
					$('#startgame-button').css('display','block');
				});
			},
			function(configureGameError){
				console.error('configureGameError',configureGameError);
			}
		);

    },



	onStartGameMessage : function (message) {
		$('#numpad a').removeClass('ui-disabled');

		ninecards.game = {};
		ninecards.game.password = $(message).find('password').text();
		ninecards.game.round = 1;
		ninecards.game.rounds = parseInt( $(message).find('rounds').text() );

		$('#round-count #round').html(ninecards.game.round);
		$('#round-count #rounds').html(ninecards.game.rounds);
		$('#round-count .ingame').show();

	},



	onCardPlayedMessage : function (message) {
		var nick = Strophe.getResourceFromJid( $(message).find('player').text() );
		$('#'+ninecards.clearString(nick)).buttonMarkup({ icon: 'check' });
	},



	onRoundCompleteMessage : function (message) {

		ninecards.updateScore(message);
		ninecards.game.round = parseInt($(message).find('round').text())+1;
		$('#round-count #round').html(ninecards.game.round);
	},



	onGameOverMessage : function (message) {

		ninecards.updateScore(message);

		var winner = Strophe.getResourceFromJid( $(message).find('winner').text() );
		var score = ninecards.players[winner].score;
		$('#round-count .ingame').hide();
		$('#round-count #gameover').show();

		$('#dialog-popup').popup({
			afteropen: function( event, ui ) {
				$(this).find('h1').html('Game Over');
				$(this).find('.ui-content h3').html('Winner:');
				$(this).find('.ui-content p').html(winner+' ('+score+')');
			},
            afterclose: function( event, ui ) {
                jQuery.mobile.changePage('#games', {
                    transition: 'slide',
                    reverse: true,
                    changeHash: true
                });
            }
		});
		$('#dialog-popup').popup('open', {
			positionTo: 'window'
		});

	},



	onTextMessage : function (message) {
		console.log('Text Message:', message);
	},



	onAuthFail : function(){
        $('#error-popup').popup({
            afteropen: function( event, ui ) {
                $(this).find('h1').html('Error');
                $(this).find('.ui-content h3').html('Authentication Fail');
                $(this).find('.ui-content p').html('Please check the settings');
            },
            afterclose: function( event, ui ) {
                MX.connection.disconnect();
                jQuery.mobile.changePage('#start', {
                    transition: 'slide',
                    reverse: true,
                    changeHash: true
                });
            }
        });
        $('#error-popup').popup('open', {
            positionTo: 'window'
        });
	},



	onPresenceError : function (presence) {

		var code = $(presence).find('error').attr('code');
		var children = $(presence).find('error').children().prop('tagName');
		$('#dialog-popup').popup({
			afteropen: function( event, ui ) {
				$(this).find('h1').html('Error');
				$(this).find('.ui-content h3').html(code);
				$(this).find('.ui-content p').html(children);
			},
			afterclose: function( event, ui ) {
				jQuery.mobile.changePage('#games', {
					transition: 'slide',
					reverse: true,
					changeHash: true
				});
			}
		});
		$('#dialog-popup').popup('open', {
			positionTo: 'window'
		});

	},



















	/* helper functions */

	clearString : function(string){
		return string.replace(/@/g,'-').replace(/\./g,'-').replace(/\//g,'-');
	}

}





































/* application jQuery handlers */

$(document).on('pageshow', '#settings', function() {

	ninecards.fillSettingsForm( jQuery.jStorage.get('settings') );

});


$(document).on('vclick', '#settings-submit', function() {

	var settings  = {};
	$('#settings-form input[placeholder]').each(function(key,value){
		settings[$(value).attr('id')] = $(value).val();
	});
	jQuery.jStorage.set('settings',settings);
	return true;

});


$(document).on('pageshow', '#games', function(){

	ninecards.queryGames();

});


$(document).on('vclick', '#refresh-games-button', function(event) {

	event.preventDefault();
	ninecards.queryGames();
	return false;

});


$(document).on('vclick', '.available-game', function (event) {

	event.preventDefault();
	ninecards.requestConfiguration($(this).attr('id'),$(this).attr('data-name'));

});


$(document).on('vclick', '#create-game-submit', function(event) {

	event.preventDefault();
	var game = {};
	game.name = $('#create-game-form #gamename').val();
	game.numPlayers = $('#create-game-form #numplayers').val();
	game.numRounds = $('#create-game-form #numrounds').val();

	if (game.name && game.numPlayers && game.numRounds) {
		jQuery.jStorage.set('game',game);
		ninecards.createGame(game.name);
	} else {
		console.error('fill out all fields');
	}

	$('#create-game-popup').popup('close');
	return false;
});


$(document).on('vclick', '#startgame-button', function(event){

	event.preventDefault();
	$('#startgame-button').hide();
	ninecards.startGame();
	return false;

});


$(document).on('click', '#numpad a', function(event) {

	event.preventDefault();
	var card  = $(this).attr('data-id');
	ninecards.sendCard(card, function(){
		$('a[data-id='+card+']').addClass('ui-disabled');
	});
	return false;

});


$(document).on('vclick', '#exitgame-button', function(event){

	event.preventDefault();
	ninecards.leaveGame();
	return false;

});


$(document).on('vclick', '#exitgames-button', function(event){

	event.preventDefault();
	MX.core.disconnect('Application Closed');
	jQuery.mobile.changePage(
		'#start', {
			transition: 'slide',
			reverse: true,
			changeHash: true
		}
	);
	return false;

});














/* core jQuery handlers */

$( window ).on('beforeunload', function() {
	MX.core.disconnect('Browser Window Closed');
});















/* Development Hooks */

$(document).on('vclick', '#load-defaults-button', function() {

    $.getJSON('settings.json', function(data){
		ninecards.fillSettingsForm(data);
    });

});

$(document).on('pageshow', '#games', function(){

	$('#create-game-form #gamename').val('asdf');
	$('#create-game-form #numplayers').val('3');
	$('#create-game-form #numrounds').val('3');

});
