/*
 * Copyright (C) 2015 Laurent CLOUET
 * Author Laurent CLOUET <laurent.clouet@nopnop.net>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.sheepit.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.sheepit.client.Configuration.ComputeType;
import com.sheepit.client.hardware.gpu.GPU;
import com.sheepit.client.hardware.gpu.GPUDevice;
import lombok.Setter;

public class SettingsLoader {
	private String path;
	
	private String login;
	
	@Setter private String password;
	
	private String proxy;
	private String hostname;
	private String computeMethod;
	private String gpu;
	private String renderbucketSize;
	private String cores;
	private String ram;
	private String renderTime;
	private String cacheDir;
	private String autoSignIn;
	private String useSysTray;
	private String ui;
	private String theme;
	private int priority;
	
	public SettingsLoader(String path_) {
		if (path_ == null) {
			path = getDefaultFilePath();
		}
		else {
			path = path_;
		}
	}
	
	public SettingsLoader(String path_, String login_, String password_, String proxy_, String hostname_, ComputeType computeMethod_, GPUDevice gpu_,
		int renderbucketSize_, int cores_, long maxRam_, int maxRenderTime_, String cacheDir_, boolean autoSignIn_, boolean useSysTray_, String ui_,
		String theme_, int priority_) {
		if (path_ == null) {
			path = getDefaultFilePath();
		}
		else {
			path = path_;
		}
		login = login_;
		password = password_;
		proxy = proxy_;
		hostname = hostname_;
		cacheDir = cacheDir_;
		autoSignIn = String.valueOf(autoSignIn_);
		useSysTray = String.valueOf(useSysTray_);
		ui = ui_;
		priority = priority_;
		theme = theme_;
		
		if (cores_ > 0) {
			cores = String.valueOf(cores_);
		}
		if (maxRam_ > 0) {
			ram = String.valueOf(maxRam_) + "k";
		}
		if (maxRenderTime_ > 0) {
			renderTime = String.valueOf(maxRenderTime_);
		}
		if (computeMethod_ != null) {
			try {
				computeMethod = computeMethod_.name();
			}
			catch (IllegalArgumentException e) {
			}
		}
		
		if (gpu_ != null) {
			gpu = gpu_.getId();
		}
		
		if (renderbucketSize_ >= GPU.MIN_RENDERBUCKET_SIZE) {
			renderbucketSize = String.valueOf(renderbucketSize_);
		}
	}
	
	public static String getDefaultFilePath() {
		return System.getProperty("user.home") + File.separator + ".sheepit.conf";
	}
	
	public String getFilePath() {
		return path;
	}
	
	public void saveFile() {
		Properties prop = new Properties();
		OutputStream output = null;
		try {
			output = new FileOutputStream(path);
			prop.setProperty("priority", new Integer(priority).toString());
			
			if (cacheDir != null) {
				prop.setProperty("cache-dir", cacheDir);
			}
			
			if (computeMethod != null) {
				prop.setProperty("compute-method", computeMethod);
			}
			
			if (gpu != null) {
				prop.setProperty("compute-gpu", gpu);
			}
			
			if (renderbucketSize != null) {
				prop.setProperty("renderbucket-size", renderbucketSize);
			}
			
			if (cores != null) {
				prop.setProperty("cores", cores);
			}
			
			if (ram != null) {
				prop.setProperty("ram", ram);
			}
			
			if (renderTime != null) {
				prop.setProperty("rendertime", renderTime);
			}
			
			if (login != null) {
				prop.setProperty("login", login);
			}
			
			if (password != null) {
				prop.setProperty("password", password);
			}
			
			if (proxy != null) {
				prop.setProperty("proxy", proxy);
			}
			
			if (hostname != null) {
				prop.setProperty("hostname", hostname);
			}
			
			if (autoSignIn != null) {
				prop.setProperty("auto-signin", autoSignIn);
			}
			
			if (useSysTray != null) {
				prop.setProperty("use-systray", useSysTray);
			}
			
			if (ui != null) {
				prop.setProperty("ui", ui);
			}
			
			if (theme != null) {
				prop.setProperty("theme", theme);
			}
			
			prop.store(output, null);
		}
		catch (IOException io) {
			io.printStackTrace();
		}
		finally {
			if (output != null) {
				try {
					output.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		// Set Owner read/write
		Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
		perms.add(PosixFilePermission.OWNER_READ);
		perms.add(PosixFilePermission.OWNER_WRITE);
		
		try {
			Files.setPosixFilePermissions(Paths.get(path), perms);
		}
		catch (UnsupportedOperationException e) {
			// most likely because it's MS Windows
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void loadFile() {
		this.login = null;
		this.password = null;
		this.proxy = null;
		this.hostname = null;
		this.computeMethod = null;
		this.gpu = null;
		this.renderbucketSize = null;
		this.cacheDir = null;
		this.autoSignIn = null;
		this.useSysTray = null;
		this.ui = null;
		this.priority = 19; // must be the same default as Configuration
		this.ram = null;
		this.renderTime = null;
		this.theme = null;
		
		if (new File(path).exists() == false) {
			return;
		}
		
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(path);
			prop.load(input);
			
			if (prop.containsKey("cache-dir")) {
				this.cacheDir = prop.getProperty("cache-dir");
			}
			
			if (prop.containsKey("compute-method")) {
				this.computeMethod = prop.getProperty("compute-method");
			}
			
			if (prop.containsKey("compute-gpu")) {
				this.gpu = prop.getProperty("compute-gpu");
			}
			
			if (prop.containsKey("renderbucket-size")) {
				this.renderbucketSize = prop.getProperty("renderbucket-size");
			}
			
			if (prop.containsKey("cpu-cores")) { // backward compatibility
				this.cores = prop.getProperty("cpu-cores");
			}
			
			if (prop.containsKey("cores")) {
				this.cores = prop.getProperty("cores");
			}
			
			if (prop.containsKey("ram")) {
				this.ram = prop.getProperty("ram");
			}
			
			if (prop.containsKey("rendertime")) {
				this.renderTime = prop.getProperty("rendertime");
			}
			
			if (prop.containsKey("login")) {
				this.login = prop.getProperty("login");
			}
			
			if (prop.containsKey("password")) {
				this.password = prop.getProperty("password");
			}
			
			if (prop.containsKey("proxy")) {
				this.proxy = prop.getProperty("proxy");
			}
			
			if (prop.containsKey("hostname")) {
				this.hostname = prop.getProperty("hostname");
			}
			
			if (prop.containsKey("auto-signin")) {
				this.autoSignIn = prop.getProperty("auto-signin");
			}
			
			if (prop.containsKey("use-systray")) {
				this.useSysTray = prop.getProperty("use-systray");
			}
			
			if (prop.containsKey("ui")) {
				this.ui = prop.getProperty("ui");
			}
			
			if (prop.containsKey("theme")) {
				this.theme = prop.getProperty("theme");
			}
			
			if (prop.containsKey("priority")) {
				this.priority = Integer.parseInt(prop.getProperty("priority"));
			}
		}
		catch (IOException io) {
			io.printStackTrace();
		}
		finally {
			if (input != null) {
				try {
					input.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Merge the Settings file with the Configuration.
	 * The Configuration will have high priority.
	 */
	public void merge(Configuration config) {
		if (config == null) {
			System.out.println("SettingsLoader::merge config is null");
		}
		
		loadFile();
		
		if (config.getLogin().isEmpty() && login != null) {
			config.setLogin(login);
		}
		if (config.getPassword().isEmpty() && password != null) {
			config.setPassword(password);
		}
		
		if ((config.getProxy() == null || config.getProxy().isEmpty()) && proxy != null) {
			config.setProxy(proxy);
		}
		
		if ((config.getHostname() == null || config.getHostname().isEmpty() || config.getHostname().equals(config.getDefaultHostname())) && hostname != null) {
			config.setHostname(hostname);
		}
		
		if (config.getPriority() == 19) { // 19 is default value
			config.setUsePriority(priority);
		}
		try {
			if (config.getComputeMethod() == null && computeMethod == null) {
				config.setComputeMethod(ComputeType.CPU);
			}
			else if ((config.getComputeMethod() == null && computeMethod != null) || (computeMethod != null && config.getComputeMethod() != ComputeType
				.valueOf(computeMethod))) {
				if (config.getComputeMethod() == null) {
					config.setComputeMethod(ComputeType.valueOf(computeMethod));
				}
				
			}
		}
		catch (IllegalArgumentException e) {
			System.err.println("SettingsLoader::merge failed to handle compute method (raw value: '" + computeMethod + "')");
			computeMethod = null;
		}
		if (config.getGPUDevice() == null && gpu != null) {
			GPUDevice device = GPU.getGPUDevice(gpu);
			if (device != null) {
				config.setGPUDevice(device);
				
				// If the user has indicated a render bucket size at least 32x32 px, overwrite the config file value
				if (config.getRenderbucketSize() >= GPU.MIN_RENDERBUCKET_SIZE) {
					config.getGPUDevice().setRenderbucketSize(config.getRenderbucketSize());    // Update size
				}
				else {
					// If the configuration file does have any value
					if (renderbucketSize != null) {
						config.getGPUDevice().setRenderbucketSize(Integer.valueOf(renderbucketSize));
					}
					else {
						// Don't do anything here as the GPU get's a default value when it's initialised
						// The configuration will take the default GPU value
					}
				}
				
				// And now update the client configuration with the new value
				config.setRenderbucketSize(config.getGPUDevice().getRenderbucketSize());
			}
		}
		else if (config.getGPUDevice() != null) {
			// The order of conditions is important to ensure the priority or app arguments, then the config file and finally the recommended size (if none
			// specified or already in config file).
			if (config.getRenderbucketSize() >= GPU.MIN_RENDERBUCKET_SIZE) {
				config.getGPUDevice().setRenderbucketSize(config.getRenderbucketSize());
			}
			else if (renderbucketSize != null) {
				config.getGPUDevice().setRenderbucketSize(Integer.parseInt(renderbucketSize));
			}
			else {
				config.getGPUDevice().setRenderbucketSize(config.getGPUDevice().getRecommendedBucketSize());
			}
		}
		
		if (config.getNbCores() == -1 && cores != null) {
			config.setNbCores(Integer.valueOf(cores));
		}
		
		if (config.getMaxMemory() == -1 && ram != null) {
			config.setMaxMemory(Utils.parseNumber(ram) / 1000); // internal ram value is in kB
		}
		
		if (config.getMaxRenderTime() == -1 && renderTime != null) {
			config.setMaxRenderTime(Integer.valueOf(renderTime));
		}
		
		if (config.isUserHasSpecifiedACacheDir() == false && cacheDir != null) {
			config.setCacheDir(new File(cacheDir));
		}
		
		if (config.getUIType() == null && ui != null) {
			config.setUIType(ui);
		}
		
		if (config.getTheme() == null) {
			if (this.theme != null) {
				config.setTheme(this.theme);
			}
			else {
				config.setTheme("light");
			}
		}
		
		// if the user has invoked the app with --no-systray, then we just overwrite the existing configuration with (boolean)false. If no parameter has been
		// specified and the settings file contains use-systray=false, then deactivate as well.
		if (!config.isUseSysTray() || (config.isUseSysTray() && useSysTray != null && useSysTray.equals("false"))) {
			config.setUseSysTray(false);
		}
		
		config.setAutoSignIn(Boolean.parseBoolean(autoSignIn));
	}
	
	@Override public String toString() {
		return String.format(
			"SettingsLoader [path=%s, login=%s, password=%s, computeMethod=%s, gpu=%s, renderbucket-size=%s, cacheDir=%s, theme=%s, priority=%d, autosign=%s, usetray=%s]",
			path, login, password, computeMethod, gpu, renderbucketSize, cacheDir, theme, priority, autoSignIn, useSysTray);
	}
}
