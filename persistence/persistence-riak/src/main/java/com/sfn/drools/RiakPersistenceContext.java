package com.sfn.drools;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.log4j.Logger;
import org.drools.persistence.PersistenceContext;
import org.drools.persistence.info.SessionInfo;
import org.drools.persistence.info.WorkItemInfo;

public class RiakPersistenceContext implements PersistenceContext {
	public static final Logger logger = Logger.getLogger(RiakPersistenceContext.class);
	
	private SecureRandom prng = null;
	
	public RiakPersistenceContext() {
		try { prng = SecureRandom.getInstance("SHA1PRNG"); }
		catch (NoSuchAlgorithmException nsa) { throw new RuntimeException(nsa); }
	}
	
	public void remove(WorkItemInfo arg0) { logger.info("called"); }
	public void persist(WorkItemInfo arg0) { logger.info("called"); }
	public void persist(SessionInfo session_info) {
		int session_id = prng.nextInt();
		logger.info("Saving Session Info using id: " + session_id);
		session_info.setId(session_id);
	}
	public WorkItemInfo merge(WorkItemInfo arg0) { logger.info("called"); return null; }
	public void joinTransaction() { logger.info("called"); }
	public boolean isOpen() { logger.info("called"); return true; }
	public WorkItemInfo findWorkItemInfo(Long work_item_id) { logger.info("called"); return null; }
	public SessionInfo findSessionInfo(Integer session_id) {
		logger.info("called");
		SessionInfo sii = new SessionInfo();
		sii.setId(session_id);
		return sii;
	}
	public void close() { logger.info("called"); }

}
