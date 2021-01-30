package com.example.currencyconverterwithxmlparser;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private NumberPicker FromNumberPicker, ToNumberPicker;
    private TextView resultText;
    private EditText inputText;
    private Button convertButton;
    private ArrayList<String> currencyArrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new HukoAsyncGetCurrenciesClass().execute();
        currencyArrayList = new ArrayList<String>();
        FromNumberPicker = (NumberPicker)findViewById(R.id.FromCurrency);
        ToNumberPicker = (NumberPicker)findViewById(R.id.ToCurrency);
        resultText = (TextView)findViewById(R.id.resultText);
        inputText = (EditText)findViewById(R.id.inputText);
        convertButton = (Button)findViewById(R.id.convertButton);
    }

    public void HukoCurrencyConverterButtonClickEvent(View v) {
        if(v.getId() == convertButton.getId())
            new HukoAsyncSelectedCurrencyConverter().execute();
    }
    public void HukoShowConvertedCurrency_XMLParserFunction (String hukoXML) throws XmlPullParserException, IOException
    {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(new StringReader(hukoXML));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.TEXT) {
                    resultText.setText(String.format("%.2f",Double.parseDouble(xpp.getText())) + " " + (ToNumberPicker.getDisplayedValues()[ToNumberPicker.getValue()]));
                }
                eventType = xpp.next();
            }
    }
    public void HukoFillNumberPicker_XMLParserFunction (String hukoXML) throws XmlPullParserException, IOException
    {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(new StringReader(hukoXML ));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.TEXT) {
                    if(!xpp.getText().startsWith(" "))
                        currencyArrayList.add(xpp.getText());
                }
                eventType = xpp.next();
            }
            FromNumberPicker.setMinValue(0);
            FromNumberPicker.setMaxValue(currencyArrayList.size()-1);
            ToNumberPicker.setMinValue(0);
            ToNumberPicker.setMaxValue(currencyArrayList.size()-1);

            String[] temp = currencyArrayList.toArray(new String[0]);
            FromNumberPicker.setDisplayedValues(temp);
            ToNumberPicker.setDisplayedValues(temp);
    }

    private class HukoAsyncGetCurrenciesClass extends AsyncTask<String, String, StringBuffer>
    {
        @Override
        //FUNC ICINDE URL TANIMLAMAK YERINE URL YI DISARIDAN DA ALABILIRSIN (BIRDEN FAZLA URL varsa url[0] diyerek erisim yapabilirsin, URL GIRILMEYE DE BILIR)
        protected StringBuffer doInBackground(String... URLs)
        {
            URL url = null;
            StringBuffer response = new StringBuffer();
            try { url = new URL("http://currencyconverter.kowabunga.net/converter.asmx/GetCurrencies"); } catch (MalformedURLException e) { e.printStackTrace(); }
            HttpURLConnection con = null;
            try
            {
                con = (HttpURLConnection) url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    response.append(inputLine);
                in.close();
            } catch (IOException e) { e.printStackTrace(); }
            try { con.setRequestMethod("GET"); } catch (ProtocolException e) { e.printStackTrace(); }
            return response;
        }
        @Override
        protected void onPostExecute(StringBuffer s) {
            super.onPostExecute(s);
            try { HukoFillNumberPicker_XMLParserFunction(s.toString()); }catch (Exception e) { }
        }
    }
    private class HukoAsyncSelectedCurrencyConverter extends AsyncTask<String, String, StringBuffer> {

        @Override
        //FUNC ICINDE URL TANIMLAMAK YERINE URL YI DISARIDAN DA ALABILIRSIN (BIRDEN FAZLA URL varsa url[0] diyerek erisim yapabilirsin, URL GIRILMEYE DE BILIR)
        protected StringBuffer doInBackground(String... URLs)
        {
            URL url = null;
            StringBuffer response = new StringBuffer();
            try
            {
                String currentDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());
                url = new URL("http://currencyconverter.kowabunga.net/converter.asmx/GetConversionAmount?CurrencyFrom=" + FromNumberPicker.getDisplayedValues()[FromNumberPicker.getValue()] + "&CurrencyTo=" + ToNumberPicker.getDisplayedValues()[ToNumberPicker.getValue()] + "&RateDate=" + currentDate + "&Amount=" + inputText.getText());
            } catch (MalformedURLException e) { e.printStackTrace(); }
            HttpURLConnection con = null;
            try
            {
                con = (HttpURLConnection) url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            } catch (IOException e) { e.printStackTrace(); }
            try { con.setRequestMethod("GET"); } catch (ProtocolException e) { e.printStackTrace(); }
            return response;
        }
        @Override
        protected void onPostExecute(StringBuffer response) {
            super.onPostExecute(response);
            try { HukoShowConvertedCurrency_XMLParserFunction(response.toString()); }catch (Exception e) {e.printStackTrace(); }
        }
    }
}