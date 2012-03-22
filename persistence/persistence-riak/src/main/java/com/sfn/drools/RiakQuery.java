package com.sfn.drools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.FlushModeType;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.apache.log4j.Logger;

public class RiakQuery implements Query {
	private static final Logger logger = Logger.getLogger(RiakQuery.class);
	
    public Query setFlushMode(FlushModeType type) { logger.info("called"); return this; }
    public int executeUpdate() { logger.info("called"); return 0; }
    public List getResultList() { logger.info("called"); return new ArrayList(); }
    public Object getSingleResult() { logger.info("called"); return null; }
    public Query setFirstResult(int i) { logger.info("called"); return null; }
    public Query setHint(String i, Object o) { logger.info("called"); return null; }
    public Query setMaxResults(int i) { logger.info("called"); return null; }
    public Query setParameter(String s, Object o) {
    	logger.info("called - " + s + "::" + o.toString());
    	return null;
    }
    public Query setParameter(int i, Object v) { logger.info("called"); return null; }
    public Query setParameter(String s, Date d, TemporalType t) { logger.info("called"); return null; }
    public Query setParameter(String s, Calendar c, TemporalType t) { logger.info("called"); return null; }
    public Query setParameter(int i, Date d, TemporalType t) { logger.info("called"); return null; }
    public Query setParameter(int i, Calendar c, TemporalType t) { logger.info("called"); return null; }
}
