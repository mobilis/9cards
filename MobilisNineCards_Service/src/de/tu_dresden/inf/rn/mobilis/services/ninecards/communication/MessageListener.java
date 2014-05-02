package de.tu_dresden.inf.rn.mobilis.services.ninecards.communication;

import org.jivesoftware.smackx.muc.MultiUserChat;

import de.tu_dresden.inf.mobilis.apps._9Cards.beans.CardPlayedMessage;
import de.tu_dresden.inf.mobilis.apps._9Cards.beans.GameStartsMessage;
import de.tu_dresden.inf.mobilis.apps._9Cards.beans.PlayCardMessage;
import de.tu_dresden.inf.mobilis.apps._9Cards.beans.StartGameMessage;
import de.tu_dresden.inf.mobilis.apps._9Cards.service.AbstractMessageListener;
import de.tu_dresden.inf.rn.mobilis.services.ninecards.Game;
import de.tu_dresden.inf.rn.mobilis.services.ninecards.NineCardsService;
import de.tu_dresden.inf.rn.mobilis.services.ninecards.Player;

/**
 * Logic implementation for incoming Beans (Messages) via MUC or private Chat.
 * 
 * @author Markus Wutzler
 * 
 */
public class MessageListener extends AbstractMessageListener {

	private final NineCardsService service;

	public MessageListener(NineCardsService service) {
		this.service = service;
	}

	/**
	 * This method checks whether the sender has already chosen a card in this
	 * round or if he has already played this card. If both is false, the card
	 * value is accepted and all other players are notified.
	 * 
	 * @param message
	 *            the {@link PlayCardMessage} to be processed
	 */
	@Override
	public void onPlayCardMessage(PlayCardMessage inBean) {
		Player player = service.getGame().getPlayer(inBean.getFrom());
		if (player != null) {

			// check if player already played a card this round
			if (player.getChosenCard() == -1) {

				if (!player.getUsedCards().contains(inBean.getCard())) {
					player.getUsedCards().add(inBean.getCard());
					player.setChosenCard(inBean.getCard());

					// inform other players that this player chose some card
					service.getMucConnection().sendMessagetoMuc(
							new CardPlayedMessage(service.getGame().getRound(), player.getMucJID()));

					service.getMucConnection().checkRoundOver();
				}
			}
		}
	}

	/**
	 * This method checks whether the game is in gamestate 'ready' and if the
	 * sender of the {@link StartGameMessage} is allowed to start the game. If
	 * both is true, the {@link MultiUserChat} room is locked and the game is
	 * being started.
	 * 
	 * @param message
	 *            the {@link StartGameMessage} to be processed
	 */
	@Override
	public void onStartGameMessage(StartGameMessage inBean) {
		if (service.getGame().getGameState() == Game.State.READY) {

			if (service.getMucConnection().isAdmin(inBean.getFrom())) {
				service.getMucConnection().lockMuc();
				service.getGame().setGameState(Game.State.PLAYING);
				service.getGame().startNewRound();

				// inform all players about start of game
				service.getMucConnection().sendMessagetoMuc(new GameStartsMessage(service.getSettings().getRounds()));
			}
		}
	}

}
