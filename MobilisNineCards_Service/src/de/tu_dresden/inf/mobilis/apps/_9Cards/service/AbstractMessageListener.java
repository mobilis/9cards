package de.tu_dresden.inf.mobilis.apps._9Cards.service;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tu_dresden.inf.mobilis.apps._9Cards.beans.PlayCardMessage;
import de.tu_dresden.inf.mobilis.apps._9Cards.beans.StartGameMessage;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Message;
import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.util.logging.Logger;
import java.io.StringReader;

public abstract class AbstractMessageListener implements PacketListener {

private final static Logger LOGGER = Logger.getLogger(AbstractMessageListener.class.getCanonicalName());

	@Override
	public final void processPacket(Packet packet) {
		if (packet instanceof Message) {
			Message msg = (Message) packet;
			if (null != msg.getBody()) {
				if (msg.getBody().startsWith("<")) {
					XMPPBean msgBean;
					XmlPullParser parser = new MXParser();
					
					try {
						parser.setFeature(MXParser.FEATURE_PROCESS_NAMESPACES, true);
						parser.setInput(new StringReader(msg.getBody()));
						if (msg.getBody().startsWith("<"+PlayCardMessage.CHILD_ELEMENT)) {
							msgBean = new PlayCardMessage();
							msgBean.fromXML(parser);
							msgBean.setFrom(msg.getFrom());
							msgBean.setTo(msg.getTo());
							msgBean.setId(msg.getPacketID());
							this.onPlayCardMessage((PlayCardMessage) msgBean);
						} else if (msg.getBody().startsWith("<"+StartGameMessage.CHILD_ELEMENT)) {
							msgBean = new StartGameMessage();
							msgBean.fromXML(parser);
							msgBean.setFrom(msg.getFrom());
							msgBean.setTo(msg.getTo());
							msgBean.setId(msg.getPacketID());
							this.onStartGameMessage((StartGameMessage) msgBean);
						}
					} catch (XmlPullParserException e) {
						LOGGER.severe(e.getLocalizedMessage());
						return;
					} catch (Exception e) {
						LOGGER.warning("Couldn't parse incoming Message Bean: "+msg.getBody());
						this.handleMessage(msg);
					}
				} else {
					this.handleMessage(msg);
				}
			}
		}
	}

	public abstract void onPlayCardMessage(PlayCardMessage inBean);
	
	public abstract void onStartGameMessage(StartGameMessage inBean);
	
	public void handleMessage(Message msg) {}
}