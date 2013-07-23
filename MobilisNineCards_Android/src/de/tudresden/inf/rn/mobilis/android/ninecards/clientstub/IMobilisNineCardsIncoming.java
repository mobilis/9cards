package de.tudresden.inf.rn.mobilis.android.ninecards.clientstub;

public interface IMobilisNineCardsIncoming {

	void onConfigureGame( ConfigureGameResponse in );

	void onConfigureGameError( ConfigureGameRequest in);

	void onJoinGame( JoinGameResponse in );

	void onJoinGameError( JoinGameRequest in);

}