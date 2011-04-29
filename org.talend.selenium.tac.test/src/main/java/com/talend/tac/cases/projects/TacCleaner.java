package com.talend.tac.cases.projects;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.Assert;

import com.thoughtworks.selenium.Selenium;

public class TacCleaner {
	public static String MAX_SPEED = "5000";
	public static String MID_SPEED = "5000";
	public static String MIN_SPEED = "5000";
	public Locale currentLocale = new Locale("en", "US");	// set the locale that you want to test
	public ResourceBundle rb = ResourceBundle.getBundle("messages", currentLocale);
	public ResourceBundle other = ResourceBundle.getBundle("other", currentLocale);
 public void cleanProjectsNotused(Selenium selenium) throws InterruptedException{
	 List<String> projects = new ArrayList<String>();
//		this.clickWaitForElementPresent("idMenuUserElement");
	 selenium.click("!!!menu.project.element!!!");
	 Thread.sleep(5000);
		projects = this.findSpecialMachedStrings(selenium,".*@[a-zA-Z0-9]*");
		// users = this.findSpecialMachedStrings(".[A-Za-z]*\\ to ");
		for (int i = 0; i < projects.size(); i++) {
			// System.out.println(users.get(i));
			if (projects.get(i).startsWith("test")||projects.get(i).startsWith("Copy_of_")) {
				selenium.refresh();
				Thread.sleep(5000);
				selenium.mouseDown("//div[@class='x-grid3-cell-inner x-grid3-col-label' and text()='"+projects.get(i)+"']");
				Thread.sleep(3000);
				selenium.chooseOkOnNextConfirmation();
				selenium.click("//div[text()='Projects']/ancestor::div[@class='x-panel-body x-panel-body-noheader x-panel-body-noborder x-border-layout-ct']//button[@id='idSubModuleDeleteButton']");
				selenium.setSpeed(MAX_SPEED);
				assert (selenium.getConfirmation()
						.matches(other.getString("delete.project.warning")));
				selenium.setSpeed(MIN_SPEED);
			}

		}
	 
 }
 public void cleanProjectsForce(Selenium selenium) throws InterruptedException{

 }
 public void cleanUsersExceptAdmin(Selenium selenium) throws InterruptedException{
	 List<String> users = new ArrayList<String>();
	 selenium.click("idMenuUserElement");
	 Thread.sleep(5000);
		users = this.findSpecialMachedStrings(selenium,".*@[a-zA-Z0-9]*\\.com");
		for (int i = 0; i < users.size(); i++) {
			if (!"admin@company.com".equals(users.get(i))) {
				selenium.mouseDown("//div[text()='" + users.get(i) + "']");
				selenium.chooseOkOnNextConfirmation();
				selenium.click("idSubModuleDeleteButton");
				selenium.setSpeed(MAX_SPEED);
				Assert.assertTrue(selenium.getConfirmation().matches(
						"^" + other.getString("delete.User.confirmation")
								+ " [\\s\\S]$"));
				selenium.setSpeed(MIN_SPEED);
			}

		}

	 
 }
 public List<String> findSpecialMachedStrings(Selenium selenium,String regex) 
	{
		List<String> strs = new ArrayList<String>();
		String findedString = "";
		String[] texts;
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher;

		String text = selenium.getBodyText();
//System.out.println(text);
		texts = text.split("\n");
		for (int i = 0; i < texts.length - 1; i++) {
//System.out.println("text " + i +": " + texts[i]);
			matcher = pattern.matcher(texts[i].trim());
			if (matcher.matches()) {
				findedString = texts[i].trim();
				strs.add(findedString);
//System.out.println(texts[i].trim());
				continue;
			}
		}
		return strs;
	}
}
