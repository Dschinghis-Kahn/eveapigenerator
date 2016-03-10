# eveapigenerator
A generator for the eveapi project generating the java bean objects.

##Features
- Easy to use
- Generates java beans with all needed simple-xml annotations
- Contains a test class testing eveapi


##Example

###Structure
```
src/main/resources
+-- account
    +-- characters
        +-- Character.properties
        +-- Characters.properties
+-- api
+-- character
+-- eve
```
###Files

- Each file must be named as its class name
- Each file can contain up to 6 lines:
  - fieldLong representing normal numbers
  - fieldDouble representing decimal numbers
  - fieldString representing texts
  - fieldDate representing dates
  - fieldList representing lists
  - fieldObject representing special objects
- Each line must contains the field names separated by commas
- Each field must be named like the field in the REST API response.
- Field ending with * are handled as text/value fields (e.g. &lt;field&gt;value&lt;field/&gt;)
- Field not ending with * are handled as attribute fields (e.g. &lt;tag field="value"/&gt;)
- Fields of type List require a separate property defining the list elements
- List elements are named as the lists singular (fieldList=characters needs a Character.properties file)
- A lists singular can be customized with fieldList=listName<listEntry>

Characters.properties:
```properties
fieldLong=
fieldDouble=
fieldString=
fieldDate=
fieldList=characters
fieldObject=
```

Character.properties:
```properties
fieldLong=characterID,corporationID,allianceID,factionID
fieldDouble=
fieldString=name,corporationName,allianceName,factionName
fieldDate=
fieldList=
fieldObject=
```

