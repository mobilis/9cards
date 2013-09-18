/*******************************************************************************
 * Copyright (C) 2013 Technische Universität Dresden
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: https://github.com/mobilis
 ******************************************************************************/
package de.tudresden.inf.rn.mobilis.services.ninecards.communication;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import de.tudresden.inf.rn.mobilis.services.ninecards.NineCardsService;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.ConfigureGameRequest;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.ConfigureGameResponse;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.IMobilisNineCardsOutgoing;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.MobilisNineCardsProxy;
import de.tudresden.inf.rn.mobilis.xmpp.beans.IXMPPCallback;
import de.tudresden.inf.rn.mobilis.xmpp.beans.ProxyBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanIQAdapter;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanProviderAdapter;

/**
 * The class for handling the raw XMPP bean connection.
 * 
 * Also implements the listener interface for receiving IQ events. The class that is interested in processing a IQ
 * event implements this interface, and the object created with that class is registered with a component using the
 * component's addIQListener() method. When the IQ event occurs, that object's appropriate method is invoked.
 * @see IQEvent
 */
public class IqConnection implements PacketListener {
	
	/** The NineCards service. */
	private NineCardsService mServiceInstance;
	/** The class which processes iq packets. */
	private IqPacketProcessor packetProcessor;
	
	/** The prototypes of registered XMPPBeans used for this service. */
	private Map<String,Map<String,XMPPBean>> beanPrototypes
		= Collections.synchronizedMap(new HashMap<String,Map<String,XMPPBean>>());
	
	/** The proxy used for instantiating requests and responses. */
	private MobilisNineCardsProxy _proxy;
	
	/** The waiting callbacks which are invoked later */
	private Map<String, IXMPPCallback<? extends XMPPBean>> _waitingCallbacks 
		= new HashMap<String, IXMPPCallback<? extends XMPPBean>>();
	
	/** The class specific Logger object. */
	private final static Logger LOGGER = Logger.getLogger(IqConnection.class.getCanonicalName());
	
	
	/**
	 * Instantiates a new Connection.
	 * @param serviceInstance the NineCards service
	 */
	public IqConnection(NineCardsService serviceInstance) {
		this.mServiceInstance = serviceInstance;
		this.packetProcessor = new IqPacketProcessor(mServiceInstance);
		this._proxy = new MobilisNineCardsProxy(_proxyOutgoingMapper);
		
		registerXMPPExtensions();
	}
	
	
	@Override
	public void processPacket(Packet packet) {
		
		// Check if the incoming Packet is of type IQ (BeanIQAdapter is just a wrapper)
		if(packet instanceof BeanIQAdapter) {
			
			// Convert packet to @see XMPPBean
			XMPPBean xmppBean = unpackBeanIQAdapter((BeanIQAdapter) packet);
			
			// If Bean is of type ERROR it will be logged
			if(xmppBean.getType() == XMPPBean.TYPE_ERROR)
				LOGGER.severe("ERROR: Bean of Type ERROR received: " + beanToString(xmppBean));
			
	    	// Else handle it in IqPacketProcessor
			else {
				LOGGER.info("processing incoming iq packet: " + beanToString(xmppBean));
				packetProcessor.processPacket(xmppBean);
			}
		}
	}
	
	
	/**
	 * Converts an XMPPBean to a string.
	 *
	 * @param bean the XMPPBean
	 * @return the XMPPBean as string
	 */
	private String beanToString(XMPPBean bean){
		String str = "XMPPBean: [NS="
			+ bean.getNamespace()
			+ " id=" + bean.getId()
			+ " from=" + bean.getFrom()
			+ " to=" + bean.getTo()
			+ " type=" + bean.getType()
			+ " payload=" + bean.payloadToXML();
		
		if(bean.errorCondition != null)
			str += " errorCondition=" + bean.errorCondition;
		if(bean.errorText != null)
			str += " errorText=" + bean.errorText;
		if(bean.errorType != null)
			str += " errorType=" + bean.errorType;
		
		str += "]";
		
		return str;
	}
	
	
	public MobilisNineCardsProxy getProxy() {
		return _proxy;
	}
	
	
	@SuppressWarnings("unchecked")
	public boolean handleCallback(XMPPBean inBean){
		@SuppressWarnings("rawtypes")
		IXMPPCallback callback = _waitingCallbacks.get(inBean.getId());

		if (callback != null)
			callback.invoke( inBean );
		
		return callback != null ? true : false;
	}
	
	
	/**
	 * Checks if 9Cards service is connected to XMPP server.
	 * @return true, if is connected
	 */
	public boolean isConnected(){
		return mServiceInstance.getAgent().getConnection() != null
			? mServiceInstance.getAgent().getConnection().isConnected()
			: false;
	}
	
	
	/**
	 * Register all XMBBBeabs labeled as XMPP extensions.
	 */
	private void registerXMPPExtensions() {		
		registerXMPPBean(new ConfigureGameRequest());
		registerXMPPBean(new ConfigureGameResponse());
	}
	
	
	/**
	 * Register an XMPPBean as prototype.
	 *
	 * @param prototype a basic instance of the XMPPBean
	 */
	private void registerXMPPBean(XMPPBean prototype) {
		
		// add XMPPBean to service provider to enable it in XMPP
		(new BeanProviderAdapter(new ProxyBean(prototype.getNamespace(), prototype.getChildElement()))).addToProviderManager();
		
		// add the prototype of the XMPPBean to the managed list of prototypes
		synchronized (this.beanPrototypes) {
			if (!this.beanPrototypes.keySet().contains(prototype.getNamespace()))
				this.beanPrototypes.put(prototype.getNamespace(), 
						Collections.synchronizedMap( new HashMap<String,XMPPBean>() ));
			
			this.beanPrototypes.get(prototype.getNamespace())
				.put(prototype.getChildElement(), prototype);
		}
	}
	
	
	/**
	 * Send a single XMPPBean using the routing information determined in the XMPPBean itself. 
	 * This function should be used for all classes to send a XMPPBean.
	 *
	 * @param bean the XMPPBean to send
	 * @return true, if sending was successful
	 */
	private boolean sendXMPPBean(XMPPBean bean) {
		bean.setFrom(mServiceInstance.getAgent().getFullJid());
		sendBean(bean);

		return true;
	}
	
	
	/**
	 * Send a single XMPPBean using the routing information determined in the XMPPBean itself. 
	 * This function doesn't store the XMPPBean to send in the list of waiting XMPPBeans 
	 * mWatingForResultBeans, so there will be no check for response. This function can 
	 * only be used by this Connection class is not qualified for normal usage of sending 
	 * XMPPBeans by other classes like the GameState classes.
	 *
	 * @param bean the XMPPBean to send
	 */
	private void sendBean(XMPPBean bean) {
		// just send the XMPPBean if XMPP connection is established and no FileTransfer is active
		if((mServiceInstance.getAgent() != null)
				&& (mServiceInstance.getAgent().getConnection() != null)
				&& (mServiceInstance.getAgent().getConnection().isConnected()) ) {
			
			// send XMPPBean
			mServiceInstance.getAgent().getConnection().sendPacket(new BeanIQAdapter(bean));
			LOGGER.info("sent IQ: " + beanToString(bean));
		}
	}
	
	
	/**
	 * Send a XMPPBean of type error using the original XMPPBean for routing information.
	 *
	 * @param resultBean the error XMPPBean with the specific error information
	 * @param fromBean the original XMPPBean
	 * @return true, if sending successful
	 */
	public boolean sendXMPPBeanError(XMPPBean resultBean, XMPPBean fromBean){
		resultBean.setTo(fromBean.getFrom());
		resultBean.setType(XMPPBean.TYPE_ERROR);
		resultBean.setId(fromBean.getId());
		
		return this.sendXMPPBean(resultBean);
	}

	
	/**
	 * Extracts an XMPPBean out of a given BeanIQAdapter.
	 * @param adapter
	 * @return
	 */
	public XMPPBean unpackBeanIQAdapter(BeanIQAdapter adapter){
		XMPPBean unpackedBean = null;
		
		if( beanPrototypes.containsKey( adapter.getNamespace() )
				&& beanPrototypes.get( adapter.getNamespace() )
					.containsKey( adapter.getChildElement() )){
			unpackedBean = adapter.unpackProxyBean( 
					beanPrototypes.get( adapter.getNamespace() )
						.get( adapter.getChildElement() ).clone() );
		}
		
		return unpackedBean;
	}
	

	/**
	 * The proxy for outgoing XMPP beans.
	 */
	private IMobilisNineCardsOutgoing _proxyOutgoingMapper = new IMobilisNineCardsOutgoing() {
		
		@Override
		public void sendXMPPBean(XMPPBean out) {
			IqConnection.this.sendXMPPBean(out);
		}
		
		@Override
		public void sendXMPPBean(XMPPBean out, IXMPPCallback<? extends XMPPBean> callback) {
			_waitingCallbacks.put(out.getId(), callback);
			sendXMPPBean(out);
		}
	};
}
