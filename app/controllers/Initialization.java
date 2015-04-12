package controllers;

import models.Currency;
import models.CurrencyValue;
import models.Date;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import play.Logger;
import play.api.Play;
import play.mvc.Controller;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import static controllers.Currencies.currencyCalculationSave;

public class Initialization extends Controller {
    public static void readFiles(List<File> fileList) {

        createCurrencies(readSingleFile(fileList.get(0)));

        for (File fileElement : fileList) {
            updateCurrencyData(readSingleFile(fileElement),getDateFromFile(fileElement));
        }
    }

    public static NodeList readSingleFile(File file) {
        DocumentBuilderFactory dbFactory=DocumentBuilderFactory.newInstance();
        DocumentBuilder dbBuilder = null;
        Document doc = null;
        try {
            dbBuilder = dbFactory.newDocumentBuilder();
            doc = dbBuilder.parse(file);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }
        if (doc!=null){
            return doc.getElementsByTagName("pozycja");
        }
        return null;
    }
    public static String getDateFromFile(File file) {
        DocumentBuilderFactory dbFactory=DocumentBuilderFactory.newInstance();
        DocumentBuilder dbBuilder = null;
        Document doc = null;
        try {
            dbBuilder = dbFactory.newDocumentBuilder();
            doc = dbBuilder.parse(file);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }
        if (doc!=null){
            return doc.getElementsByTagName("data_publikacji").item(0).getTextContent();
        }
        return null;
    }

    public static void updateCurrencyData(NodeList nodeList,String date){
        for (int i=0;i<nodeList.getLength();i++){
            Node node = nodeList.item(i);
            Element element = (Element) node;
            Date currencyDate=Currencies.createDate(date);
            Currencies.createCurrencyValue(
                    getElement("kod_waluty", element),
                    currencyDate,
                    getElement("kurs_sredni", element)
            );
        }
    }

    public static void createCurrencies(NodeList nodeList){
        for (int i=0;i<nodeList.getLength();i++){
            Node node = nodeList.item(i);
            Element element = (Element) node;
            Currencies.createCurrency(
                    getElement("nazwa_waluty",element),
                    getElement("kod_waluty",element),
                    getElement("przelicznik",element),
                    getElement("kurs_sredni",element)
            );
        }
    }

    public static String getElement(String name,Element element){
        return element.getElementsByTagName(name).item(0).getTextContent();
    }

    public static void initializeData() {
        Logger.info("Downloading currency average values from NBP. ");
        readFiles(downloadCurrencyValuesFiles(downloadFilesList()));
        calculateMinMaxValues();
    }

    public static void calculateMinMaxValues(){
        List<Currency> currencyList = Currency.find.all();
        for (Currency currency : currencyList){
            BigDecimal minValue=currency.avgValue;
            BigDecimal maxValue=currency.avgValue;
            BigDecimal avgValue=new BigDecimal(0);
            for (CurrencyValue currencyValue : currency.currencyValues){
                if (minValue.compareTo(currencyValue.value)==1)
                    minValue=currencyValue.value;
                else if (maxValue.compareTo(currencyValue.value)==-1)
                    maxValue=currencyValue.value;
                avgValue=avgValue.add(currencyValue.value);
            }
            avgValue=avgValue.divide(new BigDecimal(currency.currencyValues.size()), RoundingMode.HALF_UP);
            currencyCalculationSave(currency, minValue, maxValue, avgValue);
        }
    }

    public static File downloadFilesList() {
        String path = Play.current().path().getAbsolutePath();

        File fileList=new File(path+"/tmpData"+"/dir.txt");
        try {
            URL fileListURL= new URL("http://www.nbp.pl/kursy/xml/dir.txt");
            FileUtils.copyURLToFile(fileListURL, fileList, 10000, 10000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileList;
    }

    public static List<File> downloadCurrencyValuesFiles(File file) {
        List<File> fileList = new ArrayList<>();
        String path = Play.current().path().getAbsolutePath();

        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (scanner != null) {
            scanner.useDelimiter(Pattern.compile(
                    "(\r\n([b-z][0-9]{3}z[0-9]{6}\r\n){1,3}(?=a))|" +
                            "(\uFEFF([b-z][0-9]{3}z[0-9]{6}\r\n){1,3}(?=a))"
            ));
        }

        //k - number of downloaded files, m - number of files that should be downloaded
        int k=0, m = 0;

        LocalDate startDate = LocalDate.of(2007, 1, 1);
        LocalDate maxDate = LocalDate.of(2014,12,31);

        String previousFileName = null;

        //Since we're taking whole years into database, its enough to check for year only.
        while (scanner.hasNext() && startDate.getYear()<=maxDate.getYear()) {
            String s = scanner.next();

            int year = Integer.parseInt(s.substring(5, 7)) + 2000;
            int month = Integer.parseInt(s.substring(7, 9));
            if (startDate.getYear() == year && startDate.getMonth().getValue() == month) {
                previousFileName = s;
            } else if ((startDate.getMonth().getValue() < month &&
                    startDate.getYear() == year)
                    || startDate.getYear()<year){
                m++;
                File f= downloadCurrencyFile(previousFileName);
                if(f!=null){
                    fileList.add(f);
                    k++;
                }
                if (startDate.getYear() < year) {
                    startDate = startDate.withYear(year);
                }
                startDate = startDate.withMonth(month);
            }
        }
        scanner.close();

        Logger.info(+m+" out of "+k+" files have been downloaded.");
        return fileList;
    }

    public static File downloadCurrencyFile(String fileName) {
        String path = Play.current().path().getAbsolutePath();
        path=path+"/tmpData/";
        URL fileURL= null;
        try {
            fileURL = new URL("http://www.nbp.pl/kursy/xml/"+fileName+".xml");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        File currencyFile=new File(path+fileName+".xml");
        Logger.info(fileURL+" is downloaded.");
        try {
            FileUtils.copyURLToFile(fileURL, currencyFile,10000,10000);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        Logger.info("Download complete.");
        return currencyFile;
    }
}
