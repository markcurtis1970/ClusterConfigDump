import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.HostDistance;
import com.google.common.base.Strings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * This class enables you to pass in a cluster object
 * and get some configuration details out
 */
public class ClusterObjectDiag {

    // Only use local hosts
    private static final HostDistance distance = HostDistance.LOCAL;
    // ?
    private static final Set<Class> primitiveClasses = new HashSet<Class>() {{
        add(Long.class);
        add(String.class);
        add(Integer.class);
        add(Boolean.class);
        add(Double.class);
        add(Float.class);
    }};
    // Excluded methods we dont want to query
    private static final Set<String> excludedFunctions = new HashSet<String>() {{
        add("getDeclaringClass");
        add("getClass");

    }};

    public ClusterObjectDiag() {

    }

    /**
     * Call this method with your cluster object to
     * run the check
     *
     * @param cluster
     */
    public void runCheck(Cluster cluster) {
        System.out.println("\n=== Cluster config for " + cluster.getMetadata().getClusterName() + " ===\n");
        dumpClass(cluster.getConfiguration(), 1);
    }


    /**
     * Dump the cluster object and all its values to stdout
     *
     * @param obj
     * @param level
     */
    private void dumpClass(Object obj, int level) {
        String ident = Strings.repeat(" ", level);

        for (Method method : obj.getClass().getMethods()) {
            String name = method.getName();
            if ((name.startsWith("get") || name.startsWith("is")) && !excludedFunctions.contains(name)) {
                try {
                    Object result = null;
                    if (method.getParameterCount() == 0) {
                        result = method.invoke(obj);
                    } else {
                        Class[] params = method.getParameterTypes();
                        if (params.length == 1 && params[0].equals(distance.getDeclaringClass())) {
                            for (HostDistance distance : HostDistance.values()) {
                                result = method.invoke(obj, distance);
                                System.out.println(ident + name + "(" + distance.name() + ")" + "=" + result);
                            }
                            continue;
                        } else {
                            System.out.println(name + ": method with " + params.length + " arguments");
                        }
                    }
                    if (result == null) {
                        System.out.println(ident + name + "=" + result);
                        continue;
                    }
                    Class resultClass = result.getClass();
                    if (primitiveClasses.contains(resultClass)) {
                        System.out.println(ident + name + "=" + result);
                    } else if (result instanceof Enum) {
                        System.out.println(ident + name + "=" + ((Enum) result).name());
                    } else {
                        System.out.println(ident + name + ": " + resultClass.getName());
                        dumpClass(result, level + 2);
                    }
                } catch (IllegalAccessException e) {
                    System.out.println("Could not determine method: " + method.getName());
                } catch (InvocationTargetException e) {
                    System.out.println("Could not determine method: " + method.getName());
                }
            }
        }
    }

}
