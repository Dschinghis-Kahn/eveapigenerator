package net.dschinghiskahn.eveapigenerator.bean;

public class Import implements Comparable<Import> {

    private String importName;

    public Import(Class<?> importType) {
        this.importName = importType.getName();
    }

    public Import(String importName) {
        this.importName = importName;
    }

    public String getImport() {
        return String.format("import %s;\n", importName);
    }

    public String getFirstPathSegment() {
        return importName.replaceAll("\\..*", "");
    }

    @Override
    public int compareTo(Import o) {
        return importName.compareTo(o.importName);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((importName == null) ? 0 : importName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Import other = (Import) obj;
        if (importName == null) {
            if (other.importName != null) return false;
        } else if (!importName.equals(other.importName)) return false;
        return true;
    }

}
