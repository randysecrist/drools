package com.sfn.drools;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.jbpm.task.Content;
import org.jbpm.task.Task;
import org.jbpm.task.User;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

public class RiakEntityManager implements EntityManager {
	private static final Logger logger = Logger.getLogger(RiakEntityManager.class);
	
    public EntityTransaction getTransaction() { logger.info("called"); return new RiakEntityTransaction(); }
    public boolean isOpen() { logger.info("called"); return true; }
    public void close() { logger.info("called"); }
    public Object getDelegate() { logger.info("called"); return new Object(); }
    public void joinTransaction() { logger.info("called"); }

    public Query createNativeQuery(String x) { logger.info("called"); return new RiakQuery(); }
    public Query createNativeQuery(String x, String y) { logger.info("called"); return new RiakQuery(); }
    public Query createNativeQuery(String x, Class klass) { logger.info("called"); return new RiakQuery(); }

    public Query createNamedQuery(String x) {
    	logger.info("called - " + x);
    	return new RiakQuery();
    }
    public Query createQuery(String x) { logger.info("called"); return new RiakQuery(); }

    public boolean contains(Object o) { logger.info("called"); return false; }
    public void clear() { logger.info("called"); }
    public void refresh(Object o) { logger.info("called"); }
    public void lock(Object o, LockModeType type) { logger.info("called"); }

    public FlushModeType getFlushMode() { logger.info("called"); return null; }
    public void setFlushMode(FlushModeType type) { logger.info("called"); }
    public void flush() { logger.info("called"); }

    public <T> T getReference(Class<T> klass, Object o) { logger.info("called"); return null; }
    public <T> T find(Class<T> klass) { return this.find(klass, null); }
    public <T> T find(Class<T> klass, Object o) {
    	if (klass != null && o != null)
    		logger.info("called - " + klass.getName() + "::" + o.toString());
    	else if (klass != null)
    		logger.info("called - " + klass.getName());
    	return null;
    }
    public <T> T merge(T t) { logger.info("called"); return null; }

    public void remove(Object o) { logger.info("called"); }
    public void persist(Object o) {
    	//XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
    	/*
    	XStream xstream = new XStream(new JsonHierarchicalStreamDriver() {
    	    public HierarchicalStreamWriter createWriter(Writer writer) {
    	        return new JsonWriter(writer, JsonWriter.DROP_ROOT_MODE);
    	    }
    	});
    	*/
    	XStream xstream = new XStream(new JettisonMappedXmlDriver());
    	
    	xstream.setMode(XStream.NO_REFERENCES);
    	// Application Specific Things
    	//xstream.alias("user", User.class);
    	//xstream.alias("task", Task.class);
    	//xstream.alias("content", Content.class);

    	// Deserialization
    	String json = xstream.toXML(o);
    	if (o instanceof User || o instanceof Content) {
    		xstream.fromXML(json);
    	}
    	else if (o instanceof Task) {
    		logger.error("Task JSON Deserialization not working!");
    	}
    	logger.info("called - " + o.getClass().getCanonicalName() + "\n" + json);
    }
}
