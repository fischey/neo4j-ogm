/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.testutil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.config.ClasspathConfigurationSource;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.driver.DriverManager;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author vince
 */
public class MultiDriverTestClass {

	private static final Logger logger = LoggerFactory.getLogger(MultiDriverTestClass.class);
	private static TestServer testServer;
	private static GraphDatabaseService impermanentDb;
	private static File graphStore;
	protected static Configuration.Builder baseConfiguration;

	@BeforeClass
	public static synchronized void setupMultiDriverTestEnvironment() {

		baseConfiguration = new Configuration.Builder(new ClasspathConfigurationSource(configFileName()));

		if (baseConfiguration.build().getDriverClassName().equals(HttpDriver.class.getCanonicalName())) {
			testServer = new TestServer.Builder()
					.enableAuthentication(true)
					.enableBolt(false)
					.transactionTimeoutSeconds(30)
					.build();
			baseConfiguration.uri(testServer.getUri()).credentials(testServer.getUsername(), testServer.getPassword());
		} else if (baseConfiguration.build().getDriverClassName().equals(BoltDriver.class.getCanonicalName())) {
			testServer = new TestServer.Builder()
					.enableBolt(true)
					.transactionTimeoutSeconds(30)
					.build();
			baseConfiguration.uri(testServer.getUri()).credentials(testServer.getUsername(), testServer.getPassword());
		} else {
			graphStore = createTemporaryGraphStore();
			impermanentDb = new TestGraphDatabaseFactory().newImpermanentDatabase(graphStore);
			logger.info("Creating new impermanent database {}", impermanentDb);
			baseConfiguration .uri(graphStore.toURI().toString()).build();
		}
	}


	@AfterClass
	public static synchronized void tearDownMultiDriverTestEnvironment() {
		close();
	}

	private static void close() {

		if (testServer != null) {
			if (testServer.isRunning(1000)) {
				testServer.shutdown();
			}
			testServer = null;
		}
		if (impermanentDb != null) {
			if (impermanentDb.isAvailable(1000)) {
				impermanentDb.shutdown();
			}
			impermanentDb = null;
			graphStore = null;
			DriverManager.deregister(DriverManager.getDriver());
		}
	}

	@After
	public void tearDown() {
		if (impermanentDb != null) {
			DriverManager.deregister(DriverManager.getDriver());
		}
	}


	public static synchronized GraphDatabaseService getGraphDatabaseService() {
		if (testServer != null) {
			return testServer.getGraphDatabaseService();
		}
		return impermanentDb;
	}

	private static String configFileName() {
		String configFileName = System.getenv("ogm.properties");

		if (configFileName == null) {
			configFileName = System.getProperty("ogm.properties");
			if (configFileName == null) {
				configFileName = "ogm.properties";
			}
		}
		return configFileName;
	}

	public static File createTemporaryGraphStore() {
		try {
			Path path = Files.createTempDirectory("graph.db");
			File f = path.toFile();
			f.deleteOnExit();
			return f;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
