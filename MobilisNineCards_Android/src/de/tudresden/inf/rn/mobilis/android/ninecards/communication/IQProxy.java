package de.tudresden.inf.rn.mobilis.android.ninecards.communication;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.ConfigureGameRequest;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.ConfigureGameResponse;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.IMobilisNineCardsOutgoing;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.IXMPPCallback;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.JoinGameRequest;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.JoinGameResponse;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.MobilisNineCardsProxy;
import de.tudresden.inf.rn.mobilis.android.ninecards.service.BackgroundService;
import de.tudresden.inf.rn.mobilis.mxa.IXMPPService;
import de.tudresden.inf.rn.mobilis.mxa.callbacks.IXMPPIQCallback;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPIQ;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.coordination.CreateNewServiceInstanceBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.coordination.MobilisServiceDiscoveryBean;

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
public class IQProxy {
	
	/** The application service running in the background. */
	private BackgroundService bgService;

	/** The XMPP service provided by MXA. */
	private IXMPPService iXMPPService;
	
	/** Contains templates for each IQ used in this application. */
	private Map<String, Map<String,XMPPBean>> beanPrototypes;
	
	/** Callbacks to be invoked when corresponding response arrives */
	private Map< String, IXMPPCallback< ? extends XMPPBean >> _waitingCallbacks;
	
	/** The proxy used for sending specific requests. */
	private MobilisNineCardsProxy _proxy;
	
	
	/**
	 * 
	 * @param appContext
	 * @param iXMPPService
	 */
	public IQProxy(BackgroundService bgService, IXMPPService iXMPPService) {
		this.bgService = bgService;
		this.iXMPPService = iXMPPService;
		
		beanPrototypes = Collections.synchronizedMap(new HashMap<String,Map<String,XMPPBean>>()); 
		_waitingCallbacks = new HashMap<String, IXMPPCallback<? extends XMPPBean>>();
		_proxy = new MobilisNineCardsProxy(_proxyOutgoingMapper);
		
		registerBeanPrototypes();
	}
	
	
	// =====================================================================================
	// Methods needed for sending
	// -------------------------------------------------------------------------------------	
	/**
	 * 
	 * @param xmppIQ
	 */
	public void sendIQ(XMPPIQ xmppIQ) {
		Log.v(this.getClass().getName(),
				"Sending IQ: ID=" + xmppIQ.packetID
				+ ", ns=" + xmppIQ.namespace
				+ ", type=" + xmppIQ.type
				+ ", to=" + xmppIQ.to
				+ ", payload=" + xmppIQ.payload);
		
		try {
			iXMPPService.sendIQ(
					new Messenger(mAckHandler),
					new Messenger(mResultHandler),
					1,	// oder 0? ka was das macht
					xmppIQ);
			
		} catch(RemoteException e) {
			Toast.makeText(bgService.getApplicationContext(), "Failed to send IQ!", Toast.LENGTH_LONG).show();
			Log.e(this.getClass().getName(), "Failed to send IQ " + xmppIQ.packetID + ": " + xmppIQ.payload);
		}
	}
	
	
	/**
	 * Send an XMPPBean of type ERROR.
	 *
	 * @param inBean the XMPPBean to reply with an ERROR. The payload will be copied.
	 */
	public void sendXMPPBeanError(XMPPBean inBean){
		XMPPBean resultBean = inBean.clone();
		resultBean.setTo(inBean.getFrom());
		resultBean.setFrom( bgService.getUserJid());
		resultBean.setType(XMPPBean.TYPE_ERROR);
		
		sendIQ(beanToIQ(resultBean, true));
	}
	

	private Handler mAckHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Log.i(IQProxy.class.getSimpleName(), "Ack received (" + msg.toString() + ")");
		}
	};
	
	
	private Handler mResultHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Log.i(IQProxy.class.getSimpleName(), "Result received (" + msg.toString() + ")");
		}
	};
	
	
	/**
	 * Convert an XMPPBean to an XMPPIQ to send it via the MXAProxy/MXA.
	 *
	 * @param bean the bean to convert
	 * @param mergePayload true if the playload should be merged
	 * @return the XMPPIQ
	 */
	public XMPPIQ beanToIQ(XMPPBean bean, boolean mergePayload) {
		
		int type;
		switch (bean.getType()) {
			case XMPPBean.TYPE_GET:		type = XMPPIQ.TYPE_GET; break;
			case XMPPBean.TYPE_SET:		type = XMPPIQ.TYPE_SET; break;
			case XMPPBean.TYPE_RESULT:	type = XMPPIQ.TYPE_RESULT; break;
			case XMPPBean.TYPE_ERROR:	type = XMPPIQ.TYPE_ERROR; break;
			default:					type = XMPPIQ.TYPE_GET;
		}
		
		XMPPIQ iq;
		
		if(mergePayload)
			iq = new XMPPIQ(bean.getFrom(), bean.getTo(), type, null, null, bean.toXML());
		else
			iq = new XMPPIQ(bean.getFrom(), bean.getTo(), type, bean.getChildElement(), bean.getNamespace(), bean.payloadToXML());
		
		iq.packetID = bean.getId();
		
		return iq;
	}
	
	
	// =====================================================================================
	// The following requests were not defined in the msdl, but are already offered by MXA
	// -------------------------------------------------------------------------------------
	/**
	 * Sends a MobilisServiceDiscoveryBean to the an MobilisNineCards game service.
	 * 
	 * IQ parameters:
	 * Type: GET
	 * From: players JID
	 * To: Mobilis Coordinator service
	 *
	 * @param serviceNamespace the namespace of a service to discover amount of instances 
	 * or null to discover all services running on the Mobilis server
	 */
	public void sendServiceDiscoveryIQ(String serviceNamespace) {
		MobilisServiceDiscoveryBean bean = new MobilisServiceDiscoveryBean(serviceNamespace, Integer.MIN_VALUE, false);
		
		bean.setType(XMPPBean.TYPE_GET);
		bean.setFrom(bgService.getUserJid());
		bean.setTo(bgService.getCoordinatorServiceJID());
		sendIQ(beanToIQ(bean, true));
		
		Log.v("IQProxy", "MobilisServiceDiscoveryBean send");
	}
	
	
	/**
	 * Sends a CreateNewServiceInstanceBean to the current Mobilis Coordinator service.
	 * 
	 * IQ parameters:
	 * Type: SET
	 * From: players JID
	 * To: Mobilis Coordinator service
	 *
	 * @param serviceNamespace the service namespace for to create
	 * @param serviceName the service name
	 * @param servicePassword the service password
	 */
	public void sendCreateNewServiceInstanceIQ(String serviceNamespace, String serviceName, String servicePassword){
		CreateNewServiceInstanceBean bean = 
			new CreateNewServiceInstanceBean(serviceNamespace, servicePassword);
		bean.setServiceName(serviceName);
		
		bean.setType(XMPPBean.TYPE_SET);
		bean.setFrom(bgService.getUserJid());
		bean.setTo(bgService.getCoordinatorServiceJID());
		sendIQ(beanToIQ(bean, true));
		
		Log.v("IQProxy", "Sent CreateNewServiceInstanceBean");
	}
	
	
	// =====================================================================================
	// Game-specific requests are sent like this, they were defined in the msdl as in-out-operations (from server point of view)
	// -------------------------------------------------------------------------------------
	public void configureGame(String toJid, String gameName, int maxPlayers, int numberOfRounds, IXMPPCallback<ConfigureGameResponse> callback) {
		_proxy.ConfigureGame(toJid, gameName, maxPlayers, numberOfRounds, callback);
	}
	
	
	public void joinGame(String toJid, IXMPPCallback<JoinGameResponse> callback) {
		_proxy.JoinGame(toJid, callback);
	}
	
	
	private IMobilisNineCardsOutgoing _proxyOutgoingMapper = new IMobilisNineCardsOutgoing() {
		
		@Override
		public void sendXMPPBean(XMPPBean out) {
			sendIQ(beanToIQ(out, true));
		}
		
		@Override
		public void sendXMPPBean(XMPPBean out, IXMPPCallback<? extends XMPPBean> callback) {
			_waitingCallbacks.put(out.getId(), callback);
			sendIQ(beanToIQ(out, true));
		}
	};
	
	
	// =====================================================================================
	// Register Request-/Response Beans (necessary before sending/receiving)
	// -------------------------------------------------------------------------------------
	/**
	 * Register XMPPBean prototypes used in this application to communicate with the MobilisNineCards service.
	 */
	// called from constructor
	private void registerBeanPrototypes(){
		registerXMPPBean(new ConfigureGameRequest());
		registerXMPPBean(new ConfigureGameResponse());
		registerXMPPBean(new JoinGameRequest());
		registerXMPPBean(new JoinGameResponse());
	}
	
	
	/**
	 * Register a prototype of an XMPPBean.
	 *
	 * @param prototype the prototype XMPPBean
	 */
	private void registerXMPPBean(XMPPBean prototype) {
		String namespace    = prototype.getNamespace();
		String childElement = prototype.getChildElement();
		
		synchronized (this.beanPrototypes) {
			if (!this.beanPrototypes.keySet().contains(namespace))
				this.beanPrototypes.put(namespace, Collections.synchronizedMap(new HashMap<String,XMPPBean>()));
			
			this.beanPrototypes.get(namespace).put(childElement, prototype);
		}
	}
	
	
	// =====================================================================================
	// Register Callbacks (necessary before sending/receiving)
	// -------------------------------------------------------------------------------------
	/**
	 * Register a global callback(AbstractCallback) which will be notified if an
	 * IQ related to the registered XMPPBeans is arriving in MXAProxy/MXA.
	 */
	// called from StartActivity.mMxaConnectedHandler
	public void registerCallbacks() {
		if (bgService.getMXAProxy().isConnectedToXMPPServer()) {
			try {
				for (Map.Entry<String, Map<String, XMPPBean>> entity : this.beanPrototypes.entrySet()) {
					for (Map.Entry<String, XMPPBean> subEntity : entity.getValue().entrySet()) {
						registerXMPPExtension(
								AbstractCallback,
								subEntity.getValue().getNamespace(),
								subEntity.getValue().getChildElement());
					}
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Register a callback extension which is used by the MXAProxy/MXA for 
	 * listening for this kind of IQ.
	 *
	 * @param callback the callback which should be notified if IQ is incoming
	 * @param beanNamespace the namespace of the IQ
	 * @param beanElement the root element tag of the IQ
	 * @return true, if callback was registered successful
	 * @throws RemoteException the remote exception if something goes wrong
	 */
	private boolean registerXMPPExtension(IXMPPIQCallback callback, String beanNamespace, String beanElement) throws RemoteException {
		boolean wasRegistered = false;
		
		if(bgService.getMXAProxy().isConnectedToXMPPServer()) {
			XMPPBean bean = getRegisteredBean(beanNamespace, beanElement);
			
			if(bean != null) {
				iXMPPService.registerIQCallback(callback, bean.getChildElement(), bean.getNamespace());
				
				Log.v(this.getClass().getName(), "child: " + bean.getChildElement() + " ns: " + bean.getNamespace());
				wasRegistered = true;
			}
		}
		
		return wasRegistered;
	}
	
	
	/**
	 * The Abstract callback to receive each IQ registered by an XMPPBean
	 * prototype. the incoming IQ will be converted to an XMPPBean and referred
	 * to the current running GameState inside the specific Activity.
	 */
	private IXMPPIQCallback AbstractCallback = new IXMPPIQCallback.Stub() {

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void processIQ(XMPPIQ iq) throws RemoteException {

			// ignore IQs from other games, just for safety
			if (!(iq.from.equals(bgService.getGameServiceJid()) || iq.from.equals(bgService.getCoordinatorServiceJID()))) {
				String msg = "Discarded IQ from unknown JID " + iq.from
						+ " to prevent GameService zombies from interfering"
						+ " - see IQProxy.AbstractCallback.processIQ()";
				Log.w(this.getClass().getName(), msg);
				return;
			}

			Log.v(this.getClass().getName(), "AbstractCallback: ID: " + iq.packetID + " type: "+ iq.type + " ns: " + iq.namespace + " payload: " + iq.payload);
			XMPPBean inBean = convertXMPPIQToBean(iq);

			if (_waitingCallbacks.containsKey(inBean.getId())) {
				IXMPPCallback callback = _waitingCallbacks.get(inBean.getId());

				if(callback != null) {
					try {
						callback.invoke(inBean);
					} catch(ClassCastException e) { e.printStackTrace(); }
				}
				
			} else {
				bgService.processIq(inBean);
			}
		}
	};
	
	
	/**
	 * Convert XMPPIQ to XMPPBean to simplify the handling of the IQ using the 
	 * beanPrototypes.
	 *
	 * @param iq the XMPPIQ
	 * @return the related XMPPBean or null if something goes wrong
	 */
	public XMPPBean convertXMPPIQToBean(XMPPIQ iq) {
		
		try {
			String childElement = iq.element;
			String namespace    = iq.namespace;
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(iq.payload));
			XMPPBean bean = null;
			
			Log.v(this.getClass().getSimpleName(), "prototypes contains ns: " + namespace + "? " +  this.beanPrototypes.containsKey(namespace));
			if(this.beanPrototypes.containsKey(namespace))
				Log.v(this.getClass().getSimpleName(), "prototypes contains ce: " + childElement + "? " +  this.beanPrototypes.get(namespace).containsKey(childElement));
			
			synchronized (this.beanPrototypes) {
				if (namespace != null && this.beanPrototypes.containsKey(namespace)
						&& this.beanPrototypes.get(namespace).containsKey(childElement) ) {
					
					bean = (this.beanPrototypes.get(namespace).get(childElement)).clone();					
					bean.fromXML(parser);
					
					bean.setId(iq.packetID);
					bean.setFrom(iq.from);
					bean.setTo(iq.to);
					
					switch (iq.type) {
						case XMPPIQ.TYPE_GET: bean.setType(XMPPBean.TYPE_GET); break;
						case XMPPIQ.TYPE_SET: bean.setType(XMPPBean.TYPE_SET); break;
						case XMPPIQ.TYPE_RESULT: bean.setType(XMPPBean.TYPE_RESULT); break;
						case XMPPIQ.TYPE_ERROR: bean.setType(XMPPBean.TYPE_ERROR); break;
					}
					
					return bean;
				}
			}
		} catch (Exception e) {
			Log.e(this.getClass().getSimpleName(), "failed to parse XMPPIQ to XMPPBean: " + e.getMessage());
		}
		
		return null;
	}
	
	
	/**
	 * Get a registered prototype XMPPBean by namespace and element tags.
	 *
	 * @param namespace the namespace of the XMPPBean
	 * @param element the element of hte XMPPBEan
	 * @return the registered XMPPBean or null if nothing was matched
	 */
	private XMPPBean getRegisteredBean(String namespace, String element){
		try{
			return beanPrototypes.get(namespace).get(element);
		}
		catch(NullPointerException e){
			Log.e(this.getClass().getSimpleName(), "Cannot find namespace '" + namespace + "' in list of bean prototypes!");
			
			return null;
		}
	}
	
	
	// =====================================================================================
	// Methods needed for unregistering (used when finishing the StartActivity)
	// -------------------------------------------------------------------------------------
	/**
	 * Unregister the global callback of all registered XMPPBeans in MXA. 
	 * Now, MXA will no more refer the IQs to this application and answers with 
	 * an 'Not supported' to the requester.
	 */
	public void unregisterCallbacks() {
		if (bgService.getMXAProxy().isConnectedToXMPPServer()) {
			try {
				unregisterXMPPExtension(AbstractCallback,
						MobilisServiceDiscoveryBean.NAMESPACE,
						MobilisServiceDiscoveryBean.CHILD_ELEMENT);

				unregisterXMPPExtension(AbstractCallback,
						CreateNewServiceInstanceBean.NAMESPACE,
						CreateNewServiceInstanceBean.CHILD_ELEMENT);

				for (Map.Entry<String, Map<String, XMPPBean>> entity : this.beanPrototypes.entrySet()) {
					for (Map.Entry<String, XMPPBean> subEntity : entity.getValue().entrySet()) {
						unregisterXMPPExtension(
								AbstractCallback,
								subEntity.getValue().getNamespace(),
								subEntity.getValue().getChildElement());
					}
				}
			} catch (RemoteException e) {
				Log.e(this.getClass().getName(), e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Unregister a single XMPPBean in MXA.
	 *
	 * @param callback the callback to be unregistered
	 * @param beanNamespace the namespace of the IQ
	 * @param beanElement the root element of the IQs payload
	 * @throws RemoteException the remote exception if something goes wrong
	 */
	private void unregisterXMPPExtension(IXMPPIQCallback callback, 
			String beanNamespace, String beanElement) throws RemoteException{
		if(bgService.getMXAProxy().isConnectedToXMPPServer()){
			XMPPBean bean = getRegisteredBean(beanNamespace, beanElement);
			
			if(bean != null)
				iXMPPService.unregisterIQCallback(callback,
						bean.getChildElement(), bean.getNamespace());
		}
	}
}
