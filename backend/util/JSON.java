package util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSON {

    public static HashMap<String, Object> parseJSON(String jsonString) {
        HashMap<String, Object> resultMap = new HashMap<>();
        parseJSONObject(jsonString, resultMap);
        return resultMap;
    }

    private static void parseJSONObject(String jsonString, Map<String, Object> resultMap) {
        Pattern pattern = Pattern.compile("\\s*\\\"([^\\\"]*)\\\":\\s*(\\\"[^\\\"]*\\\"|\\[[^\\]]*\\]|\\{[^\\}]*\\}|[0-9]*|true|false)", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(jsonString);

        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);

            if (value.startsWith("{")) {
                Map<String, Object> innerMap = new HashMap<>();
                parseJSONObject(value, innerMap);

                resultMap.put(key, innerMap);
            } else if (value.startsWith("\"")) {
                resultMap.put(key, value.substring(1, value.length() - 1));
            } else if ("true".equals(value) || "false".equals(value)) {
                resultMap.put(key, Boolean.parseBoolean(value));
            } else if ("null".equals(value)) {
                resultMap.put(key, null);
            } else {
                resultMap.put(key, Long.parseLong(value));
            }
        }
    }

    public static String createJSON(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sb.append("\"" + entry.getKey() + "\":");
            if (entry.getValue() instanceof Map) {
                if (((Map) entry.getValue()).isEmpty()) {
                    sb.append("{}");
                } else sb.append(createJSON((Map<String, Object>) entry.getValue()));
            } else if (entry.getValue() instanceof String) {
                sb.append("\"" + entry.getValue() + "\"");
            } else if (entry.getValue() instanceof Boolean) {
                sb.append(entry.getValue());
            } else if (entry.getValue() == null) {
                sb.append("null");
            } else if (entry.getValue().getClass().isArray()) {
                if (((Object[]) entry.getValue()).length == 0) {
                    sb.append("[]");
                } else {
                    sb.append("[");
                    for (Object o : (Object[]) entry.getValue()) {
                        if (o instanceof String) {
                            sb.append("\"" + o + "\"");
                        } else {
                            sb.append(o);
                        }
                        sb.append(",");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    sb.append("]");
                }
            } else {
                sb.append(entry.getValue());
            }
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("}");
        return sb.toString();
    }




}
