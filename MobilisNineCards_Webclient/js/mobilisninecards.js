/* mobilis 9Cards
 * 
 */


var mobilisninecards = {

	HTTPBIND : "http://mobilis-dev.inf.tu-dresden.de/http-bind",

	connect : function(userJid, userPassword, serverURL) {
		console.log('connect');

		Mobilis.core.connect(
			serverURL, 
			userJid, 
			userPassword, 
			function(iq) {
			
				console.log('connect success:', iq);

				mobilisninecards.queryGames();

			},
			function(iq) {
			
				console.log('connect error:', iq);

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
												 + '" href="game.html" data-transition="slide">' 
												 + $(this).attr('serviceName') 
												 + '</a></li>');
					});
				} else {
					$('#game-list').append('<li>No games found</li>');
				}
				$('#game-list').listview('refresh');
			},
			function(iq) {
				console.log('error:',iq);
			}
		);

	},


	createConfigureGameRequest : function(GameName, MaxPlayers, NumberOfRounds) {
		return new Mobilis.mobilisninecards.ELEMENTS.ConfigureGameRequest(GameName, MaxPlayers, NumberOfRounds);
	},


	createGame : function(GameName, MaxPlayers, NumberOfRounds) {

		Mobilis.core.createServiceInstance({
				'serviceNamespace': Mobilis.mobilisninecards.NS.SERVICE, 
				'serviceName' : GameName,
				'servicePassword' : null
			},
			function(result){
				console.log('createServiceInstance:',result);
				var gameJid = ($(result).find('jidOfNewService').text());

				// mobilisninecards.storeData({'gameJid': gameJid});

				// var gameRequest = mobilisninecards.createConfigureGameRequest(GameName, MaxPlayers, NumberOfRounds);
				// console.log('gameRequest', gameRequest);

				Mobilis.mobilisninecards.ConfigureGame({
						'GameName': GameName, 
						'MaxPlayers': MaxPlayers,
						'NumberOfRounds': NumberOfRounds
					},
					gameJid,
					function(result){
						console.log('configure result',result);
						var myJid = mobilisninecards.loadData(['jid']).jid;
						console.log(myJid);
						// Mobiis.mobilisninecards.JoinGame({
						// 		'ChatRoom': gameJid,
						// 		'ChatPassword':'',
						// 		'CreatorJid': myJid
						// 	},
						// 	function(result){
						// 		console.log('join result',result);							
						// 	},
						// 	function(error){
						// 		console.log('join error',error);
						// 	},
						// 	function(timeout){
						// 		console.log('join timeout',timeout);
						// 	}
						// );
					},
					function(error){
						console.log('configure error',error);
					},
					function(timeout){
						console.log('configure timeout',timeout);
					}
				);


			},
			function(error){
				console.log('createServiceInstance:',error);
			}

		);
	},

	loadData : function(data) {
		var loadedObjects = {};
		$.each(data, function(index,value){
			loadedObjects[value] = localStorage.getItem('mobilis.9cards.'+value);
			console.log('loaded from localStorage:', value, '=', loadedObjects[value]);
		});
		return loadedObjects;
	},

	storeData : function(storedObjects) {

		$.each(storedObjects, function(index,value){
			localStorage.setItem('mobilis.9cards.'+index, value);
			console.log('stored in localStorage:', index, '=', value);
		});
		return true;
	}

}






/* jQuery Event Handlers */

$(document).on('pageshow', '#settings-page', function() {

	var settingsData = mobilisninecards.loadData(['username','gameserver','jid','password']);

	$('#settings-form #username').val(settingsData.username);
	$('#settings-form #gameserver').val(settingsData.gameserver);
	$('#settings-form #jid').val(settingsData.jid);
	$('#settings-form #password').val(settingsData.password);

	return true;
});




$(document).on('pageshow', '#games-page', function(){
	
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


