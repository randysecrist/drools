package org.ihc.hwcir.drools;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;

public class PersistenceAppToo {

    public static void main(String[] args) {
        new PersistenceAppToo();
    }

    private PersistenceAppToo() {

        try {
            final Hashtable<String, String> jndiProperties = new Hashtable<String, String>();
            jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
            final Context context = new InitialContext(jndiProperties);

            final String appName = "persistence-ear";

            final String moduleName = "persistence-ejb-0.1";

            final String distinctName = "";

            final String beanName = DroolsPersistenceImpl.class.getSimpleName();

            final String viewClassName = DroolsPersistenceRemote.class.getName();

            String result = "ejb:" + appName + "/" + moduleName + "/" + distinctName + "/" + beanName + "!" + viewClassName;

            DroolsPersistenceRemote remote = (DroolsPersistenceRemote) context.lookup(result);

            remote.loadSession(98, null);

            int processState = remote.findProcessState(57L);

            if (processState == 1) {
                System.out.println("Process is currently waiting for some interaction...");
            } else {
                System.out.println("Process is completed...");
            }

            context.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}