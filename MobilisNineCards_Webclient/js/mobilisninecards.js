/* mobilis 9Cards
 * 
 */


var mobilisninecards = {

	HTTPBIND : "http://mobilis-dev.inf.tu-dresden.de/http-bind",

	connect : function(userJid, userPassword, serverURL) {

		Mobilis.core.connect(
			serverURL, 
			userJid, 
			userPassword, 
			function(iq) {
			
				console.log('connect status:', iq);

				mobilisninecards.queryGames();

			},
			function(iq) {
			
				console.error('connect error:', iq);

			}
		);
	},

	queryGames : function() {

		Mobilis.core.mobilisServiceDiscovery(
			[Mobilis.mobilisninecards.NS.SERVICE],
			function(iq) {
				$('#game-list').empty().listview();
				console.log('listing games…',iq);
				if ($(iq).find('mobilisService').length){
					$(iq).find('mobilisService').each( function() {
						Mobilis.core.SERVICES[$(this).attr('namespace')] = {
							'version': $(this).attr('version'),
							'jid': $(this).attr('jid'),
							'servicename' : $(this).attr('serviceName')
						};
						$('#game-list').append('<li><a class="available-game" id="'
												 + $(this).attr('jid') 
												 + '" href="#game" data-transition="slide">' 
												 + $(this).attr('serviceName') 
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

		Mobilis.mobilisninecards.createServiceInstance(
			gameName,
			function(result){
				console.log('createServiceInstance result',result);

				var gameJid = ($(result).find('jidOfNewService').text());
				console.log(gameJid);

				Mobilis.mobilisninecards.ConfigureGame(
					gameJid, gameName, maxPlayers, numberOfRounds, 
					function(result){
						console.log('ConfigureGame result',result);
						mobilisninecards.joinGame(gameJid);
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
		console.log('joining game…');

		Mobilis.mobilisninecards.joinGame(gameJid,
			function(result){
				console.log('joinGame result',result);

				mobilisninecards.storeData({
					'chatRoom': 	($(result).find('ChatRoom').text()),
					'chatPassword': 	($(result).find('ChatPassword').text()),
					'creatorJid': 	($(result).find('CreatorJid').text())
				});

				Mobilis.connection.muc.join(
					mobilisninecards.loadData(['chatRoom']).chatRoom, 
					mobilisninecards.loadData(['username']).username, 
					function (message) {	 
						if ( from = Strophe.getResourceFromJid($(message).attr('from')) ){
							console.log([from + ' says: ', $(message).find('body').text() ]);
						}
						return true;
					}, 						
					function (presence){

						if ( from = Strophe.getResourceFromJid($(presence).attr('from')) ){
							console.log('presence: ' + from);
						}
						return true;
					},
					mobilisninecards.loadData(['chatPassword']).chatPassword
				);

			},
			function(error){
				console.error('joinGame error',error);
			}

		);
	},

	sendMessage : function (message) {
		console.log('sending message…');

		Mobilis.connection.muc.message(
			mobilisninecards.loadData(['chatRoom']).chatRoom, 
			mobilisninecards.loadData(['username']).username, 
			message, 
			'groupchat');
		return true;
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

	var settingsData = mobilisninecards.loadData(['username','gameserver','jid','password']);

	$('#settings-form #username').val(settingsData.username);
	$('#settings-form #gameserver').val(settingsData.gameserver);
	$('#settings-form #jid').val(settingsData.jid);
	$('#settings-form #password').val(settingsData.password);

	return true;
});




$(document).on('pageshow', '#games', function(){
	
	var connData = mobilisninecards.loadData(['gameserver','jid','password']);

	mobilisninecards.connect(
		connData.jid,
		connData.password,
		connData.gameserver
		);

	// return true;
});




$(document).on('vclick', '#settings-form #submit', function() {

	mobilisninecards.storeData({
		'username': 	$('#settings-form #username').val(),
		'gameserver': 	$('#settings-form #gameserver').val(),
		'jid': 			$('#settings-form #jid').val(),
		'password': 	$('#settings-form #password').val()
	});

	return true;
});




$(document).on('vclick', '#create-game-form #submit', function() {

	var gamename = $('#create-game-form #gamename').val();
	var numplayers = $('#create-game-form #numplayers').val();
	var numrounds = $('#create-game-form #numrounds').val();
	
	if (gamename && numplayers && numrounds) {
	
		mobilisninecards.createGame(gamename, numplayers, numrounds);
	}
});


$(document).on('vclick', '.available-game', function () {

	mobilisninecards.joinGame( $(this).attr('id') );
	
});


$(document).on('vclick', '#message-button', function() {
	$('#message-container').popup('open', {
		positionTo: 'window',
		theme: 'b',
		corners: true
	});
});



$(document).on('vclick', '#message-form #submit', function() {
	var message = $('#message-form #message').val();
	if (message) {
		// console.debug(message);
		mobilisninecards.sendMessage(message);
	}
	$('#message-container').popup('close');
	return false;
});

