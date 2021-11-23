package com.company;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JSONHead extends JSON {
    JsonObject json;

    public JSONHead(JsonObject json){
        this.json=json;
    }

    @Override
    public String toHTML() {
        JsonObject json=this.json;
        String wrapper="<head>\n";
        String html="";
        for (String key: json.keySet()){

            System.out.println(key);
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
                    JsonObject jsonObject=new JsonObject();
                    jsonObject.add(key,json.get(key));
                    JSON jsonDefault=new JSON(jsonObject);
                    html  += jsonDefault.toHTML();
                    break;
            }
            //System.out.println(html);
        }
        wrapper  += html;
        wrapper  += "</head>\n";

        return wrapper;

    }
}
