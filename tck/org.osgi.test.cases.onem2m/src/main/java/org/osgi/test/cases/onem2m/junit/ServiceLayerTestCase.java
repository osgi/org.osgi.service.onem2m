/*******************************************************************************
 * Copyright (c) Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0 
 *******************************************************************************/
package org.osgi.test.cases.onem2m.junit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.osgi.service.onem2m.dto.Constants.RT_AE;
import static org.osgi.service.onem2m.dto.Constants.RT_CSEBase;
import static org.osgi.service.onem2m.dto.Constants.RT_container;
import static org.osgi.service.onem2m.dto.Constants.RT_subscription;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.onem2m.NotificationListener;
import org.osgi.service.onem2m.OneM2MException;
import org.osgi.service.onem2m.ServiceLayer;
import org.osgi.service.onem2m.dto.FilterCriteriaDTO.FilterUsage;
import org.osgi.service.onem2m.dto.FilterCriteriaDTO;
import org.osgi.service.onem2m.dto.NotificationDTO;
import org.osgi.service.onem2m.dto.NotificationEventDTO;
import org.osgi.service.onem2m.dto.RequestPrimitiveDTO;
import org.osgi.service.onem2m.dto.ResourceDTO;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.util.promise.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test Case for oneM2M
 * 
 * '-' is a special character to indicate CSEBase, that is root of resource
 * tree.
 * 
 */
public class ServiceLayerTestCase {

	@InjectBundleContext
	BundleContext			context;

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceLayerTestCase.class);

	private ServiceLayer serviceLayerService = null;
	// private String uri = null;
	private BundleContext con = null;

	@BeforeEach
	public void setUp() throws Exception {
		if (serviceLayerService == null) {
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//				fail();
//				return;
//			}
			con = context;
			// Get ServiceLayerFactory
			ServiceReference<?> serviceLayerFactory = con.getServiceReference(ServiceLayer.class.getName());
			serviceLayerService = (ServiceLayer) con.getService(serviceLayerFactory);

			if (serviceLayerService == null) {
				fail("");
				return;
			}
		}
	}

	@AfterEach
	public void tearDown() throws Exception {
		cleanup();
	}

	@SuppressWarnings("unused")
	@Test
	public void testRetrieve1() {
		LOGGER.info("----Start RETRIEVE Test 1----");

		ResourceDTO res = null;

		try {
			// retrieve with short cut of csebase.
			res = serviceLayerService.retrieve("-").getValue();
		} catch (Exception e) {
			e.printStackTrace();
			assertThat((Object) null).isNotNull();// TODO
			res = null;
		}

		// Check result
		LOGGER.info("Response Data:\n" + res.toString());

		assertThat(res).as("Response is Null.").isNotNull();
		assertThat(RT_CSEBase).isEqualTo(res.resourceType.intValue());
		assertThat(res.resourceID).as("ResourceID is Null.").isNotNull();
		assertThat(res.lastModifiedTime).as("LastModifiedTime is Null").isNotNull();
		assertThat(res.creationTime).as("CreationTime is Null.").isNotNull();

		assertThat(res.attribute).as("CSE attribute is null.").isNotNull();
		String cseid = (String) res.attribute.get("CSE-ID");
		assertThat(cseid).as("CSE-ID is null.").isNotNull();
		String csebaseName = res.resourceName;

		try {
			// retrieve with short cut of csebase with SP-relative name.
			res = serviceLayerService.retrieve("/" + cseid + "/-").getValue();
		} catch (Exception e) {
			e.printStackTrace();
			assertThat((Object) null).isNotNull();// TODO
			res = null;
		}

		// Check result
		LOGGER.info("Response Data:\n" + res.toString());

		assertThat(res).as("Response is Null.").isNotNull();
		assertThat(RT_CSEBase).isEqualTo(res.resourceType.intValue());
		assertThat(res.resourceID).as("ResourceID is Null.").isNotNull();
		assertThat(res.lastModifiedTime).as("LastModifiedTime is Null").isNotNull();
		assertThat(res.creationTime).as("CreationTime is Null.").isNotNull();

		assertThat(res.attribute).as("CSE's attribute is null.").isNotNull();
		String cseid2 = (String) res.attribute.get("CSE-ID");
		assertThat(cseid2).as("CSE-ID is null.").isNotNull();
		assertThat(cseid2).isEqualTo(cseid);

		try {
			// retrieve regular SP-relative name.
			res = serviceLayerService.retrieve("/" + cseid + "/" + csebaseName).getValue();
		} catch (Exception e) {
			e.printStackTrace();
			assertThat((Object) null).isNotNull();// TODO
			res = null;
		}

		// Check result
		LOGGER.info("Response Data:\n" + res.toString());

		assertThat(res).as("Response is Null.").isNotNull();
		assertThat(RT_CSEBase).isEqualTo(res.resourceType.intValue());
		assertThat(res.resourceID).as("ResourceID is Null.").isNotNull();
		assertThat(res.lastModifiedTime).as("LastModifiedTime is Null").isNotNull();
		assertThat(res.creationTime).as("CreationTime is Null.").isNotNull();

		assertThat(res.attribute).as("CSE's attribute is null.").isNotNull();
		String cseid3 = (String) res.attribute.get("CSE-ID");
		assertThat(cseid3).as("CSE-ID is null.").isNotNull();
		assertThat(cseid3).isEqualTo(cseid);

		LOGGER.info("----RETRIEVE Test 1 is complete----");
	}

	@Test
	public void testRetrieve2() {
		LOGGER.info("----Start RETRIEVE Test 2----");

		ResourceDTO res = null;

		String uri = "/in-cse2/-";

		try {
			res = serviceLayerService.retrieve(uri).getValue();
		} catch (Exception e) {
			res = null;
		}

		// Check result
		assertThat(res).isNull();

		LOGGER.info("----RETRIEVE Test 2 is complete----");
	}

	@Test
	public void testCreate1() {
		LOGGER.info("----Start CREATE Test 1----");

		ResourceDTO req = new ResourceDTO();
		ResourceDTO res = null;

		String uri = "-";
		req.resourceName = "cnt1";
		req.resourceType = RT_container;

		try {
			res = serviceLayerService.create(uri, req).getValue();
		} catch (Exception e) {
			res = null;
		}

		// Check result
		assertThat(res).as("Response is Null.").isNotNull();
		assertThat(res.resourceName).isEqualTo(req.resourceName);
		assertThat(res.resourceID).as("ResourceID IS  is Null.").isNotNull();
		assertThat(res.lastModifiedTime).as("LastModifiedTime is Null").isNotNull();
		assertThat(res.creationTime).as("CreationTime is Null.").isNotNull();

		LOGGER.info("----CREATE Test 1 is complete----");
	}

	/**
	 * Duplicate creation. 2nd call will fail.
	 */
	@Test
	public void testCreate2() {
		LOGGER.info("----Start CREATE Test 2----");

		ResourceDTO req = new ResourceDTO();
		@SuppressWarnings("unused")
		ResourceDTO res = null;

		String uri = "-";
		req.resourceName = "cnt1";
		req.resourceType = RT_container;

		Throwable th = null;
		try {
			res = serviceLayerService.create(uri, req).getValue();

			Promise<ResourceDTO> pr = serviceLayerService.create(uri, req);

			th = pr.getFailure();
			// res = pr.getValue();
			LOGGER.info("Throwable:" + th);
			// LOGGER.info("Response:"+res);
		} catch (Exception e) {
			LOGGER.warn("Exception Caught ex:" + e);
			e.printStackTrace();
			fail("");
		}

		// Check result

		// assertNull(res);
		assertThat(th).isNotNull();
		assertThat(th.getClass()).isEqualTo(OneM2MException.class);
		OneM2MException oex = (OneM2MException) th;
		assertThat(oex.getErrorCode()).isEqualTo(4105);

		LOGGER.info("----CREATE Test 2 is complete----");
	}

	/**
	 * create under wrong path.
	 */
	@Test
	public void testCreate3() {
		LOGGER.info("----Start CREATE Test 3----");

		ResourceDTO req = new ResourceDTO();
		ResourceDTO res = new ResourceDTO();

		String uri = "/in-cse2/-";
		req.resourceName = "cnt2";
		req.resourceType = RT_container;

		try {
			res = serviceLayerService.create(uri, req).getValue();
		} catch (Exception e) {
			res = null;
		}

		// Check result
		assertThat(res).isNull();// TODO check return code.

		LOGGER.info("----CREATE Test 3 is complete----");
	}

	@Test
	public void testCreate4AE() {
		LOGGER.info("----Start CREATE Test 4 for AE----");

		ResourceDTO r = new ResourceDTO();
		ResourceDTO res = null;

		String uri = "-";
		r.resourceName = "ae1";
		r.resourceType = RT_AE;
		r.attribute = new HashMap<String, Object>();
		r.attribute.put("App-ID", "TESTAPP");
		r.attribute.put("requestReachability", true);

		try {
			res = serviceLayerService.create(uri, r).getValue();
		} catch (Exception e) {
			res = null;
		}

		// Check result
		assertThat(res).as("Response is Null.").isNotNull();
		assertThat(res.resourceName).isEqualTo(r.resourceName);
		assertThat(res.resourceID).as("ResourceID IS  is Null.").isNotNull();
		assertThat(res.lastModifiedTime).as("LastModifiedTime is Null").isNotNull();
		assertThat(res.creationTime).as("CreationTime is Null.").isNotNull();
		assertThat(res.attribute.get("pointOfAccess")).as("pointOfAccess is Null.").isNotNull();
		assertThat(res.attribute.get("AE-ID")).as("AE-ID is Null.").isNotNull();
		assertThat(res.creationTime).as("CreationTime is Null.").isNotNull();

		LOGGER.info("----CREATE Test 4 is complete----");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUpdate1() {
		LOGGER.info("----Start Update Test 1----");

		ResourceDTO req = new ResourceDTO();
		ResourceDTO res = null;

		String uri = "-";
		req.resourceName = "updateCnt";
		req.resourceType = RT_container;

		try {
			res = serviceLayerService.create(uri, req).getValue();
		} catch (Exception e) {
			fail("");
		}
		System.out.println(res.toString());

		ResourceDTO req2 = new ResourceDTO();
		String lblText = "updateTest";

		uri = "-/updateCnt";
		req2.resourceName = res.resourceName;
		req2.resourceType = res.resourceType;
		req2.resourceID = res.resourceID;
		req2.attribute = new HashMap<String, Object>();
		List<String> lbl = new ArrayList<String>();
		lbl.add(lblText);
		req2.attribute.put("labels", lbl);

		try {
			Thread.sleep(1200);// to ensure second digit to change
			serviceLayerService.update(uri, req2).getValue();
		} catch (Exception e) {
			fail("");
		}

		LOGGER.info("finished first update");// TODO remove this.

		ResourceDTO res3 = null;

		// retrieve updated resource
		uri = "-/updateCnt";

		try {
			res3 = serviceLayerService.retrieve(uri).getValue();
		} catch (Exception e) {
			fail("");
		}

		// Check result
		System.out.println(res3.toString());
		assertThat(res3.resourceName).isEqualTo(res.resourceName);
		assertThat(res3.resourceType).isEqualTo(res.resourceType);
		assertThat(res3.resourceID).isEqualTo(res.resourceID);
		assertThat(res3.creationTime).isEqualTo(res.creationTime);
		if (res.lastModifiedTime.equals(res3.lastModifiedTime)) {
			LOGGER.warn("modification time is expected changed." + " before:" + res.lastModifiedTime + " after:"
					+ res3.lastModifiedTime);
			fail("");
		}
		assertThat(((List<String>) res3.attribute.get("labels")).get(0)).isEqualTo(lblText);

		LOGGER.info("----Update Test 1 is complete----");
	}

	@Test
	public void testUpdate2() {
		LOGGER.info("----Start Update Test 2----");

		ResourceDTO req = new ResourceDTO();
		ResourceDTO res = null;

		String lblText = "updateTest";

		String uri = "-/updateCnt2";
		List<String> lbl = new ArrayList<String>();
		lbl.add(lblText);
		req.attribute = new HashMap<String, Object>();
		req.attribute.put("labels", lbl);

		try {
			res = serviceLayerService.update(uri, req).getValue();
		} catch (Exception e) {
			res = null;
		}

		// Check result
		assertThat(res).isNull();

		LOGGER.info("----Update Test 2 is complete----");
	}

	@Test
	public void testDelete1() {
		LOGGER.info("----Start Delete Test 1----");

		ResourceDTO req = new ResourceDTO();

		String uri = "-";
		req.resourceName = "deleteCnt";
		req.resourceType = RT_container;

		try {
			serviceLayerService.create(uri, req).getValue();
		} catch (Exception e) {
			fail("");
		}

		uri = "-/deleteCnt";

		try {
			if (!serviceLayerService.delete(uri).getValue()) {
				fail("");
			}
		} catch (Exception e) {
			fail("");
		}

		LOGGER.info("----Delete Test 1 is complete----");
	}

	@Test
	public void testDelete2() {
		LOGGER.info("----Start Delete Test 2----");

		String uri = "-/deleteCnt2";

		try {
			if (serviceLayerService.delete(uri).getValue()) {
				fail("");
			}
		} catch (Exception e) {
			fail("");
		}

		LOGGER.info("----Delete Test 2 is complete----");
	}

	@Test
	public void testDiscovery1() {
		LOGGER.info("----Start Discovery Test 1----");

		ResourceDTO req1 = new ResourceDTO();

		String uri = "-";
		req1.resourceName = "disCnt1";
		req1.resourceType = RT_container;

		try {
			serviceLayerService.create(uri, req1).getValue();
		} catch (Exception e) {
			fail("");
		}

		ResourceDTO req2 = new ResourceDTO();

		uri = "-";
		req2.resourceName = "disCnt2";
		req2.resourceType = 3;

		try {
			serviceLayerService.create(uri, req2).getValue();
		} catch (Exception e) {
			fail("");
		}

		ResourceDTO req3 = new ResourceDTO();

		uri = "-/disCnt1";
		req3.resourceName = "disCnt3";
		req3.resourceType = RT_container;

		try {
			serviceLayerService.create(uri, req3).getValue();
		} catch (Exception e) {
			fail("");
		}

		List<String> uril = null;
		FilterCriteriaDTO fc = new FilterCriteriaDTO();
		fc.filterUsage = FilterUsage.DiscoveryCriteria;
		uri = "-/disCnt1";

		try {
			uril = serviceLayerService.discovery(uri, fc).getValue();
		} catch (Exception e) {
			fail("");
		}

		for (String u : uril) {
			System.out.println(u);
			String[] subelements = u.split("/", 2);
			if (subelements.length == 1) {
				continue;
			}
			switch (subelements[1]) {// "remove "cb/" part from "cb/disCnt1", for example.
			case "disCnt1":
			case "disCnt1/disCnt3":
				break;
			default:
				fail("");
			}
		}

		LOGGER.info("----Discovery Test 1 is complete----");
	}

	@Test
	public void testDiscovery2() {
		String uri;
		LOGGER.info("----Start Discovery Test 2----");

		List<String> uril = null;
		FilterCriteriaDTO fc = new FilterCriteriaDTO();
		fc.filterUsage = FilterUsage.DiscoveryCriteria;
		uri = "-/disCnt0";

		try {
			uril = serviceLayerService.discovery(uri, fc).getValue();
		} catch (Exception e) {
			fail("");
		}

		assertThat(uril).isNull();

		LOGGER.info("----Discovery Test 2 is complete----");
	}

	/**
	 * Class for receiving notification
	 */
	class MyListener implements NotificationListener {
		Set<RequestPrimitiveDTO> holder;

		public MyListener(Set<RequestPrimitiveDTO> holder) {
			this.holder = holder;
		}

		public void notified(RequestPrimitiveDTO request) {
			holder.add(request);
		}
	}

	@Test
	public void testNotify1() {
		LOGGER.info("----Start Notify Test 1----");

		String myaddress = getMyAddress();

		HashSet<RequestPrimitiveDTO> set = new HashSet<RequestPrimitiveDTO>();

		NotificationListener listener = new MyListener(set);
		ServiceRegistration<NotificationListener> reg = con.registerService(NotificationListener.class, listener, null);

		NotificationDTO notif = new NotificationDTO();
		notif.notificationEvent = new NotificationEventDTO();
		notif.notificationEvent.representation = "dummy:URI";

		Promise<Boolean> pb = serviceLayerService.notify(myaddress, notif);

		boolean ret = false;
		try {
			ret = pb.getValue();
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
			fail("");
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			fail("");
		}

		assertThat(ret).isTrue();
		assertThat(set).isNotEmpty();

		reg.unregister();
		LOGGER.info("---- Notify Test 1 is complete----");
	}

	@Test
	public void testNotify2() {
		LOGGER.info("----Start Notify Test 2----");
		HashSet<RequestPrimitiveDTO> set = new HashSet<RequestPrimitiveDTO>();

		NotificationListener listener = new MyListener(set);
		ServiceRegistration<NotificationListener> reg = con.registerService(NotificationListener.class, listener, null);

		NotificationDTO notif = new NotificationDTO();
		notif.notificationEvent = new NotificationEventDTO();
		notif.notificationEvent.representation = "dummy:URI";

		Promise<Boolean> pb = serviceLayerService.notify("/mn-cse-not-exist/CAE2", notif);// disconnected cse

		boolean ret = false;
		try {
			ret = pb.getValue();
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
			fail("");
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			fail("");
		}

		assertThat(ret).isFalse();
		assertThat(set).isEmpty();

		reg.unregister();
		LOGGER.info("---- Notify Test 2 is complete----");
	}

	@Test
	public void testNotify3byUpdate() {
		LOGGER.info("----Start Notify Test 3 by update----");
		HashSet<RequestPrimitiveDTO> set = new HashSet<RequestPrimitiveDTO>();

		NotificationListener listener = new MyListener(set);
		ServiceRegistration<NotificationListener> reg = con.registerService(NotificationListener.class, listener, null);

		ResourceDTO r = new ResourceDTO();
		ResourceDTO r2 = null;

		r.resourceName = "cont";
		r.resourceType = RT_container;

		try {
			r2 = serviceLayerService.create("-", r).getValue();
		} catch (Exception e) {
			fail("");
		}
		LOGGER.info("returned resource :" + r2);

		String uri = "-/cont";
		ResourceDTO sub = new ResourceDTO();
		sub.resourceName = "sub";
		sub.resourceType = RT_subscription;
		sub.attribute = new HashMap<String, Object>();
		sub.attribute.put("notificationURI", getMyAddress());

		try {
			r2 = serviceLayerService.create(uri, sub).getValue();
		} catch (Exception e) {
			fail("");
		}
		LOGGER.info("returned resource :" + r2);

		// update resource
		uri = "-/cont";
		r = new ResourceDTO();// create new object
		r.resourceName = "cont";
		r.resourceType = RT_container;

		String lblText = "updated";
		List<String> lbl = new ArrayList<String>();
		lbl.add(lblText);
		r.attribute = new HashMap<String, Object>();
		r.attribute.put("labels", lbl);

		try {
			r2 = serviceLayerService.update(uri, r).getValue();
		} catch (Exception e) {
			fail("");
		}
		LOGGER.info("returned resource :" + r2);

		assertThat(set).hasSize(1);

		reg.unregister();
		LOGGER.info("---- Notify Test 3 is complete----");
	}

	String myaddress = null;

	private String getMyAddress() {
		if (myaddress != null)
			return myaddress;
		try {
			ResourceDTO res = null;

			res = serviceLayerService.retrieve("-").getValue();

			String cseid = (String) res.attribute.get("CSE-ID");

			ResourceDTO r = new ResourceDTO();

			String uri = "-";
			r.resourceName = "dummyae";
			r.resourceType = RT_AE;
			r.attribute = new HashMap<String, Object>();
			r.attribute.put("App-ID", "TESTAPP");
			r.attribute.put("requestReachability", true);

			res = serviceLayerService.create(uri, r).getValue();

			LOGGER.warn("res:" + res);
			String aeid = (String) res.attribute.get("AE-ID");
			myaddress = "/" + cseid + "/" + aeid;
			return myaddress;
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return null;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}

	}

	private void cleanup() {
		LOGGER.info("---- Clean Up -----");
		List<String> uril = null;
		FilterCriteriaDTO fc = new FilterCriteriaDTO();
		fc.filterUsage = FilterUsage.DiscoveryCriteria;

		try {
			uril = serviceLayerService.discovery("-", fc).getValue();

			if (uril == null) {
				LOGGER.warn("returned uril is null.");
				return;// TODO:
			}
			for (String url : uril) {
				if (url.contains("/")) {// skip CSEBase
					LOGGER.warn("removing " + url);
					serviceLayerService.delete(url).getValue();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.info("---- Clean Up done. -----");

	}
}
