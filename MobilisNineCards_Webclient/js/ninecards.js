var ninecards = {


	// TODO: refactor on…-handlers:

	onMessage : function (message){

		var rawMessageBody = $(message).find('body').text();
		var parsedMessageHtml = $.parseHTML(rawMessageBody)[0];

		if ( parsedMessageHtml.nodeName.toLowerCase() == 'mobilismessage' ) {
			console.log('mobilismessage', parsedMessageHtml);
			switch ( $(parsedMessageHtml).attr('type').toLowerCase() ) {
				case 'startgamemessage' : ninecards.onStartGameMessage(parsedMessageHtml); break;
				case 'cardplayedmessage' : ninecards.onCardPlayedMessage(parsedMessageHtml); break;
				case 'roundcompletemessage' : ninecards.onRoundCompleteMessage(parsedMessageHtml); break;
				case 'gameovermessage' : ninecards.onGameOverMessage(parsedMessageHtml); break;
			}
		} else {
			console.log('chatmessage:', rawMessageBody);
		}
		return true;
	},

	onPresence : function (presence){
		
		console.log('presence',presence);
		if ($(presence).attr('type') == 'error') ninecards.onPresenceError(presence);
		return true;
	},

	onRoster : function (roster){

		console.log('roster',roster);
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

		console.log('cardplayedmessage',message);
		var nick = Strophe.getResourceFromJid( $(message).find('player').text() );
		console.log('nick',nick);
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
			}
		});
		$('#dialog-popup').popup('open', {
			positionTo: 'window'
		});

	},

































	/* application-specific functions */

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
								MX.NS.APP, // namespace
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

    onIq : function(iq) {

        switch (iq.firstChild.nodeName) {
			case 'sendNewServiceInstance' : ninecards.onSendNewServiceInstance(iq.firstChild); break;
		}

    },

    onSendNewServiceInstance : function(iq){
		console.log('onSendNewServiceInstance',iq);
    },

	createGame : function(gameName, maxPlayers, numberOfRounds) {


		MX.ninecards.createServiceInstance(
			gameName,
			function(result){
				console.log(result);
				// var gameJid = ($(result).find('jidOfNewService').text());

				// MX.ninecards.ConfigureGame(
				// 	gameJid, gameName, maxPlayers, numberOfRounds, 
				// 	function(result){
				// 		// TODO  mobilis 3.0 änderungen änderungen, siehe mobilis.inf
				// 		ninecards.joinGame(gameJid, gameName, function(result){
				// 			console.log(result);
				// 			$('#startgame-button').css('display','block');
				// 		});
				// 	},
				// 	function(error){
				// 		console.error('ConfigureGame error',error);
				// 	}
				// );
			},
			function(error){
				console.error('createServiceInstance error',error);
			}

		);
	},


	joinGame : function(serviceJid, gameName, result){

		var resource = Strophe.getResourceFromJid(serviceJid).toLowerCase();
		var domain = Strophe.getDomainFromJid(serviceJid);
		var chatservice = jQuery.jStorage.get('settings').chatservice;
		var chatroom = resource+'@'+chatservice+'.'+domain;

		jQuery.jStorage.set('chatroom',chatroom);

		var res;
		MX.core.joinMuc(
			chatroom,
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
			MX.core.sendChatMessage(
				jQuery.jStorage.get('serviceNick'),
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
			function(mobilisMessage){
				MX.core.sendChatMessage(
					jQuery.jStorage.get('serviceNick'),
					mobilisMessage
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


	updateScore : function(message){ // TODO maybe move to onRoundCompleteMessage if MSDL refactoring
		$(message).find('playerinfo').each( function(index,playerInfo){
			console.log('playerinfo',playerInfo);
			var nick = Strophe.getResourceFromJid( $(playerInfo).find('id').text() );

			ninecards.players[nick].score = $(playerInfo).find('score').text();
			console.log(ninecards.players[nick],'score',ninecards.players[nick].score);

			var usedCardsHTML = '';
			$(playerInfo).find('usedcards').each(function(index,usedCard){
				usedCardsHTML += '<span class="used-card">'+$(usedCard).text()+'</span>';
			});

			$('#'+ninecards.clearString(nick)+' p.ui-li-aside').html(usedCardsHTML).append(
				'<strong>/ '+ninecards.players[nick].score+'</strong>');

			$('#'+ninecards.clearString(nick)).buttonMarkup({ icon: 'clear' });
		});
	},















	/* core functions */

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





































/* application-specific jQuery handlers */

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
	ninecards.joinGame(
		$(this).attr('id'),
		$(this).attr('data-name'),
		function (result){
			console.log(result);
			jQuery.mobile.changePage(
				'#game', {
					transition: 'slide',
					changeHash: true
				}
			);
		}
	);

});


$(document).on('vclick', '#create-game-submit', function(event) {

	event.preventDefault();
	var gameName = $('#create-game-form #gamename').val();
	var numPlayers = $('#create-game-form #numplayers').val();
	var numRounds = $('#create-game-form #numrounds').val();

	if (gameName && numPlayers && numRounds) {
		ninecards.createGame(gameName, numPlayers, numRounds);
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


// $(document).on('vclick', '#message-submit', function() {

// 	event.preventDefault();
// 	var message = $('#message-form #message').val();
// 	if (message) {
// 		MX.core.sendGroupchatMessage(message);
// 	}
// 	$('#message-popup').popup('close');
// 	return false;

// });













/* core jQuery handlers */

$( window ).on('beforeunload', function() {
	MX.core.disconnect('Browser Window Closed');
});















/* Development Hooks */

$(document).on('vclick', '#load-defaults-button', function() {

    $.getJSON('localhost-settings.json', function(data){
		ninecards.fillSettingsForm(data);
    });

});

$(document).on('pageshow', '#games', function(){

	$('#create-game-form #gamename').val('asdf');
	$('#create-game-form #numplayers').val('4');
	$('#create-game-form #numrounds').val('4');

});
