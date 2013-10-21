package me.parapenguin.overcast.scrimmage.map;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import lombok.Getter;
import me.parapenguin.overcast.scrimmage.Scrimmage;
import me.parapenguin.overcast.scrimmage.map.extras.Contributor;

public class MapLoader {
	
	@Getter File file;
	@Getter Document doc;
	
	@Getter Map map;
	
	private MapLoader(File file, Document doc) {
		/*
		 * Load the map and it's attributes now ready for loading into the rotation
		 */
		
		Element root = doc.getRootElement();
		
		String name = root.elementText("name");
		String version = root.elementText("version");
		String objective = root.elementText("objective");
		List<String> authors = getList("authors", "author");
		List<String> rules = getList("rules", "rule");
		
		List<Contributor> contributors = new ArrayList<Contributor>();
		Element contributorsElement = root.element("contributors");
		
		int cur = 0;
		while(contributorsElement.elements().size() < cur) {
			if(((Element) contributorsElement.elements().get(cur)).getName().equalsIgnoreCase("contributor")) {
				String contributorName = ((Element) contributorsElement.elements().get(cur)).getText();
				String contribution = ((Element) contributorsElement.elements().get(cur)).attributeValue("contribution");
				contributors.add(new Contributor(contributorName, contribution));
			}
			cur++;
		}
		
		List<MapTeam> teams = new ArrayList<MapTeam>();
		Element teamsElement = root.element("teams");
		
		cur = 0;
		while(teamsElement.elements().size() < cur) {
			if(((Element) teamsElement.elements().get(cur)).getName().equalsIgnoreCase("team")) {
				String teamName = ((Element) contributorsElement.elements().get(cur)).getText();
				String teamCap = ((Element) contributorsElement.elements().get(cur)).attributeValue("max");
				String teamColor = ((Element) contributorsElement.elements().get(cur)).attributeValue("color");
				MapTeam team = new MapTeam(teamName, teamColor, teamCap);
				if(team.getColor() == null || team.getColor() == ChatColor.AQUA)
					Scrimmage.getInstance().getLogger().info("Failed to load team '" + teamName + "' due to having an invalid color supplied!");
				else
					teams.add(team);
			}
			cur++;
		}
		
		for(MapTeam team : teams)
			team.load(root.element("spawns"));
	}
	
	public static boolean isLoadable(File file) {
		SAXReader reader = new SAXReader();
		try {
			reader.read(file);
			return true;
		} catch (DocumentException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static MapLoader getLoader(File file) {
		if(!isLoadable(file))
			return null;
		
		SAXReader reader = new SAXReader();
		Document doc;
		try {
			doc = reader.read(file);
		} catch (DocumentException e) {
			/* This should never happen */
			return null;
		}
		
		return new MapLoader(file, doc);
	}
	
	private List<String> getList(String container, String contains) {
		Element root = doc.getRootElement();
		
		List<String> contents = new ArrayList<String>();
		Element containerElement = root.element(container);
		
		int cur = 0;
		while(containerElement.elements().size() < cur) {
			if(((Element) containerElement.elements().get(cur)).getName().equalsIgnoreCase(contains))
				contents.add(((Element) containerElement.elements().get(cur)).getText());
			cur++;
		}
		
		return contents;
	}
	
	public static List<Element> getElements(Element from, String name) {
		List<Element> elements = new ArrayList<Element>();
		
		int cur = 0;
		while(from.elements().size() < cur) {
			if(((Element) from.elements().get(cur)).getName().equalsIgnoreCase(name))
				elements.add(((Element) from.elements().get(cur)));
			cur++;
		}
		
		return elements;
	}
	
}
