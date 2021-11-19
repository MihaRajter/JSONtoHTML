package com.company;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args){
	    Path path= Path.of(args[0]+".json");
        Gson gson = new Gson();
        JsonObject json=null;
        try {
            json = gson.fromJson(Files.readString(path), JsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(json);


        String html=wrapper(json);
        //System.out.println(html);

        FileWriter fileWriter = null;
        BufferedWriter bf = null;

        try {
            fileWriter = new FileWriter(args[0] + "test.html");
            bf = new BufferedWriter(fileWriter);
            bf.write(html);
            bf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * Writes the <html> </html> part with parameters and doctype if it exists. Calls functions for more inner JSON parts
     * @param json (Entire JsonObject that was read from file)
     * @return wrapper (vrne html String)
     */
    private static String wrapper(JsonObject json) {
        //doctype
        String wrapper=jsonToDoctype(json);

        //check for attributes
        ArrayList<String> attributes=new ArrayList<String>();
        attributes.add("language");
        attributes.add("xmlns");
        wrapper += "<html ";

        //string for parameters, removing them after parsing
        //lang="en" xmlns="http://www.w3.org/1999/xhtml"

        for (String att : attributes){
            if (json.has(att)){
                if(att.equals("language")){
                    wrapper=wrapper + "lang=" + json.get(att) + " ";
                    json.remove(att);
                    continue;
                }
                wrapper=wrapper + att + "=" + json.get(att) + " ";
                json.remove(att);
            }
        }
        wrapper=wrapper + ">\n";

        //reads rest of the file. head has separate function for different HTML syntax.
        for (String key : json.keySet()){
            String html="";

            switch (key) {
                case "head":
                    html=headToHtml(key,json.get(key).getAsJsonObject());
                    break;
                default:
                    html=jsonToHtml(key,json.get(key).getAsJsonObject());
                    break;
             }
             wrapper  += html;
        }

        wrapper=wrapper + "</html>";
        return wrapper;

    }

    /**
     * Handles the head. special cases for meta, link, charset..
     * @param parentKey
     * @param json (the "head" object)
     * @return wrapper, String in html format
     */
    private static String headToHtml(String parentKey, JsonObject json) {
        String wrapper="<head>\n";
        String html="";
        for (String key: json.keySet()){

            switch(key){
                case "meta":
                    for (String metaKey: json.get(key).getAsJsonObject().keySet()){
                        //"charset": "utf-8" -> <meta charset="utf-8">
                        if (metaKey.equals("charset")){
                            html  += "<meta charset=" + json.get(key).getAsJsonObject().get(metaKey) + ">\n";
                        }
                        else{
                            String content="";
                            //for viewport/other keys with an extra level
                            if(json.get(key).getAsJsonObject().get(metaKey).isJsonObject()){
                                for (String key2 : json.get(key).getAsJsonObject().get(metaKey).getAsJsonObject().keySet()){
                                    content += key2 + "=" + json.get(key).getAsJsonObject().get(metaKey).getAsJsonObject().get(key2).getAsString() + ", ";
                                }
                                content = content.substring(0, content.length()-2);
                            }
                            //"author": "Flawless Code",-> <meta name="author" content="Flawless Code">
                            else{
                                content=json.get(key).getAsJsonObject().get(metaKey).getAsString();
                            }
                            html  += "<meta name=\"" + metaKey + "\" content=\"" + content + "\">\n";
                        }

                    }
                    break;
                //json array in link -> <link href="style.css" rel="stylesheet" type="text/css">, <link href="icon.ico" rel="icon">
                case "link":
                    JsonArray jsonArray=json.get(key).getAsJsonArray();
                    for (JsonElement jsonElement:jsonArray){
                        JsonObject jsonObject=jsonElement.getAsJsonObject();
                        html  += "<link ";
                        for (String linkKey : jsonObject.keySet()){
                            html  += linkKey + "=" + jsonObject.get(linkKey) + " ";
                        }
                        html  += ">\n";
                    }
                    //System.out.println(jsonArray);
                    break;
                //any other json objects with "normal" html syntax like <title>Hello World v10</title>
                default:
                    html  += jsonToHtml(key, json.get(key));
                    break;
            }
            //System.out.println(html);
        }
        wrapper  += html;
        wrapper  += "</head>\n";

        return wrapper;
    }

    /**
     * Recursive, handles any json with "normal" syntax
     * @param parentKey (parentKey is the key for json)
     * @param json ("normal" syntax HTML. Form: <key attribute_string> value </key>. Value can be another json node -> recursive)
     * @return wrapper, a string in HTML syntax.
     */
    private static String jsonToHtml(String parentKey, JsonElement json) {
        String wrapper="";
        //exit condition. for turns key:value pair into <key> value </key>.
        if (!json.isJsonObject()){
            wrapper="<" + parentKey + ">" + json.getAsString() + "</" + parentKey + ">\n";
            //<title>Hello World v10</title>
            return wrapper;
        }
        //parse attributes, makes attribute_string, deletes attributes after
        JsonObject jsonObject=json.getAsJsonObject();
        if(jsonObject.has("attributes")){
            wrapper="<" + parentKey + " ";
            for (String att : jsonObject.get("attributes").getAsJsonObject().keySet()){
                JsonElement attValue=jsonObject.get("attributes").getAsJsonObject().get(att);
                if(!attValue.isJsonObject()){
                    wrapper += att + "=" + attValue + " ";
                    //wrapper += ">\n";
                }
                else{
                    wrapper += att + "=\"";
                    for (String attKey : attValue.getAsJsonObject().keySet()){
                        wrapper += attKey + ":" + attValue.getAsJsonObject().get(attKey).getAsString()+";";
                    }
                    wrapper = wrapper.substring(0, wrapper.length() - 1);
                    wrapper += "\"";
                }
                //System.out.println(wrapper);
            }
            wrapper += ">\n";
            //System.out.println("here\n"+wrapper);
            jsonObject.remove("attributes");
        }
        else{
            wrapper="<" + parentKey + ">\n";
        }
        //recursive call for the rest of the jsonObjects left
        for (String key : jsonObject.keySet()){
            wrapper += jsonToHtml(key, jsonObject.get(key));
        }
        wrapper += "</" + parentKey + ">\n";
        return wrapper;
    }

    /**
     * @param json (entire json file)
     * @return doctype, string with form <!DOCTYPE type>
     */
    private static String jsonToDoctype(JsonObject json) {
        String doctype="";
        try {
            doctype = "<!DOCTYPE "  +  json.get("doctype").getAsString()  +  ">";
            json.remove("doctype");
            doctype=doctype + "\n";
        }catch(NullPointerException e){
            System.out.println("There is no doctype");
        }
        return doctype;
    }
}
