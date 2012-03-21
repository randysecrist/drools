package com.sfn.drools;

import javax.persistence.EntityTransaction;

import org.apache.log4j.Logger;

public class RiakEntityTransaction implements EntityTransaction {
	private static final Logger logger = Logger.getLogger(RiakEntityTransaction.class);
	
	public void begin() { logger.info("called"); }
	public void commit() { logger.info("called"); }
	public boolean getRollbackOnly() { logger.info("called"); return false; }
	public boolean isActive() { logger.info("called"); return false; }
	public void rollback() { logger.info("called"); }
	public void setRollbackOnly() { logger.info("called"); }
}
