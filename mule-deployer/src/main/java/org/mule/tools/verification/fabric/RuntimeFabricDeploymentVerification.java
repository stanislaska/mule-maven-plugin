/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.verification.fabric;

import org.apache.commons.lang3.StringUtils;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.client.fabric.RuntimeFabricClient;
import org.mule.tools.client.fabric.model.DeploymentDetailedResponse;
import org.mule.tools.client.fabric.model.DeploymentGenericResponse;
import org.mule.tools.client.fabric.model.Deployments;
import org.mule.tools.model.Deployment;
import org.mule.tools.verification.DefaultDeploymentVerification;
import org.mule.tools.verification.DeploymentVerification;
import org.mule.tools.verification.DeploymentVerificationStrategy;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class RuntimeFabricDeploymentVerification implements DeploymentVerification {

  private final RuntimeFabricClient client;

  public static final String STARTED_STATUS = "STARTED";

  public RuntimeFabricDeploymentVerification(RuntimeFabricClient client) {
    this.client = client;
  }

  @Override
  public void assertDeployment(Deployment deployment) throws DeploymentException {
    DefaultDeploymentVerification verification =
        new DefaultDeploymentVerification(new RuntimeFabricDeploymentVerificationStrategy(deployment));
    verification.assertDeployment(deployment);
  }

  private class RuntimeFabricDeploymentVerificationStrategy implements DeploymentVerificationStrategy {

    private final Deployment deployment;
    private Deployments deployments;
    private String deploymentId;

    private RuntimeFabricDeploymentVerificationStrategy(Deployment deployment) {
      this.deployment = deployment;
      deployments = client.getDeployments();
    }

    @Override
    public Boolean isDeployed(Deployment deployment) {
      String deploymentId = getDeploymentId(deployment);
      if (deploymentId == null) {
        return false;
      }
      DeploymentDetailedResponse response = client.getDeployment(deploymentId);
      return response != null && StringUtils.equals(response.status, STARTED_STATUS);
    }

    private String getDeploymentId(Deployment deployment) {
      if (deploymentId == null) {
        deployments = client.getDeployments();
        for (DeploymentGenericResponse dep : deployments) {
          if (StringUtils.equals(dep.name, deployment.getApplicationName())) {
            deploymentId = dep.id;
            return deploymentId;
          }
        }
      }
      return deploymentId;
    }

    @Override
    public void onTimeout(Deployment deployment) {
      client.deleteDeployment(getDeploymentId(deployment));
    }

    @Override
    public Boolean run() {
      return !isDeployed(deployment);
    }

    @Override
    public String getRetryExhaustedMessage() {
      return "Runtime Fabric deployment has timeouted";
    }
  }
}