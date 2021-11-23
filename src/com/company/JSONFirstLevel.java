package com.company;

import com.google.gson.JsonObject;

import java.util.ArrayList;

public class JSONFirstLevel extends JSON {
    JsonObject json;

    public JSONFirstLevel(JsonObject json) {
        this.json=json;
    }

    @Override
    public String toHTML() {
        JsonObject json=this.json;
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
            JsonObject jsonObject=new JsonObject();
            jsonObject.add(key,json.get(key));

            switch (key) {
                case "head":
                    JSONHead json1=new JSONHead(json.get("head").getAsJsonObject());
                    html = json1.toHTML();
                    break;
                default:
                    JSON json2=new JSON(jsonObject);
                    html=json2.toHTML();
                    break;
            }
            wrapper  += html;
        }

        wrapper=wrapper + "</html>";
        return wrapper;
    }

    private String jsonToDoctype(JsonObject json) {
        String doctype="";
        if(json.has("doctype")){
            doctype = "<!DOCTYPE "  +  json.get("doctype").getAsString()  +  ">\n";
        }
        return doctype;
    }
}
