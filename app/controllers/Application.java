package controllers;

import models.Currency;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import play.Logger;
import play.api.Play;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public static void readFiles() {

        String fileName="a021z140131";
        readSingleFile(fileName);
    }

    public static void readSingleFile(String fileName) {
        String path = Play.current().path().getAbsolutePath()+"/tmpData/";

        File file = new File(path+fileName+".xml");

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
            System.out.println("Root"+doc.getDocumentElement().getNodeName());
            NodeList nodeList=doc.getElementsByTagName("pozycja");
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
        Logger.info("Downloading currency average values from NBP. ");
        downloadCurrencyValuesFiles(downloadFilesList());
        readFiles();
    }

    public static File downloadFilesList() {
        String path = Play.current().path().getAbsolutePath();

        File fileList=new File(path+"/tmpData"+"/dir.txt");
        try {
            URL fileListURL= new URL("http://www.nbp.pl/kursy/xml/dir.txt");
            FileUtils.copyURLToFile(fileListURL, fileList,10000,10000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileList;
    }

    public static void downloadCurrencyValuesFiles(File fileList) {
        System.out.println("downloadCurrencyValuesFiles method:");
        String path = Play.current().path().getAbsolutePath();
        path = path + "/tmpData/";

        Scanner scanner = null;
        try {
            scanner = new Scanner(fileList);
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
                    if(downloadCurrencyFile(previousFileName)){
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
    }

    public static boolean downloadCurrencyFile(String fileName) {
        String path = Play.current().path().getAbsolutePath();
        path=path+"/tmpData/";
        URL fileURL= null;
        try {
            fileURL = new URL("http://www.nbp.pl/kursy/xml/"+fileName+".xml");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
        File currencyFile=new File(path+fileName+".xml");
        Logger.info(fileURL+" is downloaded.");
        try {
            FileUtils.copyURLToFile(fileURL, currencyFile,10000,10000);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        Logger.info("Download complete.");
        return true;
    }
}
