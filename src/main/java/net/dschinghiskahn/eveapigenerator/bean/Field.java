package net.dschinghiskahn.eveapigenerator.bean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;

public class Field implements Comparable<Field> {

    private String name;
    private String nameSingular;
    private int position;
    private boolean isAttribute;
    private boolean isId;
    private boolean isBase;
    private String className;
    private boolean isLocalClass;

    public Field(String name, Class<?> type, boolean isBase, int posistion) {
        this.name = name;
        isAttribute = !this.name.endsWith("*");
        if (!isAttribute) {
            this.name = this.name.substring(0, this.name.length() - 1);
        }
        if (type == null) {
            try {
                type = Class.forName(this.name);
                className = type.getName();
                isLocalClass = type.getName().startsWith("net.dschinghiskahn.");
            } catch (ClassNotFoundException e) {
                className = this.name.replaceAll(this.name.replaceAll(".*\\.", ""),
                        this.name.replaceAll(".*\\.", "").substring(0, 1).toUpperCase() + this.name.replaceAll(".*\\.", "").substring(1));
                isLocalClass = true;
            }
            this.name = this.name.replaceAll(".*\\.", "");
            this.name = this.name.substring(0, 1).toLowerCase() + this.name.substring(1);
        } else {
            className = type.getName();
            isLocalClass = type.getName().startsWith("net.dschinghiskahn.");
        }
        if (name.contains("<")) {
            this.nameSingular = this.name.substring(this.name.indexOf('<') + 1, this.name.indexOf('>'));
            this.name = this.name.substring(0, this.name.indexOf('<'));
        }
        if (nameSingular == null) {
            if (name.endsWith("s")) {
                nameSingular = this.name.substring(0, this.name.length() - 1);
                if (nameSingular.endsWith("ie")) {
                    nameSingular = nameSingular.substring(0, nameSingular.length() - 2) + "y";
                }
            } else {
                if (name.endsWith("List")) {
                    nameSingular = this.name.substring(0, this.name.length() - 4);
                } else {
                    nameSingular = this.name;
                }
            }
        }
        this.position = posistion;
        this.isId = name.contains("ID");
        this.isBase = isBase;
    }

    public String getVariableName() {
        return name.replaceAll("ID", "Id");
    }

    public String getGetterName() {
        return "get" + getVariableName().substring(0, 1).toUpperCase() + getVariableName().substring(1);
    }

    public String getType() {
        return className;
    }

    public String getSimpleType() {
        return className.replaceAll(".*\\.", "");
    }

    @Override
    public int compareTo(Field o) {
        return name.compareTo(o.name);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Field other = (Field) obj;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        return true;
    }

    public String getOriginalApiName() {
        return name;
    }

    public boolean isList() {
        return "java.util.List".equals(className);
    }

    public String getListType() {
        return nameSingular.substring(0, 1).toUpperCase() + nameSingular.substring(1, nameSingular.length());
    }

    public int getPosition() {
        return position;
    }

    public boolean isAttribute() {
        return isAttribute;
    }

    public Set<Import> getNeededImports() {
        Set<Import> result = new HashSet<Import>();

        if (isList()) {
            result.add(new Import(List.class));
            result.add(new Import(ArrayList.class));
            result.add(new Import(ElementList.class));
            result.add(new Import(Path.class));
            result.add(new Import(Attribute.class));
        } else {
            if (isAttribute()) {
                result.add(new Import(Attribute.class));
            } else {
                if (isBase) {
                    result.add(new Import(Path.class));
                }
                result.add(new Import(Element.class));
            }
        }
        if (isLocalClass) {
            result.add(new Import(Path.class));
        } else {
            result.add(new Import(getType()));
        }

        return result;
    }

    public String getGetterMethod() {
        StringBuilder result = new StringBuilder();

        if ("java.util.List".equals(className)) {
            result.append(String.format("    public List<%s> %s(){\n", getListType(), getGetterName()));
        } else {
            result.append(String.format("    public %s %s(){\n", getSimpleType(), getGetterName()));
        }
        result.append(String.format("        return %s;\n", getVariableName()));
        result.append(String.format("    }\n"));

        return result.toString();
    }

    public String getVariableDefinition() {
        StringBuilder result = new StringBuilder();

        if (isList()) {
            String path = "rowset";
            if (isBase) {
                path = "result/rowset";
            }
            result.append(String.format("    @Path(\"%s[%d]\")\n", path, getPosition()));
            result.append(String.format("    @Attribute(name=\"name\", required = false)\n"));
            result.append(String.format("    private String rowsetName%d;\n\n", getPosition()));
            result.append(String.format("    @Path(\"%s[%d]\")\n", path, getPosition()));
            result.append(String.format("    @Attribute(name=\"key\", required = false)\n"));
            result.append(String.format("    private String rowsetKey%d;\n\n", getPosition()));
            result.append(String.format("    @Path(\"%s[%d]\")\n", path, getPosition()));
            result.append(String.format("    @Attribute(name=\"columns\", required = false)\n"));
            result.append(String.format("    private String rowsetColumns%d;\n\n", getPosition()));
            result.append(String.format("    @Path(\"%s[%d]\")\n", path, getPosition()));
            result.append(String.format("    @ElementList(type = %s.class, required = false, inline = true)\n", getListType()));
            result.append(String.format("    private List<%s> %s = new ArrayList<%s>();\n", getListType(), getVariableName(), getListType()));
        } else {
            if (isId) {
                if (isAttribute()) {
                    result.append(String.format("    @Attribute(name = \"%s\", required = false)\n", getOriginalApiName()));
                } else {
                    if (isBase) {
                        result.append(String.format("    @Path(\"result\")\n"));
                    }
                    result.append(String.format("    @Element(name = \"%s\", required = false)\n", getOriginalApiName()));
                }
                result.append(String.format("    private %s %s;\n", getSimpleType(), getVariableName()));
            } else {
                if (isAttribute()) {
                    result.append(String.format("    @Attribute(required = false)\n"));
                } else {
                    if (isBase) {
                        result.append(String.format("    @Path(\"result\")\n"));
                    }
                    result.append(String.format("    @Element(required = false)\n"));
                }
                result.append(String.format("    private %s %s;\n", getSimpleType(), getVariableName()));
            }
        }

        return result.toString();
    }
}
