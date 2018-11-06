import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.DseSession;

public class TestClusterObjectDiag {
    // Constructor
    public TestClusterObjectDiag() {
    }

    static TestClusterObjectDiag testclusterdiag = new TestClusterObjectDiag();
    static ClusterObjectDiag clusterObjectDiag = new ClusterObjectDiag();

    // main class just for kicking things off
    public static void main(String[] args) throws Exception {
        testclusterdiag.runCheck();
    }

    private void runCheck() {
        DseCluster cluster = null;

        try {
            cluster = DseCluster.builder()
                    .addContactPoint("10.200.178.52")
                    .build();
            DseSession session = cluster.connect();

            // Now pass the cluster oject for diag checks
            clusterObjectDiag.runCheck(cluster);

        } finally {
            if (cluster != null) cluster.close();
        }
    }
}
