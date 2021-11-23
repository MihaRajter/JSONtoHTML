package com.company;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JSON {
    JsonObject json;

    public JSON (JsonObject json){
        this.json=json;
    }

    public JSON() {
        this.json=null;
    }

    public String toHTML(){
        String wrapper="";


        String parentKey=this.json.keySet().toArray()[0].toString();
        JsonElement json=this.json.get(parentKey);
        if (!json.isJsonObject()){
            System.out.println(parentKey+" "+json);
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
            JsonObject json1=new JsonObject();
            json1.add(key,jsonObject.get(key));
            JSON json2=new JSON(json1);
            wrapper += json2.toHTML();
        }
        wrapper += "</" + parentKey + ">\n";
        return wrapper;
    }
}
