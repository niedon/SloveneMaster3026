package com.bcadaval.esloveno.services.xml;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.stereotype.Service;

import com.bcadaval.esloveno.beans.enums.Persona;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;

@Deprecated
@Service
public class ScrapService {

	public List<VerboFlexion> scrapVerb(String verb) throws ScrapException {

		WebDriver driver = new FirefoxDriver();
		driver.get("https://www.verbix.com/webverbix/go.php?&D1=244&T1="+verb);
		Document doc = Jsoup.parse(driver.getPageSource());
		Element container = doc.select("#verbixConjugations").first();
		
		if(container.select(".pure-u-1-1").size()>0) {
			throw new ScrapException("No se ha podido conjugar verbo: " + container.toString());
		}
		
		Elements cabecerasConjugacion = container.select("h4");
		
		// Código deprecado - no se usa
		return null;
	}
	
	
}
