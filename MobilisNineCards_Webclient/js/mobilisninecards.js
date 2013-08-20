
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

		Mobilis.ninecards.joinGame(gameJid,
			function(result){
				console.log('joinGame result',result);

				ninecards.storeData({
					'chatRoom':     ($(result).find('ChatRoom').text()),
					'chatPassword':     ($(result).find('ChatPassword').text()),
					'creatorJid':   ($(result).find('CreatorJid').text())
				});

				ninecards.joinMuc();

				// Mobilis.connection.muc.join(
				//  ninecards.loadData(['chatRoom']).chatRoom,
				//  ninecards.loadData(['username']).username,
				//  function (message) {
				//      console.log('message',message);
				//      if ( from = Strophe.getResourceFromJid($(message).attr('from')) ){
				//          xhunt.log([from + ' says: ', $(message).find('body').text() ]);
				//      }

				//      // $(message).find('message').each(function(index,value){
				//      //  console.log('messagevalue',value);
				//      // });
				//      // $(message).find('body').each(function(index,value){
				//      //  console.log('bodyvalue',value);
				//      // });
				//  },
				//  function (presence){
				//      console.log('presence',presence);
				//      if ( from = Strophe.getResourceFromJid($(presence).attr('from')) ){
				//          console.log('presence: ' + from);
				//      }
				//  },
				//  function (roster){

				//      console.log('roster',roster);


				//      // var seen = [];

				//      // console.log(JSON.stringify(roster, function(key, val) {
				//      //  if (typeof val == "object") {
				//      //      if (seen.indexOf(val) >= 0)
				//      //          return;
				//      //      seen.push(val);
				//      //  }
				//      //  return val;
				//      // },2));

				//      // console.log(JSON.stringify(roster,null,2));

				//      // jQuery.each(roster,function(index,value){
				//      //  console.log(roster[index]);
				//      // });


				//      // for (var occupant in roster){
				//      //  console.log(occupant);

				//      //  $('#players-list').append(
				//      //      '<li class="player" id="' + occupant.jid + '">'
				//      //      + occupant.nick + ' ('+ occupant.affiliation + '/'+ occupant.role + ')' +
				//      //      '<span class="ui-li-count">4</span></li>'
				//      //  ).listview('refresh');
				//      // }
				//  },
				//  ninecards.loadData(['chatPassword']).chatPassword,
				//  null, null
				// );

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

		console.log('trying to join',chatRoom);

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

	onMessage : function (message){
		// console.log'message:', (message);
		console.log('message:', $(message).find('body').text() );
		return true;
	},

	onPresence : function (presence){
		
		console.log('presence:', presence);
		
		var presenceJid = $(presence).find('item').attr('jid');

		if ($(presence).attr('type') == 'unavailable') {
			// var id = '#'+ninecards.clearJid(presenceJid);
			// console.log(id);
			$('#'+ninecards.clearJid(presenceJid)).remove();
			$('#players-list').listview('refresh');
			// $.when($(id).remove()).then( console.log('removed it') );
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
		console.log('sending message…');

		Mobilis.connection.muc.message(
			ninecards.loadData(['chatRoom']).chatRoom, 
			ninecards.loadData(['username']).username, 
			message, 
			'groupchat');
		return true;
	},


	clearJid : function(jid){
		return jid = jid.replace(/@/g,'-').replace(/\./g,'-').replace(/\//g,'-');
	},


	loadData : function(data) {
		var loadedObjects = {};
		$.each(data, function(index,value){
			loadedObjects[value] = localStorage.getItem('mobilis.ninecards.'+value);
			console.log('loaded from localStorage:', value, '=', loadedObjects[value]);
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
		'username':     $('#settings-form #username').val(),
		'gameserver':   $('#settings-form #gameserver').val(),
		'jid':          $('#settings-form #jid').val(),
		'password':     $('#settings-form #password').val()
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

