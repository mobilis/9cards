
var ninecards = {




	connect : function(userJid, userPassword, serverURL) {

		Mobilis.core.connect(
			serverURL, 
			userJid, 
			userPassword, 
			function(result) {
			
				console.log('connect status:', result);

				ninecards.queryGames();

			},
			function(error) {
			
				console.error('connect error:', error);

			}
		);
	},





	queryGames : function() {

		Mobilis.core.mobilisServiceDiscovery(
			[Mobilis.ninecards.NS.SERVICE],
			function(result) {
				$('#game-list').empty().listview();
				console.log('listing games…',result);
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
				console.log('createServiceInstance result',result);

				var gameJid = ($(result).find('jidOfNewService').text());

				Mobilis.ninecards.ConfigureGame(
					gameJid, gameName, maxPlayers, numberOfRounds, 
					function(result){
						console.log('ConfigureGame result',result);
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
		console.log('joining game…');

		Mobilis.ninecards.joinGame(gameJid,
			function(result){
				console.log('joinGame result',result);

				ninecards.storeData({
					'chatRoom': 	($(result).find('ChatRoom').text()),
					'chatPassword': 	($(result).find('ChatPassword').text()),
					'creatorJid': 	($(result).find('CreatorJid').text())
				});

				Mobilis.connection.muc.join(
					ninecards.loadData(['chatRoom']).chatRoom, 
					ninecards.loadData(['username']).username, 
					function (message) {
						console.log(message);
					}, 						
					function (presence){
						console.log(presence);
						$(presence).find('item').each(function(index,value){
							var jid = $(value).attr('jid');
							var node = Strophe.getNodeFromJid(jid);
							var resource = Strophe.getResourceFromJid(jid);

							$('#players-list').append(
								'<li class="player" id="' + jid + '">'
								+ node + '/'+ resource +
								'<span class="ui-li-count">4</span></li>'
								).listview('refresh');
						});

					},
					ninecards.loadData(['chatPassword']).chatPassword
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
			ninecards.loadData(['chatRoom']).chatRoom, 
			ninecards.loadData(['username']).username, 
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

	// return true;
});




$(document).on('vclick', '#settings-form #submit', function() {

	ninecards.storeData({
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
	
		ninecards.createGame(gamename, numplayers, numrounds);
	}
});


$(document).on('vclick', '.available-game', function () {

	ninecards.joinGame( $(this).attr('id') );
	
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
		ninecards.sendMessage(message);
	}
	$('#message-container').popup('close');
	return false;
});

