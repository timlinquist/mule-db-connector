/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.db.lifecycle;

import static java.lang.Thread.getAllStackTraces;
import static org.awaitility.Awaitility.await;
import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;

import org.slf4j.Logger;

public class MySqlLeakTriggerer implements Runnable {

  private static final Logger LOGGER = getLogger(MySqlLeakTriggerer.class);

  @Override
  public void run() {
    // To avoid race conditions, I wait for the driver to be available.
    await().until(() -> Collections.list(DriverManager.getDrivers()).stream()
        .anyMatch(driver -> driver.getClass().getName().contains("mysql")));
    try (Connection con = DriverManager.getConnection("jdbc:mysql://hostname:3306/dummy?user=dummy&password=dummy")) {
    } catch (SQLException e) {
      LOGGER.error("Connection could not be established: {}", e.getMessage(), e);
      await().until(() -> getAllStackTraces().keySet().stream()
              .filter(thread -> thread.getName().startsWith("mysql-cj-abandoned-connection-cleanup"))
              .anyMatch(thread ->
                      thread.getContextClassLoader() == Thread.currentThread().getContextClassLoader().getParent()
                      || thread.getContextClassLoader() == Thread.currentThread().getContextClassLoader()
              )
      );
    }
  }
}
