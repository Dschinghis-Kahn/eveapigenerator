package net.dschinghiskahn.eveapigenerator.bean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;

public class Field implements Comparable<Field> {

    private String apiName;
    private String classSimpleName;
    private String variableName;
    private int position;
    private boolean isAttribute;
    private boolean isBase;
    private String className;
    private boolean isList;

    public Field(final String fieldName, final String className, final boolean isBase, final int position) {
        if (fieldName.contains("<")) {
            apiName = fieldName.substring(0, fieldName.indexOf('<'));
        } else if (fieldName.contains("*")) {
            apiName = fieldName.substring(0, fieldName.indexOf('*'));
        } else {
            apiName = fieldName;
        }
        variableName = applyJavaNamingConvention(apiName, true);

        isList = className.equals("java.util.List");
        if (isList) {
            if (fieldName.contains("<")) {
                classSimpleName = applyJavaNamingConvention(fieldName.substring(fieldName.indexOf('<') + 1, fieldName.indexOf('>')), false);
            } else {
                classSimpleName = applyJavaNamingConvention(apiName, false);
            }
            if (classSimpleName.toLowerCase(Locale.getDefault()).endsWith("list")) {
                classSimpleName = classSimpleName.substring(0, classSimpleName.length() - 4);
            } else if (classSimpleName.toLowerCase(Locale.getDefault()).endsWith("queue")) {
                classSimpleName = classSimpleName.substring(0, classSimpleName.length() - 5);
            }
            if (classSimpleName.endsWith("s")) {
                classSimpleName = classSimpleName.substring(0, classSimpleName.length() - 1);
                if (classSimpleName.endsWith("ie")) {
                    classSimpleName = classSimpleName.substring(0, classSimpleName.length() - 2) + "y";
                }
            }
            this.className = "net.dschinghiskahn.eveapi.xxx." + classSimpleName;
        } else {
            classSimpleName = applyJavaNamingConvention(className.replaceAll(".*\\.", ""), false);
            this.className = className.toLowerCase(Locale.getDefault()).replaceAll(classSimpleName.toLowerCase(Locale.getDefault()), classSimpleName);
        }

        isAttribute = !fieldName.endsWith("*");
        this.position = position;
        this.isBase = isBase;
    }

    private String applyJavaNamingConvention(String baseName, boolean isVariable) {
        StringBuilder result = new StringBuilder();
        if (isVariable) {
            result.append(Character.toLowerCase(baseName.charAt(0)));
        } else {
            result.append(Character.toUpperCase(baseName.charAt(0)));
        }
        for (int i = 1; i < baseName.length() - 1; i++) {
            if (Character.isUpperCase(baseName.charAt(i - 1)) && Character.isUpperCase(baseName.charAt(i))
                    && (Character.isUpperCase(baseName.charAt(i + 1)) || Character.isDigit(baseName.charAt(i + 1)))) {
                result.append(Character.toLowerCase(baseName.charAt(i)));
            } else {
                result.append(baseName.charAt(i));
            }
        }
        result.append(Character.toLowerCase(baseName.charAt(baseName.length() - 1)));
        return result.toString();
    }

    @Override
    public int compareTo(final Field other) {
        return apiName.compareTo(other.apiName);
    }

    @Override
    public int hashCode() {
        return apiName.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Field other = (Field) obj;
        if (apiName == null) {
            if (other.apiName != null) {
                return false;
            }
        } else if (!apiName.equals(other.apiName)) {
            return false;
        }
        return true;
    }

    public String getVariableName() {
        return variableName;
    }

    public Set<Import> getImports() {
        Set<Import> result = new HashSet<>();

        if (isList) {
            result.add(new Import(List.class));
            result.add(new Import(ArrayList.class));
            result.add(new Import(ElementList.class));
            result.add(new Import(Path.class));
            result.add(new Import(Attribute.class));
        } else {
            if (isAttribute) {
                result.add(new Import(Attribute.class));
            } else {
                if (isBase) {
                    result.add(new Import(Path.class));
                }
                result.add(new Import(Element.class));
            }
        }
        if (className.startsWith("net.dschinghiskahn.")) {
            result.add(new Import(Path.class));
        } else if (!className.startsWith("java.lang")) {
            result.add(new Import(className));
        }

        return result;
    }

    public String getGetterMethod() {
        StringBuilder result = new StringBuilder();

        if (isList) {
            result.append(String.format("    public List<%s> get%s() {\n", classSimpleName,
                    variableName.substring(0, 1).toUpperCase(Locale.getDefault()) + variableName.substring(1)));
        } else {
            result.append(String.format("    public %s get%s() {\n", classSimpleName,
                    variableName.substring(0, 1).toUpperCase(Locale.getDefault()) + variableName.substring(1)));
        }
        result.append(String.format("        return %s;\n", variableName));
        result.append(String.format("    }\n"));

        return result.toString();
    }

    public String getVariableDefinition() {
        StringBuilder result = new StringBuilder();

        if (isList) {
            String path;
            if (isBase) {
                path = "result/rowset";
            } else {
                path = "rowset";
            }
            result.append(String.format("    @Path(\"%s[%d]\")\n", path, position));
            result.append(String.format("    @Attribute(name = \"name\", required = false)\n"));
            result.append(String.format("    private String rowsetName%d;\n\n", position));
            result.append(String.format("    @Path(\"%s[%d]\")\n", path, position));
            result.append(String.format("    @Attribute(name = \"key\", required = false)\n"));
            result.append(String.format("    private String rowsetKey%d;\n\n", position));
            result.append(String.format("    @Path(\"%s[%d]\")\n", path, position));
            result.append(String.format("    @Attribute(name = \"columns\", required = false)\n"));
            result.append(String.format("    private String rowsetColumns%d;\n\n", position));
            result.append(String.format("    @Path(\"%s[%d]\")\n", path, position));
            result.append(String.format("    @ElementList(type = %s.class, required = false, inline = true)\n", classSimpleName));
            result.append(String.format("    private List<%s> %s = new ArrayList<>();\n", classSimpleName, variableName));
        } else {
            if (apiName.equals(variableName)) {
                if (isAttribute) {
                    result.append(String.format("    @Attribute(required = false)\n"));
                } else {
                    if (isBase) {
                        result.append(String.format("    @Path(\"result\")\n"));
                    }
                    result.append(String.format("    @Element(required = false)\n"));
                }
            } else {
                if (isAttribute) {
                    result.append(String.format("    @Attribute(name = \"%s\", required = false)\n", apiName));
                } else {
                    if (isBase) {
                        result.append(String.format("    @Path(\"result\")\n"));
                    }
                    result.append(String.format("    @Element(name = \"%s\", required = false)\n", apiName));
                }
            }
            result.append(String.format("    private %s %s;\n", classSimpleName, variableName));
        }

        return result.toString();
    }
}
