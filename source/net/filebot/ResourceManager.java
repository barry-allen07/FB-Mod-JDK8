package net.filebot;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;

import net.filebot.util.SystemProperty;

public final class ResourceManager {

	private static final Map<String, Icon> cache = synchronizedMap(new HashMap<String, Icon>(256));

	public static Icon getIcon(String name) {
		return cache.computeIfAbsent(name, i -> {
			// load image
			URL[] resource = getMultiResolutionImageResource(i);
			if (resource.length > 0) {
				return new ImageIcon(getMultiResolutionImage(resource));
			}

			// default image
			return null;
		});
	}

	public static Stream<URL> getApplicationIconResources() {
		return Stream.of("window.icon16", "window.icon64").map(ResourceManager::getImageResource);
	}

	public static List<Image> getApplicationIconImages() {
		return Stream.of("window.icon16", "window.icon64").map(ResourceManager::getMultiResolutionImageResource).map(ResourceManager::getMultiResolutionImage).collect(toList());
	}

	public static Icon getFlagIcon(String languageCode) {
		return getIcon("flags/" + languageCode);
	}

	private static Image getMultiResolutionImage(URL[] resource) {
		try {
			return ImageIO.read(resource[0]);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static URL[] getMultiResolutionImageResource(String name) {
		return Stream.of(name, name + "@2x").map(ResourceManager::getImageResource).filter(Objects::nonNull).toArray(URL[]::new);
	}

	private static URL getImageResource(String name) {
		return ResourceManager.class.getResource("resources/" + name + ".png");
	}

	private static final float PRIMARY_SCALE_FACTOR = SystemProperty.of("sun.java2d.uiScale", Float::parseFloat, Toolkit.getDefaultToolkit().getScreenResolution() / 96f).get();

	private static BufferedImage scale(float scale, BufferedImage image) {
		int w = (int) (scale * image.getWidth());
		int h = (int) (scale * image.getHeight());
		return Scalr.resize(image, Method.ULTRA_QUALITY, Mode.FIT_TO_WIDTH, w, h, Scalr.OP_ANTIALIAS);
	}

}
