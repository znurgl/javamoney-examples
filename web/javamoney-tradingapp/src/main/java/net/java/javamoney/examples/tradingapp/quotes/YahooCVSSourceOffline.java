/*
 * Java Financial Library
 * Extension
 * 
 * Copyright 2005-2013, Werner Keil and individual contributors by the @author tag. 
 * See the copyright.txt in the distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.java.javamoney.examples.tradingapp.quotes;

import static net.java.javamoney.examples.tradingapp.Constants.COLON;
import static net.java.javamoney.examples.tradingapp.Constants.CSV_EXT;
import static net.java.javamoney.examples.tradingapp.Constants.PROPS_EXT;
import static net.java.javamoney.examples.tradingapp.Constants.TRADING;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.StringTokenizer;

import net.neurotech.quotes.Quote;
import net.neurotech.quotes.QuoteException;
import net.neurotech.quotes.YahooCSVSource;

import org.apache.log4j.Logger;

/**
 * @author Werner Keil
 * @see YahooCSVSource
 */
public class YahooCVSSourceOffline extends YahooCSVSource {
	private Quote quote;
	private String symbol;
	private static final String DEF_FOLDER = "/temp/";
	private static final Properties props = new Properties();
	
	@Override
	public boolean fetch(Quote quote) throws QuoteException {
		this.quote = quote;
		symbol = quote.getSymbol();
		String content;
/*
		String u = "http://finance.yahoo.com/d/quotes.csv?s=" + symbol
			+ "&f=snl1d1t1c1ohgv&e=.c";
*/
        try {
        	InputStream in = getClass().getResourceAsStream 
        		("/" + TRADING + PROPS_EXT);
        	props.load(in);
        } catch (IOException ie) {
        	logger.warn("Error loading properties", ie); 
        }
		
        String folder = props.getProperty(TRADING + ".quotefolder", DEF_FOLDER);       
		String u = folder + symbol + CSV_EXT;
		try {
			InputStream is = getClass().getResourceAsStream(u);
			BufferedReader in = new BufferedReader(new InputStreamReader(is));

			content = in.readLine();
			in.close();

			//if(DEBUG) {
				//System.out.println(content);
				logger.debug(content);
			//}
		} catch (Exception e) {
			throw new QuoteException("Couldn't retrieve quote - " + e);
		}

		StringTokenizer tk = new StringTokenizer(content, COLON);
		quote.setCompany(tk.nextToken()); // symbol/name
		//quote.setCompany(stripQuotes(tk.nextToken())); 	// name @deprecated no longer provided
		quote.setValue(Float.parseFloat(tk.nextToken())); 	// value
		tk.nextToken(); // date
		tk.nextToken(); // time
		tk.nextToken(); // net

		try {
			quote.setOpenPrice(Float.parseFloat(tk.nextToken())); // open price
		} catch(NumberFormatException nfe) {
			quote.setOpenPrice(0);
		}

		tk.nextToken(); // Daily High
		tk.nextToken(); // Daily Low

		try {
			quote.setVolume(Long.parseLong(tk.nextToken()));
		} catch(NumberFormatException nfe) {
			quote.setVolume(0);
		}

		return true;
	}

	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger
			.getLogger(YahooCVSSourceOffline.class);
	
	public YahooCVSSourceOffline() {
		super();
		logger.debug("in constructor"); // TODO change to info if required.
	}
	
	private static final String stripQuotes(String q)
	{
		String s;

		s = q.substring(1, q.length() - 1);

		return s;
	}
}
