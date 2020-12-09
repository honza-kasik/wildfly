package org.jboss.as.test.manualmode.server.nongraceful;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.test.manualmode.server.nongraceful.deploymenta.TestApplicationA;
import org.jboss.as.test.manualmode.server.nongraceful.deploymentb.TestApplicationB;
import org.jboss.as.test.shared.TimeoutUtil;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class NongracefulStartTestCase {
    private static final String CONTAINER = "non-graceful-server";
    private static final String DEPLOYMENTA = "deploymenta";
    private static final String DEPLOYMENTB = "deploymentb";
    private static final Logger logger = Logger.getLogger(NongracefulStartTestCase.class);

    @ArquillianResource
    private static ContainerController containerController;

    @ArquillianResource
    Deployer deployer;

    @Deployment(name = DEPLOYMENTA, managed = false, testable = false)
    @TargetsContainer(CONTAINER)
    public static Archive<?> getDeploymentA() {
        return buildBaseArchive(DEPLOYMENTA)
                .addPackage(TestApplicationA.class.getPackage());
    }

    @Deployment(name = DEPLOYMENTB, managed = false, testable = false)
    @TargetsContainer(CONTAINER)
    public static Archive<?> getDeploymentB() {
        return buildBaseArchive(DEPLOYMENTB)
                .addPackage(TestApplicationB.class.getPackage());
    }

    private static WebArchive buildBaseArchive(String name) {
        return ShrinkWrap
                .create(WebArchive.class, name + ".war")
                .add(EmptyAsset.INSTANCE, "WEB-INF/beans.xml")
                .addClass(TimeoutUtil.class);
    }

    @Test
    public void testNonGracefulDeployment() {
        try {
            containerController.start(CONTAINER);

            deployer.deploy(DEPLOYMENTA);
            deployer.deploy(DEPLOYMENTB);

            containerController.stop(CONTAINER);
            containerController.start(CONTAINER);
        } finally {
            executeCleanup(() -> deployer.undeploy(DEPLOYMENTA));
            executeCleanup(() -> deployer.undeploy(DEPLOYMENTB));
            executeCleanup(() -> containerController.stop(CONTAINER));
        }
    }

    private void executeCleanup(Runnable func) {
        try {
            func.run();
        } catch (Exception e) {
            logger.trace("Exception during container cleanup and shutdown", e);
        }
    }
}