/* mobilis 9Cards
 * 
 */


var m9cards = {

	connectServer : function() {

		connData = m9cards.loadData(['gameserver','jid','password']);

		console.log('would connect using data:', connData.gameserver,connData.jid,connData.password);

	},

	loadData : function(data) {
		var loadedObjects = {};
		$.each(data, function(index,value){
			loadedObjects[value] = localStorage.getItem('mobilis.9cards.'+value);
			console.log('loaded', value, ': ', loadedObjects[value]);
		});
		return loadedObjects;
	},

	storeData : function(storedObjects) {

		$.each(storedObjects, function(index,value){
			localStorage.setItem('mobilis.9cards.'+index, value);
			console.log('stored', index, ': ', value);
		});
		return true;
	}

}






/* jQuery Event Handlers */

$(document).on('pageshow', '#settings-page', function() {

	settingsData = m9cards.loadData(['username','gameserver','jid','password']);

	$('#settings-form #username').val(settingsData.username);
	$('#settings-form #gameserver').val(settingsData.gameserver);
	$('#settings-form #jid').val(settingsData.jid);
	$('#settings-form #password').val(settingsData.password);

	return true;
});




$(document).on('pageshow', '#games-page', function(){
	
	m9cards.connectServer();

	return true;
});




$(document).on('vclick', '#settings-form #submit', function() {

	m9cards.storeData({
		'username': 	$('#settings-form #username').val(),
		'gameserver': 	$('#settings-form #gameserver').val(),
		'jid': 			$('#settings-form #jid').val(),
		'password': 	$('#settings-form #password').val()
	});

	return true;
});
