package net.dschinghiskahn.eveapigenerator.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.simpleframework.xml.Root;

import net.dschinghiskahn.eveapi.util.AbstractApiResponse;

public class ApiBean {

    private String className;
    private String packageName;
    private List<Field> fields = new ArrayList<Field>();
    private boolean isBase;

    public ApiBean(String className, String packageName, boolean isBase) {
        this.className = className;
        this.packageName = packageName;
        this.isBase = isBase;
    }

    public void addField(String fieldName, String fieldType) {
        addField(fieldName, fieldType, 0);
    }

    public void addField(String fieldName, String fieldType, int position) {
        fields.add(new Field(fieldName, fieldType, isBase, position));
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append(String.format("package %s;\n\n", packageName));

        Set<Import> imports = new HashSet<Import>();
        for (Field field : fields) {
            imports.addAll(field.getImports());
        }
        imports.add(new Import(Root.class));
        if (isBase) {
            imports.add(new Import(AbstractApiResponse.class));
        }
        appendImports(result, imports);

        if (isBase) {
            result.append("@Root(name = \"eveapi\")\n");
            result.append(String.format("public class %s extends AbstractApiResponse {\n\n", className));
        } else {
            result.append("@Root(name = \"row\")\n");
            result.append(String.format("public class %s {\n\n", className));
        }

        Collections.sort(fields);
        appendFields(result, fields);
        appendGetters(result, fields);
        appendToString(result, fields);

        result.append(String.format("}\n"));

        return result.toString();
    }

    private void appendToString(StringBuilder result, List<Field> fields) {
        result.append(String.format("    @Override\n"));
        result.append(String.format("    public String toString(){\n"));
        result.append(String.format("        return \"%s [\" +\n", className));
        Collections.sort(fields);
        for (Field field : fields) {
            result.append(String.format("            \"%s = \" + %s + \", \" +\n", field.getVariableName(), field.getVariableName()));
        }
        result.append(String.format("            \"]\";\n"));
        result.append(String.format("    }\n\n"));
    }

    private void appendGetters(StringBuilder result, List<Field> fields) {
        if (!fields.isEmpty()) {
            for (Field field : fields) {
                result.append(field.getGetterMethod());
                result.append("\n");
            }
        }
    }

    private void appendFields(StringBuilder result, List<Field> fields) {
        if (!fields.isEmpty()) {
            for (Field field : fields) {
                result.append(field.getVariableDefinition());
                result.append("\n");
            }
        }
    }

    private void appendImports(StringBuilder result, Set<Import> imports) {
        if (!imports.isEmpty()) {
            List<Import> list = new ArrayList<Import>(imports);
            Collections.sort(list);
            for (int i = 0; i < list.size(); i++) {
                if (i > 0 && !list.get(i).getFirstPathSegment().equals(list.get(i - 1).getFirstPathSegment())) {
                    result.append(String.format("\n"));
                }
                result.append(list.get(i).getImport());
            }
            result.append(String.format("\n"));
        }
    }
}
