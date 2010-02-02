package seco.langs.javascript;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.NativeFunction;

import seco.langs.javascript.JSCompletionProvider.JSProperty;
import seco.langs.javascript.JSCompletionProvider.JSVarArgMethod;
import seco.notebook.syntax.java.JavaResultItem;

public class BuiltIns
{
    public static final String DATE = "Date";
    public static final String REG_EXP = "RegExp";
    public static final String BOOL = "Boolean";
    public static final String OBJECT = "Object";
    public static final String ARRAY = "Array";
    public static final String NUM = "Number";
    public static final String STRING = "String";
    public static final String MATH = "Math";

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
    private static final JavaResultItem.MethodItem PROP_VALUE_OF = new JavaResultItem.MethodItem(
            "valueOf", OBJECT, Modifier.PUBLIC);
    private static final JavaResultItem.MethodItem PROP_TO_STRING = new JavaResultItem.MethodItem(
            "toString", STRING, Modifier.PUBLIC);

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
            objectsMap.put(MATH, math());
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
        params.add(new JavaResultItem.MethodItem("toTimeString", STRING,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("toDateString", STRING,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("toLocaleString", STRING,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("toLocaleTimeString", STRING,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("toLocaleDateString", STRING,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("toUTCString",
                "toLocaleTimeString", Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("toSource", STRING,
                Modifier.PUBLIC));
        params.add(PROP_VALUE_OF);
        params.add(new JavaResultItem.MethodItem("getTime", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("getYear", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("getFullYear", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("getUTCFullYear", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("getMonth", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("getUTCMonth", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("getDate", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("getUTCDate", NUM,
                Modifier.PUBLIC));
        params
                .add(new JavaResultItem.MethodItem("getDay", NUM,
                        Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("getUTCDay", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("getHours", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("getUTCHours", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("getMinutes", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("getUTCMinutes", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("getSeconds", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("getUTCSeconds", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("getMilliseconds", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("getUTCMilliseconds", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("getTimezoneOffset", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("setTime", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("setMilliseconds", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("setUTCMilliseconds", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("setSeconds", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("setUTCSeconds", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("setMinutes", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("setUTCMinutes", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("setHours", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("setUTCHours", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("setDate", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("setUTCDate", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("setMonth", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("setUTCMonth", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("setFullYear", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("setUTCFullYear", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("setYear", NUM,
                Modifier.PUBLIC));
        params.add(new JavaResultItem.MethodItem("parse", NUM, ARR_STR,
                new String[] { "dateString" }, Modifier.STATIC));
        params.add(new JavaResultItem.MethodItem("UTC", NUM, new String[] {
                NUM, NUM, NUM, NUM, NUM }, new String[] { "year", "month",
                "date", "hrs", "min", "sec", "ms" }, Modifier.STATIC));
        return params;
    }

    private static List<JavaResultItem> string()
    {
        List<JavaResultItem> params = new ArrayList<JavaResultItem>();
        params.add(new JavaResultItem.MethodItem("anchor", STRING, ARR_STR,
                new String[] { "name" }));
        params.add(new JavaResultItem.MethodItem("big", STRING));
        params.add(new JavaResultItem.MethodItem("blink", STRING));
        params.add(new JavaResultItem.MethodItem("bold", STRING));
        params.add(new JavaResultItem.MethodItem("charAt", NUM, ARR_NUM,
                new String[] { "index" }));
        params.add(new JavaResultItem.MethodItem("charCodeAt", NUM, ARR_NUM,
                new String[] { "index" }));
        params.add(new JavaResultItem.MethodItem("concat", STRING, ARR_STR,
                new String[] { "string2" }));
        params.add(new JavaResultItem.MethodItem("fixed", STRING));
        params.add(new JavaResultItem.MethodItem("fontcolor", STRING, ARR_STR,
                new String[] { "color" }));
        params.add(new JavaResultItem.MethodItem("fontsize", STRING, ARR_STR,
                new String[] { "size" }));
        params.add(new JavaResultItem.MethodItem("fromCharCode", STRING));
        params.add(new JavaResultItem.MethodItem("indexOf", NUM, new String[] {
                STRING, NUM }, new String[] { "searchString",
                "startPositionIndex" }));
        params.add(new JavaResultItem.MethodItem("italics", STRING));
        params.add(new JavaResultItem.MethodItem("lastIndexOf", NUM,
                new String[] { STRING, NUM }, new String[] { "searchString",
                        "startPositionIndex" }));
        params.add(new JavaResultItem.MethodItem("link", STRING, ARR_STR,
                new String[] { "url" }));
        params.add(new JavaResultItem.MethodItem("slice", STRING, new String[] {
                NUM, NUM }, new String[] { "startPositionIndex",
                "endPositionIndex" }));
        params.add(new JavaResultItem.MethodItem("small", STRING));
        params.add(new JavaResultItem.MethodItem("split", ARRAY, ARR_STR,
                new String[] { "delimiter" }));
        params.add(new JavaResultItem.MethodItem("strike", STRING));
        params.add(new JavaResultItem.MethodItem("sub", STRING));
        params.add(new JavaResultItem.MethodItem("substr", STRING,
                new String[] { NUM, NUM }, new String[] { "startPositionIndex",
                        "length" }));
        params.add(new JavaResultItem.MethodItem("substring", STRING,
                new String[] { NUM, NUM }, new String[] { "startPositionIndex",
                        "endPositionIndex" }));
        params.add(new JavaResultItem.MethodItem("sup", STRING));
        params.add(new JavaResultItem.MethodItem("toLowerCase", STRING));
        params.add(new JavaResultItem.MethodItem("toUpperCase", STRING));
        return params;
    }

    private static List<JavaResultItem> array()
    {
        List<JavaResultItem> params = new ArrayList<JavaResultItem>();
        params.add(PROP_CTR);
        params.add(PROP_LENGHT);
        params.add(PROP_PROTO);
        params.add(new JavaResultItem.MethodItem("concat", ARRAY,
                new String[] { ARRAY }, new String[] { "array2" }));
        params.add(new JavaResultItem.MethodItem("join", OBJECT, ARR_STR,
                new String[] { "delimiterString" }));
        params.add(new JavaResultItem.MethodItem("pop", OBJECT));
        params.add(new JavaResultItem.MethodItem("push", OBJECT,
                new String[] { OBJECT }, new String[] { "value" }));
        params.add(new JavaResultItem.MethodItem("reverse", ARRAY));
        params.add(new JavaResultItem.MethodItem("shift", OBJECT));
        params.add(new JavaResultItem.MethodItem("slice", ARRAY, new String[] {
                NUM, NUM }, new String[] { "startIndex", "endIndex" }));
        params.add(new JavaResultItem.MethodItem("sort", ARRAY,
                new String[] { OBJECT }, new String[] { "compareFunction" }));
        params.add(new JavaResultItem.MethodItem("unshift", NUM,
                new String[] { OBJECT }, new String[] { "value" }));

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

        params.add(new JavaResultItem.MethodItem("toExponential", STRING,
                ARR_NUM, new String[] { "x" }));
        params.add(new JavaResultItem.MethodItem("toFixed", STRING, ARR_NUM,
                new String[] { "x" }));
        params.add(new JavaResultItem.MethodItem("toPrecision", STRING,
                ARR_NUM, new String[] { "x" }));

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

        params.add(new JavaResultItem.MethodItem("compile", VOID));
        params.add(new JavaResultItem.MethodItem("exec", VOID));
        params.add(new JavaResultItem.MethodItem("test", BOOL));

        return params;
    }

    private static List<JavaResultItem> object()
    {

        List<JavaResultItem> params = new ArrayList<JavaResultItem>();
        params.add(PROP_CTR);
        params.add(PROP_PROTO);

        params.add(new JavaResultItem.MethodItem("hasOwnProperty", BOOL,
                ARR_STR, new String[] { "prop" }));
        params.add(new JavaResultItem.MethodItem("isPrototypeOf", BOOL,
                new String[] { OBJECT }, new String[] { "object" }));
        params.add(new JavaResultItem.MethodItem("propertyIsEnumerable", BOOL,
                ARR_STR, new String[] { "prop" }));
        params.add(new JavaResultItem.MethodItem("toLocaleString", STRING));
        params.add(new JavaResultItem.MethodItem("toSource", STRING));
        params.add(new JavaResultItem.MethodItem("unwatch", VOID, ARR_STR,
                new String[] { "prop" }));
        params.add(new JavaResultItem.MethodItem("watch", VOID, new String[] {
                STRING, OBJECT }, new String[] { "prop", "handler" }));
        params.add(PROP_TO_STRING);
        params.add(PROP_VALUE_OF);

        return params;
    }

    private static List<JavaResultItem> math()
    {
        List<JavaResultItem> params = new ArrayList<JavaResultItem>();
        params.add(new JSProperty("E", NUM, Modifier.STATIC));
        params.add(new JSProperty("LN2", NUM, Modifier.STATIC));
        params.add(new JSProperty("LN10", NUM, Modifier.STATIC));
        params.add(new JSProperty("LOG2E", NUM, Modifier.STATIC));
        params.add(new JSProperty("LOG10E", NUM, Modifier.STATIC));
        params.add(new JSProperty("PI", NUM, Modifier.STATIC));
        params.add(new JSProperty("SQRT1_2", NUM, Modifier.STATIC));
        params.add(new JSProperty("SQRT2", NUM, Modifier.STATIC));

        params.add(new JavaResultItem.MethodItem("abs", NUM, ARR_NUM,
                new String[] { "x" }, Modifier.STATIC));
        params.add(new JavaResultItem.MethodItem("acos", NUM, ARR_NUM,
                new String[] { "x" }, Modifier.STATIC));
        params.add(new JavaResultItem.MethodItem("asin", NUM, ARR_NUM,
                new String[] { "x" }, Modifier.STATIC));
        params.add(new JavaResultItem.MethodItem("atan", NUM, ARR_NUM,
                new String[] { "x" }, Modifier.STATIC));
        params.add(new JavaResultItem.MethodItem("atan2", NUM, new String[] {
                NUM, NUM }, new String[] { "y", "x" }, Modifier.STATIC));
        params.add(new JavaResultItem.MethodItem("ceil", NUM, ARR_NUM,
                new String[] { "x" }, Modifier.STATIC));
        params.add(new JavaResultItem.MethodItem("cos", NUM, ARR_NUM,
                new String[] { "x" }, Modifier.STATIC));
        params.add(new JavaResultItem.MethodItem("exp", NUM, ARR_NUM,
                new String[] { "x" }, Modifier.STATIC));
        params.add(new JavaResultItem.MethodItem("floor", NUM, ARR_NUM,
                new String[] { "x" }, Modifier.STATIC));
        params.add(new JavaResultItem.MethodItem("log", NUM, ARR_NUM,
                new String[] { "x" }, Modifier.STATIC));

        params.add(new JSVarArgMethod("max", NUM, ARR_NUM,
                new String[] { "x" }, Modifier.STATIC));
        params.add(new JSVarArgMethod("min", NUM, ARR_NUM,
                new String[] { "x" }, Modifier.STATIC));
        params.add(new JavaResultItem.MethodItem("pow", NUM, new String[] {
                NUM, NUM }, new String[] { "x", "y" }, Modifier.STATIC));
        params
                .add(new JavaResultItem.MethodItem("random", NUM,
                        Modifier.STATIC));
        params.add(new JavaResultItem.MethodItem("round", NUM, ARR_NUM,
                new String[] { "x" }, Modifier.STATIC));
        params.add(new JavaResultItem.MethodItem("sin", NUM, ARR_NUM,
                new String[] { "x" }, Modifier.STATIC));
        params.add(new JavaResultItem.MethodItem("sqrt", NUM, ARR_NUM,
                new String[] { "x" }, Modifier.STATIC));
        params.add(new JavaResultItem.MethodItem("tan", NUM, ARR_NUM,
                new String[] { "x" }, Modifier.STATIC));

        return params;
    }

    private static List<JavaResultItem> this_()
    {
        List<JavaResultItem> params = new ArrayList<JavaResultItem>();

        params.add(new JSProperty("Infinity", NUM));
        params.add(new JSProperty("NaN", NUM));
        params.add(new JSProperty("undefined", OBJECT));

        params.add(new JavaResultItem.MethodItem("decodeURI", STRING, ARR_STR,
                new String[] { "str" }));
        params.add(new JavaResultItem.MethodItem("decodeURIComponent", STRING,
                ARR_STR, new String[] { "str" }));
        params.add(new JavaResultItem.MethodItem("encodeURI", STRING, ARR_STR,
                new String[] { "str" }));
        params.add(new JavaResultItem.MethodItem("encodeURIComponent", STRING,
                ARR_STR, new String[] { "str" }));
        params.add(new JavaResultItem.MethodItem("escape", STRING, ARR_STR,
                new String[] { "str" }));
        params.add(new JavaResultItem.MethodItem("eval", VOID, ARR_STR,
                new String[] { "str" }));
        params.add(new JavaResultItem.MethodItem("isFinite", BOOL,
                new String[] { OBJECT }, new String[] { "obj" }));
        params.add(new JavaResultItem.MethodItem("isNaN", BOOL,
                new String[] { OBJECT }, new String[] { "obj" }));
        params.add(new JavaResultItem.MethodItem("Number", NUM,
                new String[] { OBJECT }, new String[] { "obj" }));
        params.add(new JavaResultItem.MethodItem("parseFloat", NUM, ARR_STR,
                new String[] { "str" }));
        params.add(new JavaResultItem.MethodItem("parseInt", NUM, ARR_STR,
                new String[] { "str" }));
        params.add(new JavaResultItem.MethodItem("String", STRING,
                new String[] { OBJECT }, new String[] { "obj" }));
        params.add(new JavaResultItem.MethodItem("unescape", STRING, ARR_STR,
                new String[] { "str" }));
        return params;
    }

    static List<JavaResultItem> this_params;

    static List<JavaResultItem> getThisParams()
    {
        if (this_params == null) this_params = this_();
        return this_params;
    }

    static JavaResultItem make_func(String name, NativeFunction s)
    {
        int n = s.getArity();
        String[] types = new String[n];
        String[] params = new String[n];
        for (int i = 0; i < n; i++)
        {
            types[i] = OBJECT;
            params[i] = "arg" + i;
        }
        return new JavaResultItem.MethodItem(name, OBJECT, types, params);
    }

}
