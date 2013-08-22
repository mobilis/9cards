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
						ninecards.joinGame(gameJid);
						jQuery.mobile.changePage('#game', { transition: "slide"} );
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





	joinMuc : function() {

		var chatRoom = ninecards.loadData(['chatRoom']).chatRoom; 
		var userName = ninecards.loadData(['username']).username;
		var chatPassword = ninecards.loadData(['chatPassword']).chatPassword;

		Mobilis.connection.muc.join(
			chatRoom,
			userName,
			ninecards.onMessage,
			ninecards.onPresence,
			ninecards.onRoster,
			chatPassword,
			null, null
		);

	},





	processSystemMessage : function (message) {

		console.log('system message:', message);

		// var messageType = $(message).find('MessageType');
		// var messageString = $(message).find('MessageString');
		// console.log(messageString, messageType);

	},





	processChatMessage : function (message) {

		console.log('chat message:',message);

	},





	onMessage : function (message){

		var messageBody = $(message).find('body').text();
		var messageXml = $.parseXML('<xml>'+messageBody+'</xml>');

		if ( $(messageXml).find('IsSystemMessage').length > 0 ) {
			ninecards.processSystemMessage(messageXml);
		} else {
			ninecards.processChatMessage(messageBody);
		}

		return true;
	},





	onPresence : function (presence){
		
		var presenceJid = $(presence).find('item').attr('jid');

		if ($(presence).attr('type') == 'unavailable') {
			$('#'+ninecards.clearJid(presenceJid)).remove();
			$('#players-list').listview('refresh');
		} else {
			$('#players-list').append(
				'<li class="player" id="' + ninecards.clearJid(presenceJid) + '">'
				+ presenceJid +
				'<span class="ui-li-count">4</span></li>'
			).listview('refresh');
		}

		return true;
	},





	onRoster : function (roster){
		console.log('the roster:', roster);
		return true;
	},





	sendMessage : function (message) {
		Mobilis.connection.muc.message(
			ninecards.loadData(['chatRoom']).chatRoom, 
			null, // no private message
			message, 
			null, // no html markup
			'groupchat');
		return true;
	},





	sendCard : function(card, disableButton){
		console.log('card', card);

		ninecards.makeSystemMessage(card,'myType',function(xml){
			console.log(xml);
			ninecards.sendMessage(xml);
			disableButton();

		} );

	},


	makeSystemMessage : function(message,type,returnXml) {

		var xmlString = $build('MobilisMessage',{type:'PlayerInfo'})
			.c('IsSystemMessage').t('true').up()
			.c('MessageString').t(message).up()
			.c('MessageType').t(type).up()
			.toString();

		returnXml(xmlString);
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
		ninecards.sendMessage(message);
	}
	$('#message-popup').popup('close');
	return false;
});



$(document).on('vclick', '#numpad a', function(card) {
	var card  = $(this).attr('data-id');
	if (card) {
		ninecards.sendCard(card, function(){
			$('a[data-id='+card+']').addClass('ui-disabled');
		});
	}
	return false;
});
