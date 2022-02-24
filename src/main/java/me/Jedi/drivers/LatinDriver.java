package me.Jedi.drivers;

import me.Jedi.utils.Utils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LatinDriver extends Driver{
    private WebDriver driver;
    private JavascriptExecutor js;
    private Map<Letter, WebElement> keyboardMap;
    private String url;

    public LatinDriver(String url) {
        this.url = url;
    }

    @Override
    public void open() {
        System.setProperty("webdriver.chrome.driver", "C:\\Program Files\\WordleBot\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        js =  (JavascriptExecutor) driver;

        driver.get(url);

        WebElement keyboard = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div[3]"));
        System.out.println(keyboard.getAccessibleName());
        List<WebElement> keys = keyboard.findElements(By.tagName("button"));

        keyboardMap = new HashMap<>();

        for(WebElement k : keys) {

            String key = k.getText();

            keyboardMap.put(Letter.valueOf(key.toUpperCase()), k);
        }
    }

    @Override
    public String getInfo(int guess) throws InterruptedException {
        Thread.sleep(1000);

        WebElement row = (WebElement) js.executeScript("return document.querySelector(\"#root > div > div > div.pb-6 > div:nth-child(" + guess + ")\")");

        List<WebElement> letters = row.findElements(By.tagName("div"));

        List<Character> info = new ArrayList<>();

        for(WebElement l : letters) {
            Pattern pattern = Pattern.compile("bg-[a-z]*-[0-9]00", Pattern.CASE_INSENSITIVE);
            String classInfo = l.getDomAttribute("class");
            Matcher matcher = pattern.matcher(classInfo);
            matcher.find();
            String evaluation  = matcher.group(0);

            Character c;

            if(evaluation.equalsIgnoreCase("bg-green-500")) c = 'g';
            else if(evaluation.equalsIgnoreCase("bg-yellow-500")) c = 'y';
            else c = 'n';

            info.add(c);
        }

        return Utils.charListToString(info);
    }

    @Override
    protected void pressKey(Character c) {
        keyboardMap.get(Letter.valueOf(c.toString().toUpperCase())).click();
    }

    @Override
    protected void pressEnter() {
        keyboardMap.get(Letter.ENTER).click();
    }

    public enum Letter {
        A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z, DELETE, ENTER;
    }
}
