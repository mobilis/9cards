/* mobilis 9Cards
 * 
 */


var mobilisninecards = {

	HTTPBIND : "http://mobilis-dev.inf.tu-dresden.de/http-bind",

	connect : function(uFullJid, uPassword, mBareJid) {
		Mobilis.utils.trace("Trying to establish a connection to Mobilis");
		Mobilis.core.connect(
			uFullJid, 
			uPassword, 
			mBareJid, 
			mobilisninecards.HTTPBIND, 
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
				console.log('listing games…');
				if ($(iq).find('mobilisService').length){
					$(iq).find('mobilisService').each( function() {
						Mobilis.core.SERVICES[$(this).attr('namespace')] = {
							'version': $(this).attr('version'),
							'jid': $(this).attr('jid'),
							'servicename' : $(this).attr('serviceName')
						};
						$('#game-list').append('<li><a class="available-game" id="'
												 + $(this).attr('jid') 
												 + '" href="lobby.html" data-transition="slide">' 
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

	return true;
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

