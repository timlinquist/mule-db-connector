/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.db.lifecycle;

import static java.lang.Thread.getAllStackTraces;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.core.IsNot.not;

import org.mule.extension.db.internal.lifecycle.OracleArtifactLifecycleListener;
import org.mule.sdk.api.artifact.lifecycle.ArtifactLifecycleListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class OracleArtifactLifecycleListenerTestCase extends AbstractArtifactLifecycleListenerTestCase {

  public static final String DRIVER_PACKAGE = "oracle.jdbc";
  public static final String DRIVER_TIMER_THREAD = "oracle.jdbc.diagnostics.Diagnostic.CLOCK";

  public OracleArtifactLifecycleListenerTestCase(String groupId, String artifactId, String version) {
    super(groupId, artifactId, version);
  }

  @Parameterized.Parameters
  public static Collection<Object[]> parameters() {
    return Arrays.asList(new Object[][] {
        {"com.oracle.database.jdbc", "ojdbc8", "23.2.0.0"}});
  }

  @Override
  Class<? extends ArtifactLifecycleListener> getArtifactLifecycleListenerClass() {
    return OracleArtifactLifecycleListener.class;
  }

  @Override
  String getPackagePrefix() {
    return DRIVER_PACKAGE;
  }

  @Override
  public String getDriverThreadName() {
    return DRIVER_TIMER_THREAD;
  }

  @Override
  protected Class getLeakTriggererClass() {
    return OracleLeakTriggerer.class;
  }

  private List<Thread> previousThreads = new ArrayList<>();

  @Before
  public void getPreviousThreads() throws Exception {
    previousThreads = getAllStackTraces().keySet().stream()
        .filter(thread -> thread.getName().startsWith(getDriverThreadName())).collect(Collectors.toList());
  }

  protected Matcher<Iterable<? super Thread>> hasDriverThreadMatcher(ClassLoader target, boolean negateMatcher) {
    Matcher<Iterable<? super Thread>> matcher =
        hasItem(
                allOf(
                      hasProperty("name", is(getDriverThreadName())),
                      not(isIn(previousThreads))));
    return negateMatcher ? not(matcher) : matcher;
  }

  @After
  public void checkPreviousThreads() throws Exception {
    assertThat(getAllStackTraces().keySet().stream()
        .filter(thread -> thread.getName().startsWith(getDriverThreadName())).collect(Collectors.toList()),
               containsInAnyOrder(previousThreads.toArray()));
  }
}
