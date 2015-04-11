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
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Created by keen on 4/11/15.
 */
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
            System.out.println("A "+file.getName()+" is read. "+doc.getDocumentElement().getNodeName());
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
            System.out.println("DATA: "+doc.getElementsByTagName("data_publikacji").item(0).getTextContent());
            return doc.getElementsByTagName("data_publikacji").item(0).getTextContent();
        }
        return null;
    }

    public static void updateCurrencyData(NodeList nodeList,String date){
        for (int i=0;i<nodeList.getLength();i++){
            Node node = nodeList.item(i);
            //System.out.println("Current element: "+node.getNodeName());
            Element element = (Element) node;
            Date currencyDate=Currencies.createDate(
                date
            );
            CurrencyValue currencyValue=Currencies.createCurrencyValue(
                    getElement("kod_waluty", element),
                    currencyDate,
                    getElement("kurs_sredni", element)
            );
            if (currencyValue!=null){
                System.out.println("Nazwa waluty: "+currencyValue.currency.name);
                System.out.println("Średnia wartość: "+currencyValue.value);
            }
        }
    }

    public static void createCurrencies(NodeList nodeList){
        for (int i=0;i<nodeList.getLength();i++){
            Node node = nodeList.item(i);
            System.out.println("Current element: "+node.getNodeName());
            Element element = (Element) node;
            Currency currency=Currencies.createCurrency(
                    getElement("nazwa_waluty",element),
                    getElement("kod_waluty",element),
                    getElement("przelicznik",element),
                    getElement("kurs_sredni",element)
            );
            if (currency!=null){
                System.out.println("Nazwa waluty: "+currency.name);
                System.out.println("Kod: "+currency.code);
                System.out.println("Przelicznik: "+currency.factor);
                System.out.println("Średnia wartość: "+currency.avgValue);
            }

        }
    }

    public static String getElement(String name,Element element){
        return element.getElementsByTagName(name).item(0).getTextContent();
    }

    public static void initializeData() {
        List<File> fileList=new ArrayList<>();

        Logger.info("Downloading currency average values from NBP. ");

        readFiles(downloadCurrencyValuesFiles(downloadFilesList()));
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
        System.out.println("downloadCurrencyValuesFiles method:");
        String path = Play.current().path().getAbsolutePath();
        path = path + "/tmpData/";

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
            //System.out.println("   Year: " + year + ", Month: " + month + ", Day:");
            if (startDate.getYear() == year && startDate.getMonth().getValue() == month) {
                //System.out.println("It is a proper filename.");
                previousFileName = s;
            } else if ((startDate.getMonth().getValue() < month &&
                    startDate.getYear() == year)
                    || startDate.getYear()<year){
                //System.out.println("This file is for download: " + previousFileName + ".xml");
                m++;
                File f= downloadCurrencyFile(previousFileName);
                if(f!=null){
                    fileList.add(f);
                    k++;
                }
                //previousFileName=previousFileName.substring(s.lastIndexOf('z')+1);
                if (startDate.getYear() < year) {
                    startDate = startDate.withYear(year);
                }
                startDate = startDate.withMonth(month);
                //startDate.withDayOfMonth(day);
            }
//            System.out.println("   Year: " + startDate.getYear()
//                    + ", Month: " + startDate.getMonthValue()
//                    + ", Day:" + startDate.getDayOfMonth());
        }
        scanner.close();

        System.out.println("End of downloadCurrencyValuesFiles method.");
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
