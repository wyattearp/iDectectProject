package edu.uc.cs.distsys.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import edu.uc.cs.distsys.Logger;
import edu.uc.cs.distsys.Node;

public class PropertiesManager {
	private int id;
	private Properties properties;
	private File file;
	private Logger logger;

	public PropertiesManager(int id, Logger logger) {
		this.id = id;
		this.properties = new Properties();
		this.logger = logger;
		this.file = new File(this.id + ".properties");
	}

	public void load() {
		try {
			// TODO: decide if we want to use a local file or a class resource
			// in the jar file
			if (this.file.exists()) {
				FileInputStream input = new FileInputStream(this.file);
				this.properties.load(input);
				input.close();
				display();
			} else {
				logger.error("Unable to load properties file: "
						+ this.file.getAbsoluteFile());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void save() {
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(this.file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			this.properties.store(output, "Properties used by iDetect Node " + this.id);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void display() {
		String line = "Using existing properties from " + this.file.getAbsoluteFile() + ":";
		for (String key : this.properties.stringPropertyNames()) {
			String value = this.properties.getProperty(key);
			line += "\n" + key + " => " + value;
		}
		logger.log(line);
	}

	public void setProperties(Node node) {
		this.properties.setProperty("NodeID", Integer.toString(node.getId()));
		this.properties.setProperty("GroupID", Integer.toString(node.getGroupId()));
	}
	
	public Properties getProperties() {
		return properties;
	}
}
