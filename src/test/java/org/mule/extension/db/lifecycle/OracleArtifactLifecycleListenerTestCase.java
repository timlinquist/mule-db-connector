/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.db.lifecycle;

import org.mule.extension.db.internal.lifecycle.DerbyArtifactLifecycleListener;
import org.mule.extension.db.internal.lifecycle.OracleArtifactLifecycleListener;
import org.mule.sdk.api.artifact.lifecycle.ArtifactLifecycleListener;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class OracleArtifactLifecycleListenerTestCase extends AbstractArtifactLifecycleListenerTestCase {

  public OracleArtifactLifecycleListenerTestCase(String groupId, String artifactId, String version) {
    super(groupId, artifactId, version);
  }

  @Parameterized.Parameters
  public static Collection<Object[]> parameters() {
    return Arrays.asList(new Object[][] {{"com.oracle.database.jdbc", "ojdbc8", "23.2.0.0"}});
  }

  @Override
  ArtifactLifecycleListener getArtifactLifecycleListener() {
    return new OracleArtifactLifecycleListener();
  }

  @Override
  void generateTargetLeak(ClassLoader classLoader) {

  }

  @Override
  String getPackagePrefix() {
    return "com.oracle";
  }

  @Override
  Boolean enableLibraryReleaseChecking() {
    return true;
  }

  @Override
  Boolean enableThreadsReleaseChecking() {
    return true;
  }

  @Override
  void assertThreadsAreNotDisposed() {
    return;
  }

  @Override
  void assertThreadsAreDisposed() {
    return;
  }
}
