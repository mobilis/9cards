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
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.GetGameConfigurationRequest;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.GetGameConfigurationResponse;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.IMobilisNineCardsOutgoing;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.MobilisNineCardsProxy;
import de.tudresden.inf.rn.mobilis.xmpp.beans.IXMPPCallback;
import de.tudresden.inf.rn.mobilis.xmpp.beans.ProxyBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanIQAdapter;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanProviderAdapter;

/**
 * This class is responsible for managing raw XMPP bean communication.
 * 
 * @author Matthias Köngeter
 *
 */
public class IqConnection implements PacketListener
{
	
	/** The NineCards game service instance. */
	private NineCardsService mServiceInstance;
	/** The IqPacketProcessor object which is responsible for processing IQ packets. */
	private IqPacketProcessor mPacketProcessor;
	
	/** The prototypes of XMPPBeans which need to be registered. */
	private Map<String, Map<String, XMPPBean>> mBeanPrototypes
		= Collections.synchronizedMap(new HashMap<String,Map<String,XMPPBean>>());
	
	/** The proxy used for sending requests and responses. */
	private MobilisNineCardsProxy _proxy;
	
	/** The waiting callbacks which are to be invoked if a response is received. */
	private Map<String, IXMPPCallback<? extends XMPPBean>> mWaitingCallbacks 
		= new HashMap<String, IXMPPCallback<? extends XMPPBean>>();
	
	/** The class specific Logger object. */
	private final static Logger LOGGER = Logger.getLogger(IqConnection.class.getCanonicalName());
	
	
	/**
	 * Constructor for instantiating a new IqConnection object.
	 * 
	 * @param serviceInstance the ninecards game service instance
	 */
	public IqConnection(NineCardsService serviceInstance)
	{
		this.mServiceInstance = serviceInstance;
		this.mPacketProcessor = new IqPacketProcessor(mServiceInstance);
		this._proxy = new MobilisNineCardsProxy(_proxyOutgoingMapper);
		
		registerXMPPExtensions();
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see org.jivesoftware.smack.PacketListener#processPacket(org.jivesoftware.smack.packet.Packet)
	 */
	@Override
	public void processPacket(Packet packet)
	{
		// Check if the incoming Packet is of type IQ (BeanIQAdapter is just a wrapper)
		if(packet instanceof BeanIQAdapter) {
			
			// Convert packet to @see XMPPBean
			XMPPBean xmppBean = unpackBeanIQAdapter((BeanIQAdapter) packet);
			
			if(xmppBean.getType() == XMPPBean.TYPE_ERROR)
				LOGGER.severe("ERROR: Bean of Type ERROR received: " + beanToString(xmppBean));
			
			else {
				LOGGER.info("processing incoming iq packet: " + beanToString(xmppBean));
				mPacketProcessor.processPacket(xmppBean);
			}
		}
	}
	
	
	/**
	 * Converts an XMPPBean to a string.
	 *
	 * @param bean the XMPPBean to be transformed into a string
	 * @return the XMPPBean as string
	 */
	private String beanToString(XMPPBean bean)
	{
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
	
	
	/**
	 * Returns the instance of MobilisNineCardsProxy.
	 * 
	 * @return the MobilisNineCardsProxy object
	 */
	public MobilisNineCardsProxy getProxy()
	{
		return _proxy;
	}
	
	
	/**
	 * Invokes a callback corresponding to the bean's ID
	 * 
	 * @param inBean the bean to whose ID a callback shall be invoked
	 * @return true if a corresponding callback was found, else if not
	 */
	@SuppressWarnings("unchecked")
	public boolean handleCallback(XMPPBean inBean)
	{
		@SuppressWarnings("rawtypes")
		IXMPPCallback callback = mWaitingCallbacks.get(inBean.getId());

		if (callback != null)
			callback.invoke( inBean );
		
		return callback != null ? true : false;
	}
	
	
	/**
	 * Checks if ninecards service is connected to XMPP server.
	 * 
	 * @return true, if is connected
	 */
	public boolean isConnected()
	{
		return mServiceInstance.getAgent().getConnection() != null
			? mServiceInstance.getAgent().getConnection().isConnected()
			: false;
	}
	
	
	/**
	 * Register XMPPBeans. In this scenario we only have ConfigureGameRequest and ConfigureGameResponse.
	 */
	private void registerXMPPExtensions()
	{		
		registerXMPPBean(new ConfigureGameRequest());
		registerXMPPBean(new ConfigureGameResponse());
		registerXMPPBean(new GetGameConfigurationRequest());
		registerXMPPBean(new GetGameConfigurationResponse());
	}
	
	
	/**
	 * Register an XMPPBean as prototype.
	 *
	 * @param prototype a basic instance of the XMPPBean
	 */
	private void registerXMPPBean(XMPPBean prototype)
	{
		// add XMPPBean to service provider
		(new BeanProviderAdapter(new ProxyBean(prototype.getNamespace(), prototype.getChildElement()))).addToProviderManager();
		
		// add the prototype of the XMPPBean to the managed list of prototypes
		synchronized (this.mBeanPrototypes) {
			if (!this.mBeanPrototypes.keySet().contains(prototype.getNamespace()))
				this.mBeanPrototypes.put(prototype.getNamespace(), 
						Collections.synchronizedMap( new HashMap<String,XMPPBean>() ));
			
			this.mBeanPrototypes.get(prototype.getNamespace())
				.put(prototype.getChildElement(), prototype);
		}
	}
	
	
	/**
	 * Send a single XMPPBean using the routing information determined in the XMPPBean itself. 
	 * This function should be used for all classes to send a XMPPBean.
	 *
	 * @param bean the XMPPBean to send
	 * @return true, if sending was successful and false, if not
	 */
	private boolean sendXMPPBean(XMPPBean bean)
	{
		if((mServiceInstance.getAgent() != null)
				&& (mServiceInstance.getAgent().getConnection() != null)
				&& (mServiceInstance.getAgent().getConnection().isConnected())) {

			bean.setFrom(mServiceInstance.getAgent().getFullJid());
			mServiceInstance.getAgent().getConnection().sendPacket(new BeanIQAdapter(bean));
			LOGGER.info("sent IQ: " + beanToString(bean));
			
			return true;
		}

		else return false;
	}
	
	
	/**
	 * Send a XMPPBean of type error using the original XMPPBean for routing information.
	 *
	 * @param resultBean the error XMPPBean with the specific error information
	 * @param fromBean the original XMPPBean
	 * @return true, if sending successful and false, if sending failed
	 */
	public boolean sendXMPPBeanError(XMPPBean resultBean, XMPPBean fromBean)
	{
		resultBean.setTo(fromBean.getFrom());
		resultBean.setType(XMPPBean.TYPE_ERROR);
		resultBean.setId(fromBean.getId());
		
		return this.sendXMPPBean(resultBean);
	}

	
	/**
	 * Extracts an XMPPBean out of a given BeanIQAdapter.
	 * 
	 * @param adapter the BeanIQAdapter from which the XMPPBean shall be extracted
	 * @return the unpacked XMPPBean
	 */
	public XMPPBean unpackBeanIQAdapter(BeanIQAdapter adapter)
	{
		XMPPBean unpackedBean = null;
		
		if(mBeanPrototypes.containsKey(adapter.getNamespace())
				&& mBeanPrototypes.get(adapter.getNamespace()).containsKey(adapter.getChildElement())) {
			
			unpackedBean = adapter.unpackProxyBean(
					mBeanPrototypes.get(adapter.getNamespace()).get(adapter.getChildElement()).clone());
		}
		
		return unpackedBean;
	}
	

	/**
	 * The proxy for outgoing XMPP beans.
	 */
	private IMobilisNineCardsOutgoing _proxyOutgoingMapper = new IMobilisNineCardsOutgoing()
	{
		@Override
		public void sendXMPPBean(XMPPBean out)
		{
			IqConnection.this.sendXMPPBean(out);
		}
		
		@Override
		public void sendXMPPBean(XMPPBean out, IXMPPCallback<? extends XMPPBean> callback)
		{
			mWaitingCallbacks.put(out.getId(), callback);
			sendXMPPBean(out);
		}
	};
}
