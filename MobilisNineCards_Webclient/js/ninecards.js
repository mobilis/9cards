var ninecards = {




	players : {},




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





	queryGames : function() {

		Mobilis.core.mobilisServiceDiscovery(
			[Mobilis.ninecards.NS.SERVICE],
			function(result) {
				$('#game-list').empty().listview();

				if ($(result).find('mobilisService').length){
					$(result).find('mobilisService').each( function() {
						Mobilis.core.SERVICES[$(this).attr('namespace')] = {
							'version': $(this).attr('version'),
							'jid': $(this).attr('jid'),
							'servicename' : $(this).attr('serviceName')
						};
						$('#game-list').append(
							'<li><a class="available-game" id="'
							+ $(this).attr('jid')
							+ '" href="#game" data-transition="slide">'
							+ $(this).attr('serviceName')
							+ ' (' + Strophe.getResourceFromJid($(this).attr('jid')) + ')'
							+ '</a></li>');
					});
				} else {
					$('#game-list').append('<li>No games found</li>');
				}
				$('#game-list').listview('refresh');
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

						ninecards.joinMuc(gameJid, function(result){

							jQuery.mobile.changePage('#game', { 
								transition: 'slide',
								changeHash: 'false'
							});
							console.log(result);

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





	joinGame : function(gameJid){

		Mobilis.ninecards.joinGame(gameJid,
			function(result){

				ninecards.storeData({
					'chatRoom':     ($(result).find('ChatRoom').text()),
					'chatPassword':     ($(result).find('ChatPassword').text()),
					'creatorJid':   ($(result).find('CreatorJid').text())
				});

				ninecards.joinMuc();

			},
			function(error){
				console.error('joinGame error',error);
			}
		);
	},





	joinMuc : function(serviceJid, result) {

		var resource = Strophe.getResourceFromJid(serviceJid).toLowerCase();
		var domain = Strophe.getDomainFromJid(serviceJid);

		var roomJid = resource+'@conference.'+domain;
		ninecards.storeData({'roomJid': roomJid});

		var userName = ninecards.loadData(['username']).username;

		Mobilis.connection.muc.join(
			roomJid,
			userName,
			ninecards.onMessage,
			ninecards.onPresence,
			ninecards.onRoster
		);

		if (result) result('joined', roomJid);
	},





	onMessage : function (message){

		console.log(message);

		var messageBody = $(message).find('body').text();
		var messageXml = $.parseXML('<xml>'+messageBody+'</xml>');

		if ( $(messageXml).find('MobilisMessage').length > 0 ) {
			ninecards.processMobilisMessage(messageXml);
		} else {
			ninecards.processChatMessage(messageBody);
		}

		return true;
	},





	onPresence : function (presence){
		
		console.log(presence);

		var presenceJid = $(presence).find('item').attr('jid');

		if ( $(presence).attr('type') == 'unavailable' ) {
			$('#'+ninecards.clearJid(presenceJid)).remove();
			$('#players-list').listview('refresh');
		} else {
			
			if ( $('#'+ninecards.clearJid(presenceJid)).length < 1 ){ // list item existent?

				$('#players-list').append(
					'<li class="player" id="' + ninecards.clearJid(presenceJid) + '">'
					+ presenceJid +
					'<span class="ui-li-count">4</span></li>'
				).listview('refresh');

			}
		}

		return true;
	},




	onRoster : function (roster){
		console.log('the roster:', roster);
		ninecards.players = roster;
		$.each(ninecards.players, function(index,player){
			console.log(player);
			if (player.affiliation == 'owner'){
				ninecards.storeData({'serviceNick':player.nick});
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

		$(message).find('MobilisMessage').each(function(){

			var type = $(this).attr('type');

			console.log('type',type);

			switch (type) {
				case 'StartGameMessage' : ninecards.onStartGameMessage(message); break;
			}

		});
		return true;
	},




	onStartGameMessage : function (message) {
		console.log('Game Started',message);
		var rounds = $(message).find('rounds').text();
		console.log('rounds',rounds);
		var password = $(message).find('password').text();
		console.log('password',password);
	},




	sendMessage : function (nick, message) {
		Mobilis.connection.muc.message(
			ninecards.loadData(['roomJid']).roomJid, 
			nick,
			message, 
			null, // no html markup
			'chat');
		return true;
	},




	sendGroupchatMessage : function (message) {
		Mobilis.connection.muc.groupchat(
			ninecards.loadData(['roomJid']).roomJid, 
			message, 
			null // no html markup
			);
		return true;
	},




	startGame : function(){

		ninecards.buildMobilisMessage(null,'StartGameMessage', function(message){
			ninecards.sendMessage(
				ninecards.loadData(['serviceNick']).serviceNick,
				// 'marc',
				message
			);
			ninecards.sendGroupchatMessage(	'sent to service: '+message );
		});
	},

	


	sendCard : function(card, disableButton){
		console.log('card', card);

		ninecards.buildMobilisMessage(card,'PlayCardMessage',function(message){
			ninecards.sendMessage(
				ninecards.loadData(['serviceNick']).serviceNick,
				message
			);
			ninecards.sendGroupchatMessage(	'sent to service: '+message );
			disableButton();
		});

	},




	buildMobilisMessage : function(message,type,returnXml) {

		var xml = $build('MobilisMessage',{type:type});
		
		if (message) {
			xml = xml.t(message);		
		}

		returnXml( xml.toString() );
	},


	clearJid : function(jid){
		return jid = jid.replace(/@/g,'-').replace(/\./g,'-').replace(/\//g,'-');
	},





	loadData : function(data) {
		var loadedObjects = {};
		$.each(data, function(index,value){
			loadedObjects[value] = localStorage.getItem('mobilis.ninecards.'+value);
			// console.log('loaded from localStorage:', value, '=', loadedObjects[value]);
		});
		return loadedObjects;
	},





	storeData : function(storedObjects) {

		$.each(storedObjects, function(index,value){
			localStorage.setItem('mobilis.ninecards.'+index, value);
			console.log('stored in localStorage:', index, '=', value);
		});
		return true;
	}

}










/* jQuery Event Handlers */

$(document).on('pageshow', '#settings', function() {

	var settingsData = ninecards.loadData(['username','gameserver','jid','password']);

	$('#settings-form #username').val(settingsData.username);
	$('#settings-form #gameserver').val(settingsData.gameserver);
	$('#settings-form #jid').val(settingsData.jid);
	$('#settings-form #password').val(settingsData.password);

	return true;
});




$(document).on('pageshow', '#games', function(){
	
	var connData = ninecards.loadData(['gameserver','jid','password']);

	ninecards.connect(
		connData.jid,
		connData.password,
		connData.gameserver
	);

});




$(document).on('vclick', '#settings-submit', function() {

	ninecards.storeData({
		'username':     $('#settings-form #username').val(),
		'gameserver':   $('#settings-form #gameserver').val(),
		'jid':          $('#settings-form #jid').val(),
		'password':     $('#settings-form #password').val()
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
	$('#create-game-popup').popup('close');
	return false;
});



$(document).on('vclick', '.available-game', function () {

	ninecards.joinGame( $(this).attr('id') );
	
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
			$('a[data-id='+card+']').addClass('ui-disabled');
		});
	}
	return false;
});

$(document).on('vclick', '#startgame-button', function(event){

	event.preventDefault();

	console.log('start the game');
	// $('#startgame-button').remove();
	ninecards.startGame();
	return false;

});
