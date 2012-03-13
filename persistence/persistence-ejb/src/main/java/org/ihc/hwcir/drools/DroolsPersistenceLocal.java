package org.ihc.hwcir.drools;

import java.util.List;

import javax.ejb.Local;

@Local
public interface DroolsPersistenceLocal {

    int fireAllRules(List<Object> facts);

    int startProcess(String processId);

    void loadSession(int sessionId, String[] sessionStuff);

    int initiateSession(String[] sessionStuff);

    int findProcessState(long processId);

    long destroyProcess(long processId);
}