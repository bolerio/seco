package seco.notebook.javascript;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.NativeFunction;

import seco.notebook.javascript.JSCompletionProvider.JSMethod;
import seco.notebook.javascript.JSCompletionProvider.JSProperty;
import seco.notebook.syntax.java.JavaResultItem;

public class BUILDINS
{
    public static final String DATE = "Date";
    public static final String REG_EXP = "RegExp";
    public static final String BOOL = "Boolean";
    public static final String OBJECT = "Object";
    public static final String ARRAY = "Array";
    public static final String NUM = "Number";
    public static final String STRING = "String";
    
    private static final String VOID = "void";
 // private static final String FUNCTION = "Function";
    
    private static final String[] ARR_NUM = new String[] { NUM };
    private static final String[] ARR_STR = new String[] { STRING };
    static Map<String, List<JavaResultItem>> objectsMap;

    private static final JSProperty PROP_PROTO = new JSProperty("prototype",
            OBJECT);
    private static final JSProperty PROP_CTR = new JSProperty("constructor",
            OBJECT);
    private static final JSProperty PROP_LENGHT = new JSProperty("length", NUM);
    private static final JSMethod PROP_VALUE_OF = new JSMethod("valueOf",
            OBJECT, Modifier.PUBLIC);
    private static final JSMethod PROP_TO_STRING = new JSMethod("toString",
            STRING, Modifier.PUBLIC);

    static List<JavaResultItem> getParams(String class_name)
    {
        return getMap().get(class_name);
    }

    static boolean isBuiltInType(String class_name)
    {
        return getMap().keySet().contains(class_name);
    }

    static Map<String, List<JavaResultItem>> getMap()
    {
        if (objectsMap == null)
        {
            objectsMap = new HashMap<String, List<JavaResultItem>>();
            objectsMap.put(DATE, date());
            objectsMap.put(STRING, string());
            objectsMap.put(ARRAY, array());
            objectsMap.put(BOOL, bool());
            objectsMap.put(NUM, number());
            objectsMap.put(REG_EXP, regexp());
            objectsMap.put(OBJECT, object());
        }
        return objectsMap;
    }

    private static List<JavaResultItem> date()
    {
        List<JavaResultItem> params = new ArrayList<JavaResultItem>();

        params.add(PROP_CTR);
        params.add(PROP_TO_STRING);
        params.add(new JSMethod("toTimeString", STRING, Modifier.PUBLIC));
        params.add(new JSMethod("toDateString", STRING, Modifier.PUBLIC));
        params.add(new JSMethod("toLocaleString", STRING, Modifier.PUBLIC));
        params.add(new JSMethod("toLocaleTimeString", STRING, Modifier.PUBLIC));
        params.add(new JSMethod("toLocaleDateString", STRING, Modifier.PUBLIC));
        params.add(new JSMethod("toUTCString", "toLocaleTimeString",
                Modifier.PUBLIC));
        params.add(new JSMethod("toSource", STRING, Modifier.PUBLIC));
        params.add(PROP_VALUE_OF);
        params.add(new JSMethod("getTime", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("getYear", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("getFullYear", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("getUTCFullYear", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("getMonth", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("getUTCMonth", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("getDate", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("getUTCDate", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("getDay", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("getUTCDay", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("getHours", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("getUTCHours", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("getMinutes", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("getUTCMinutes", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("getSeconds", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("getUTCSeconds", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("getMilliseconds", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("getUTCMilliseconds", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("getTimezoneOffset", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("setTime", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("setMilliseconds", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("setUTCMilliseconds", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("setSeconds", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("setUTCSeconds", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("setMinutes", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("setUTCMinutes", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("setHours", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("setUTCHours", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("setDate", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("setUTCDate", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("setMonth", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("setUTCMonth", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("setFullYear", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("setUTCFullYear", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("setYear", NUM, Modifier.PUBLIC));
        params.add(new JSMethod("parse", NUM, ARR_STR,
                new String[] { "dateString" }, Modifier.STATIC));
        params.add(new JSMethod("UTC", NUM, new String[] { NUM, NUM, NUM, NUM,
                NUM }, new String[] { "year", "month", "date", "hrs", "min",
                "sec", "ms" }, Modifier.STATIC));
        return params;
    }

    private static List<JavaResultItem> string()
    {
        List<JavaResultItem> params = new ArrayList<JavaResultItem>();
        params.add(new JSMethod("anchor", STRING, ARR_STR,
                new String[] { "name" }));
        params.add(new JSMethod("big", STRING));
        params.add(new JSMethod("blink", STRING));
        params.add(new JSMethod("bold", STRING));
        params.add(new JSMethod("charAt", NUM, ARR_NUM,
                new String[] { "index" }));
        params.add(new JSMethod("charCodeAt", NUM, ARR_NUM,
                new String[] { "index" }));
        params.add(new JSMethod("concat", STRING, ARR_STR,
                new String[] { "string2" }));
        params.add(new JSMethod("fixed", STRING));
        params.add(new JSMethod("fontcolor", STRING, ARR_STR,
                new String[] { "color" }));
        params.add(new JSMethod("fontsize", STRING, ARR_STR,
                new String[] { "size" }));
        params.add(new JSMethod("fromCharCode", STRING));
        params.add(new JSMethod("indexOf", NUM, new String[] { STRING, NUM },
                new String[] { "searchString", "startPositionIndex" }));
        params.add(new JSMethod("italics", STRING));
        params.add(new JSMethod("lastIndexOf", NUM,
                new String[] { STRING, NUM }, new String[] { "searchString",
                        "startPositionIndex" }));
        params
                .add(new JSMethod("link", STRING, ARR_STR,
                        new String[] { "url" }));
        params.add(new JSMethod("slice", STRING, new String[] { NUM, NUM },
                new String[] { "startPositionIndex", "endPositionIndex" }));
        params.add(new JSMethod("small", STRING));
        params.add(new JSMethod("split", ARRAY, ARR_STR,
                new String[] { "delimiter" }));
        params.add(new JSMethod("strike", STRING));
        params.add(new JSMethod("sub", STRING));
        params.add(new JSMethod("substr", STRING, new String[] { NUM, NUM },
                new String[] { "startPositionIndex", "length" }));
        params.add(new JSMethod("substring", STRING, new String[] { NUM, NUM },
                new String[] { "startPositionIndex", "endPositionIndex" }));
        params.add(new JSMethod("sup", STRING));
        params.add(new JSMethod("toLowerCase", STRING));
        params.add(new JSMethod("toUpperCase", STRING));
        return params;
    }

    private static List<JavaResultItem> array()
    {
        List<JavaResultItem> params = new ArrayList<JavaResultItem>();
        params.add(PROP_CTR);
        params.add(PROP_LENGHT);
        params.add(PROP_PROTO);
        params.add(new JSMethod("concat", ARRAY, new String[] { ARRAY },
                new String[] { "array2" }));
        params.add(new JSMethod("join", OBJECT, ARR_STR,
                new String[] { "delimiterString" }));
        params.add(new JSMethod("pop", OBJECT));
        params.add(new JSMethod("push", OBJECT, new String[] { OBJECT },
                new String[] { "value" }));
        params.add(new JSMethod("reverse", ARRAY));
        params.add(new JSMethod("shift", OBJECT));
        params.add(new JSMethod("slice", ARRAY, new String[] { NUM, NUM },
                new String[] { "startIndex", "endIndex" }));
        params.add(new JSMethod("sort", ARRAY, new String[] { OBJECT },
                new String[] { "compareFunction" }));
        params.add(new JSMethod("unshift", NUM, new String[] { OBJECT },
                new String[] { "value" }));

        return params;
    }

    private static List<JavaResultItem> bool()
    {
        List<JavaResultItem> params = new ArrayList<JavaResultItem>();
        params.add(PROP_CTR);
        params.add(PROP_PROTO);
        params.add(PROP_TO_STRING);
        params.add(PROP_VALUE_OF);

        return params;
    }

    private static List<JavaResultItem> number()
    {
        List<JavaResultItem> params = new ArrayList<JavaResultItem>();
        params.add(PROP_CTR);
        params.add(PROP_PROTO);
        params.add(new JSProperty("MAX_VALUE", NUM));
        params.add(new JSProperty("MIN_VALUE", NUM));
        params.add(new JSProperty("NEGATIVE_INFINITY", NUM));
        params.add(new JSProperty("POSITIVE_INFINITY", NUM));

        params.add(new JSMethod("toExponential", STRING, ARR_NUM,
                new String[] { "x" }));
        params.add(new JSMethod("toFixed", STRING, ARR_NUM,
                new String[] { "x" }));
        params.add(new JSMethod("toPrecision", STRING, ARR_NUM,
                new String[] { "x" }));

        params.add(PROP_TO_STRING);
        params.add(PROP_VALUE_OF);

        return params;
    }

    private static List<JavaResultItem> regexp()
    {
        List<JavaResultItem> params = new ArrayList<JavaResultItem>();
        params.add(new JSProperty("global", BOOL));
        params.add(new JSProperty("ignoreCase", BOOL));
        params.add(new JSProperty("lastIndex", NUM));
        params.add(new JSProperty("multiline", BOOL));
        params.add(new JSProperty("source", STRING));

        params.add(new JSMethod("compile", VOID));
        params.add(new JSMethod("exec", VOID));
        params.add(new JSMethod("test", BOOL));

        return params;
    }

    private static List<JavaResultItem> object()
    {

        List<JavaResultItem> params = new ArrayList<JavaResultItem>();
        params.add(PROP_CTR);
        params.add(PROP_PROTO);

        params.add(new JSMethod("hasOwnProperty", BOOL, ARR_STR,
                new String[] { "prop" }));
        params.add(new JSMethod("isPrototypeOf", BOOL, new String[] { OBJECT },
                new String[] { "object" }));
        params.add(new JSMethod("propertyIsEnumerable", BOOL, ARR_STR,
                new String[] { "prop" }));
        params.add(new JSMethod("toLocaleString", STRING));
        params.add(new JSMethod("toSource", STRING));
        params.add(new JSMethod("unwatch", VOID, ARR_STR,
                new String[] { "prop" }));
        params.add(new JSMethod("watch", VOID, new String[] { STRING, OBJECT },
                new String[] { "prop", "handler" }));
        params.add(PROP_TO_STRING);
        params.add(PROP_VALUE_OF);

        return params;
    }
    
    private static List<JavaResultItem> this_()
    {
        List<JavaResultItem> params = new ArrayList<JavaResultItem>();
        
        params.add(new JSProperty("Infinity", NUM));
        params.add(new JSProperty("NaN", NUM));
        params.add(new JSProperty("undefined", OBJECT));
        
        params.add(new JSMethod("decodeURI", STRING, ARR_STR,
                new String[] { "str" }));
        params.add(new JSMethod("decodeURIComponent", STRING, ARR_STR,
                        new String[] { "str" }));
        params.add(new JSMethod("encodeURI", STRING, ARR_STR,
                                new String[] { "str" }));
        params.add(new JSMethod("encodeURIComponent", STRING, ARR_STR,
                                        new String[] { "str" }));
        params.add(new JSMethod("escape", STRING, ARR_STR,
                new String[] { "str" }));
        params.add(new JSMethod("eval", VOID, ARR_STR, new String[] {"str"}));
        params.add(new JSMethod("isFinite", BOOL, new String[]{OBJECT},
                new String[] { "obj" }));
        params.add(new JSMethod("isNaN", BOOL, new String[]{OBJECT},
                new String[] { "obj" }));
        params.add(new JSMethod("Number", NUM, new String[]{OBJECT},
                new String[] { "obj" }));
        params.add(new JSMethod("parseFloat", NUM, ARR_STR,
                new String[] { "str" }));
        params.add(new JSMethod("parseInt", NUM, ARR_STR,
                new String[] { "str" }));
        params.add(new JSMethod("String", STRING, new String[]{OBJECT},
                new String[] { "obj" }));
        params.add(new JSMethod("unescape", STRING, ARR_STR,
                new String[] { "str" }));
        return params;
    }
    
    static List<JavaResultItem> this_params;
    static List<JavaResultItem> getThisParams()
    {
        if(this_params == null)
            this_params = this_(); 
        return this_params;
    }
    
    static JavaResultItem make_func(String name, NativeFunction s)
    {
        int n = s.getArity();
        String[] types = new String[n];
        String[] params = new String[n];
        for(int i = 0; i <n; i++){
            types[i] = OBJECT;
            params[i]= "arg" + i;
        }
        return new JSMethod(name, OBJECT, types, params);
    }
 
}
