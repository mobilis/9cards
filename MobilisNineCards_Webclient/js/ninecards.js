var ninecards = {




	connect : function(userJid, userPassword, serverURL) {

		Mobilis.core.connect(
			serverURL, 
			userJid, 
			userPassword, 
			function(status) {
				if (status == Mobilis.core.Status.CONNECTED) {
					ninecards.queryGames();
				}
			}
		);
	},



	disconnect : function(){
		Mobilis.connection.disconnect();
		jQuery.mobile.changePage(
			'#start', {
				transition: 'slide',
				reverse: true,
				changeHash: true
			}
		);

	},



	queryGames : function() {

		Mobilis.core.mobilisServiceDiscovery(
			[Mobilis.ninecards.NS.SERVICE],
			function(result) {
				$('#games-list').empty().listview();

				if ($(result).find('mobilisService').length){
					$(result).find('mobilisService').each( function() {
						Mobilis.core.SERVICES[$(this).attr('namespace')] = {
							'version': $(this).attr('version'),
							'jid': $(this).attr('jid'),
							'servicename' : $(this).attr('serviceName')
						};
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

	},





	createGame : function(gameName, maxPlayers, numberOfRounds) {

		Mobilis.ninecards.createServiceInstance(
			gameName,
			function(result){

				var gameJid = ($(result).find('jidOfNewService').text());

				Mobilis.ninecards.ConfigureGame(
					gameJid, gameName, maxPlayers, numberOfRounds, 
					function(result){
						ninecards.joinGame(gameJid, gameName, function(result){
							console.log(result);
							$('#game-area').append(
								'<a href="#" id="startgame-button" data-theme="a" data-role="button">Start Game</a>'
							).trigger('create');
						});
					},
					function(error){
						console.error('ConfigureGame error',error);
					}
				);
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
		ninecards.joinMuc(
			chatroom, 
			ninecards.onMessage,
			ninecards.onPresence,
			ninecards.onRoster,
			function(result){

				jQuery.mobile.changePage('#game', { 
					transition: 'slide',
					changeHash: true
				});

				$('#game h1').append(gameName);

				res = result;
			}
		);
		if (result) result(res);
	},





	joinMuc : function(room, onMessage, onPresence, onRoster, result) {

		Mobilis.connection.muc.join(
			room,
			jQuery.jStorage.get('settings').username,
			onMessage,
			onPresence,
			onRoster
		);

		if (result) result('joined', room);
	},



	leaveMuc : function(room, onLeft){
		Mobilis.connection.muc.leave(
			room,
			jQuery.jStorage.get('settings').username
		);
		onLeft();
	},



	leaveGame : function(){
		ninecards.leaveMuc(
			jQuery.jStorage.get('chatroom'),
			function(){
				$('#players-list').empty();
				$('#startgame-button').remove();
				jQuery.mobile.changePage(
					'#games', {
						transition: 'slide',
						reverse: true,
						changeHash: true
					}
				);
				console.log('left game');
			}
		);
	},



	onMessage : function (message){

		// console.log(message);

		var messageBody = $(message).find('body').text();
		// console.log('messageBody',messageBody);
		var html = $.parseHTML(messageBody)[0];
		// console.log('html',html);

		if ( $(html).attr('type') ) {
			ninecards.processMobilisMessage(html);
		} else {
			ninecards.processChatMessage(messageBody);
		}
		return true;
	},





	onPresence : function (presence){
		
		console.log(presence);

		var type = $(presence).attr('type');

		switch (type) {
			case 'error' : ninecards.onPresenceError(presence); break;
		}

		return true;
	},




	onRoster : function (roster){

		console.log('roster:', roster);
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





	processChatMessage : function (message) {

		console.log('chat message:',message);
		return true;

	},





	processMobilisMessage : function (message) {

		console.log('MobilisMessage:', message);

		var type = $(message).attr('type');

		console.log('type',type);

		switch (type) {
			case 'StartGameMessage' : ninecards.onStartGameMessage(message); break;
			case 'CardPlayedMessage' : ninecards.onCardPlayedMessage(message); break;
			case 'RoundCompleteMessage' : ninecards.onRoundCompleteMessage(message); break;
			case 'GameOverMessage' : ninecards.onGameOverMessage(message); break;
		}

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
		$('#round-count').css('display','block');

	},




	onCardPlayedMessage : function (message) {
		var nick = Strophe.getResourceFromJid( $(message).find('player').text() );
		console.log('nick',nick);
		$('#'+ninecards.clearString(nick)).buttonMarkup({ icon: 'check' });
	},




	onRoundCompleteMessage : function (message) {
		ninecards.updateScore(message);
		ninecards.game.round = parseInt($(message).find('round').text())+1;
		$('#round-count #round').html(ninecards.game.round);
	},


	updateScore : function(message){
		$(message).find('playerinfo').each( function(index,playerInfo){
			console.log('playerinfo',playerInfo);
			var nick = Strophe.getResourceFromJid( $(playerInfo).find('jid').text() );

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

	

	onGameOverMessage : function (message) {

		ninecards.updateScore(message);

		var winner = Strophe.getResourceFromJid( $(message).find('winner').text() );
		var score = ninecards.players[winner].score;
		$('#round-count').html('<span>Game Over</span>');

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




	sendMessage : function (nick, message) {
		Mobilis.connection.muc.message(
			jQuery.jStorage.get('chatroom'),
			nick,
			message, 
			null, // no html markup
			'chat');
		return true;
	},




	sendGroupchatMessage : function (message) {
		Mobilis.connection.muc.groupchat(
			jQuery.jStorage.get('chatroom'),
			message, 
			null // no html markup
			);
		return true;
	},




	startGame : function(){

		ninecards.buildMobilisMessage(null,'StartGameMessage', function(message){
			ninecards.sendMessage(
				jQuery.jStorage.get('serviceNick'),
				// 'marc',
				message
			);
			ninecards.sendGroupchatMessage(	'sent to '+jQuery.jStorage.get('serviceNick')+': '+message );
		});
	},

	


	sendCard : function(card, disableButton){
		console.log('card', card);
		var round = 1; // TODO
		ninecards.buildMobilisCardMessage(card,round,'PlayCardMessage',function(message){
			ninecards.sendMessage(
				jQuery.jStorage.get('serviceNick'),
				message
			);
			ninecards.sendGroupchatMessage(	'sent to '+jQuery.jStorage.get('serviceNick')+': '+message );
			disableButton();
		});

	},




	buildMobilisMessage : function(message,type,returnXml) {

		var xml = $build('mobilismessage',{type:type});
		
		if (message) {
			xml = xml.t(message);		
		}

		returnXml( xml.toString() );
	},


	buildMobilisCardMessage : function(card,round,type,returnXml) {

		var xml = $build('mobilismessage',{type:type})
				.c('round').t(round).up()
				.c('card').t(card);

		returnXml( xml.toString() );
	},


	clearString : function(string){
		return string.replace(/@/g,'-').replace(/\./g,'-').replace(/\//g,'-');
	},




	disableButton : function(button){
			$('a[data-id='+button+']').addClass('ui-disabled');
			console.log('button', button, 'disabled');
	}

}










/* jQuery Event Handlers */

$(document).on('pageshow', '#settings', function() {

	var settings = jQuery.jStorage.get('settings');

	$('#settings-form #username').val(settings.username);
	$('#settings-form #gameserver').val(settings.gameserver);
	$('#settings-form #chatservice').val(settings.chatservice);
	$('#settings-form #jid').val(settings.jid);
	$('#settings-form #password').val(settings.password);

	return true;
});




$(document).on('pageshow', '#games', function(){

	if ( Mobilis.connection && Mobilis.connection.connected ) {
		ninecards.queryGames();
	} else {
		var settings = jQuery.jStorage.get('settings');
		ninecards.connect(
			settings.jid,
			settings.password,
			settings.gameserver
		);
	}

});




$(document).on('vclick', '#refresh-games-button', function() {
	// TODO auslagern, weil doppelt
	if ( Mobilis.connection && Mobilis.connection.connected ) {
		ninecards.queryGames();
	} else {
		var settings = jQuery.jStorage.get('settings');
		ninecards.connect(
			settings.jid,
			settings.password,
			settings.gameserver
		);
	}

});



$(document).on('vclick', '#settings-submit', function() {

	jQuery.jStorage.set('settings', {
		'username':   $('#settings-form #username').val(),
		'gameserver': $('#settings-form #gameserver').val(),
		'chatservice': $('#settings-form #chatservice').val(),
		'jid':        $('#settings-form #jid').val(),
		'password':   $('#settings-form #password').val()
	});

	return true;
});







$(document).on('vclick', '#create-game-submit', function() {

	var gameName = $('#create-game-form #gamename').val();
	var numPlayers = $('#create-game-form #numplayers').val();
	var numRounds = $('#create-game-form #numrounds').val();
	
	if (gameName && numPlayers && numRounds) {
		ninecards.createGame(gameName, numPlayers, numRounds);
	}
	$('#creategame-popup').popup('close');
	return false;
});



$(document).on('vclick', '.available-game', function () {

	ninecards.joinGame( $(this).attr('id'), $(this).attr('data-name'), function (result){
		console.log(result);
	});
	
});



$(document).on('vclick', '#message-button', function() {
	$('#message-popup').popup('open', {
		positionTo: 'window',
		theme: 'b',
		corners: true
	});
});



$(document).on('vclick', '#message-submit', function() {
	var message = $('#message-form #message').val();
	if (message) {
		ninecards.sendGroupchatMessage(message);
	}
	$('#message-popup').popup('close');
	return false;
});



$(document).on('vclick', '#numpad a', function(card) {
	card.preventDefault();
	var card  = $(this).attr('data-id');
	if (card) {
		ninecards.sendCard(card, function(){
			ninecards.disableButton(card);
		});
	}
	return false;
});

$(document).on('vclick', '#startgame-button', function(event){

	event.preventDefault();
	$('#startgame-button').remove();
	ninecards.startGame();
	return false;

});

$(document).on('vclick', '#exitgame-button', function(event){

	event.preventDefault();
	ninecards.leaveGame();
	return false;

});

$(document).on('vclick', '#exitgames-button', function(event){

	event.preventDefault();
	ninecards.disconnect();
	return false;

});


$(document).on('ready', function(){

	if ( !jQuery.jStorage.index().length ){

		$.getJSON('settings.json', function(data){
			jQuery.jStorage.set('settings', {
			    "username":    window.navigator.appCodeName,
			    "gameserver":  data.gameserver,
			    "chatservice": data.chatservice,
			    "jid":         data.jid+'/'+window.navigator.appCodeName,
			    "password":    ""
			});
			console.log('imported settings');
		});

	}
});