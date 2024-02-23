package com.citizens.mainframe.service;

import com.ibm.as400.access.AS400PackedDecimal;
import java.lang.reflect.Field;


import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import net.sf.JRecord.JRecordInterface1;
import net.sf.JRecord.Details.RecordDetail;
import net.sf.JRecord.External.base.ExternalConversion;
import net.sf.JRecord.def.IO.builders.ICobolIOBuilder;
import net.sf.JRecord.detailsBasic.IItemDetails;

@Component
public class JsonToEbcdic {
   
    public static final String BLANK = " ";
    public static final String ZERO = "0";
    public static final String OPEN_BRACE = "{";
    public static final String CLOSE_BRACE = "}";
   
    public static final int CHAR_TYPE = 0;
    public static final int MAX_SHORT_SIZE = 4;
    public static final int MAX_INT_SIZE = 9;
    public static final int V907_TYPE = 22;
    public static final int NUMB_TYPE = 25;
    public static final int COMP_TYPE = 142;
    public static final int SIGNED_NUMB_TYPE = 152;
    public static final int COMP_3_4_TYPE = 31;
    public static final int COMP_3_3_TYPE = 140;
    public static final int COMP_3_5_TYPE = 141;
   
    public static final String COMP_3_4 = "31";
    public static final String COMP_3_3 = "140";
    public static final String COMP_3_5 = "141";
   
    public static final String LATIN_1_CHARSET = "ISO-8859-1";
    public static final String EBCDIC_CHARSET = String.format("CP%s", "500");
   
    private final HashMap<String, String> cobolNameValueMap;
    private final List<String> leaveAsHexFieldnameList;
   
    //constructor
    public JsonToEbcdic(HashMap<String, String> cobolNameValueMap, List<String> leaveAsHexFieldnameList) {
        this.cobolNameValueMap = cobolNameValueMap;
        this.leaveAsHexFieldnameList = leaveAsHexFieldnameList;
    }
   
    /************************************
    * convert request to aminframe format (byte array)
     * @throws InterruptedException
    * **********************************/
   
    public byte[] request2mainframe(String copybook) throws InterruptedException {
        System.out.println("constcutor called");
        ArrayList<HashMap<String,String>> intermediate_map = copybookToIntermediate(copybook);
        System.out.println("request2mainframe()--> intermediate_map : "+ intermediate_map);
        byte[] fixedLengthOutput = getFixedLengthOutput(intermediate_map);
//        System.out.println("byte array is : "+ fixedLengthOutput);
//        printByteArray(fixedLengthOutput);
        showbytesToString(fixedLengthOutput);
        return fixedLengthOutput;
    }
   
    public void showbytesToString(byte[] arr) {
        String asciiData = new String(arr, Charset.forName(EBCDIC_CHARSET));
        System.out.println("length of response is : "+ arr.length);
        System.out.println("data from jargon is : "+ asciiData + "/////////////////////////////");
    }
   
    public void printByteArray(byte[] arr) {
        for(byte i : arr) {
            System.out.println(i);
        }
    }
   
    /*************************************************************
    *convert copybook to intermediate format (arraylist of hashmap)
    *************************************************************/
    public ArrayList<HashMap<String, String>> copybookToIntermediate(String copybookName)
    {
        System.out.println("copybookToIntermediate() called");
        try {
            ClassPathResource resource = new ClassPathResource(copybookName);
            InputStream inputStream = resource.getInputStream();
            ICobolIOBuilder iob = JRecordInterface1.COBOL.newIOBuilder(inputStream,copybookName);
            RecordDetail record = iob.getLayout().getRecord(0);
//            printObjectDetails(record);
            System.out.println("copybookToIntermediate() --> record : "+record);
            IItemDetails root = record.getCobolItems().get(0);
            System.out.println("copybookToIntermediate() --> root : "+root.toString());
//            printObjectDetails(root);
//            Thread.sleep(100000);
            return getIntermediateList(root,new ArrayList<>(), new HashMap<>());
        }
        catch(Exception e) {
            System.out.println("error occured at JsonToEbcdic-->copybookToIntermedaite()" + e.getMessage());
            return null;  
        }
    }
   
   
    public static void printObjectDetails(Object obj) {
        System.out.println("-----------------------------------------------------------");
        if (obj instanceof String) { // Direct handling for Strings
            System.out.println("Instance of String: " + obj);
        } else {
            Class<?> objClass = obj.getClass();
            System.out.println("Class Name: " + objClass.getName());
            Field[] fields = objClass.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true); // if fields are private
                try {
                    System.out.println(field.getName() + ": " + field.get(obj));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    System.out.println("Error getting field value: " + e.getMessage());
                }
            }
        }
        System.out.println("---------------------------------------------------------------");
    }

   
    /*******************************************************************************************
    *Recursive method to iterate through copybook tree and extract metadata for cobol variables
    *each symbol leaf value will be stored in hashmap
    *all hashmaps will go into arraylist
     * @throws InterruptedException
    *****************************************/
   
    public ArrayList<HashMap<String, String>> getIntermediateList(IItemDetails items, ArrayList<HashMap<String, String>> fields, HashMap<String, Integer> allNames) throws InterruptedException {
        if (items == null) {
            return null;
        }

        for (IItemDetails i : items.getChildItems()) {
            printObjectDetails(i);
            String fieldName = i.getFieldName();

            if (i.isLeaf()) {
                if (!i.isFieldRedefined()) {
                    if (i.isFieldRedefined()) {
                        fieldName = i.getRedefinesFieldName();
                    }

                    if (!allNames.containsKey(fieldName)) {
                        allNames.put(fieldName, 0);
                    } else {
                        int occurrence = allNames.get(fieldName) + 1;
                        allNames.put(fieldName, occurrence);
                        fieldName = String.format("%s (%d)", fieldName, occurrence);
                    }

                    HashMap<String, String> fieldData = getRecordHashMap(i, fieldName);
                    fields.add(fieldData);
                }
            } else {
                int occurs = i.getOccurs();
                if (occurs < 0) {
                    occurs = 1;
                }

                for (int j = 0; j < occurs; j++) {
                    // Corrected recursive call: pass 'i' instead of 'items'
                    getIntermediateList(i, fields, allNames);
                }
            }
        }
        System.out.println("fields are : "+ fields);
//        Thread.sleep(10000);
        return fields;
    }



   
    /***********************************
    *get cobol variable metadata as hashmap
    *****************************************/
    public HashMap<String, String> getRecordHashMap(IItemDetails item, String fieldName){
        HashMap<String, String> hm = new HashMap();
        String fieldValue = calculateFieldValue(fieldName, item.getValue(), item.getDisplayLength(),item.getStorageLength(), item.getType(), item.getPicture());
       
        hm.put("name", fieldName);
        hm.put("value", fieldValue);
        hm.put("storage_length", Integer.toString(item.getStorageLength()));
        hm.put("display_length", Integer.toString(item.getDisplayLength()));
        hm.put("type_id", Integer.toString(item.getType()));
        hm.put("pic", item.getPicture());
       
        return hm;
    }
    /*******************************************
     * calcualte value for cobol varibles lef-pad numbers with zeros right-pad char
     * types with spaces pass in default of zero/space for variables non-required
     * request fields
     ********************************************/
   
    public String calculateFieldValue(String fieldName, String fieldValue, int displayLength, int storageLength, int typeId, String pic) {
        StringBuilder valueToAdd = new StringBuilder();
       
        if(cobolNameValueMap.containsKey(fieldName)) {
            if(cobolNameValueMap.get(fieldName) == null) {
                String messageDetail = fieldName+" is mapped with null value. It cannot be null";
            }
            valueToAdd = new StringBuilder(cobolNameValueMap.get(fieldName));
            int valueInitialLen = valueToAdd.length();
           
            //ifnumeric type
            if(typeId == NUMB_TYPE || typeId == SIGNED_NUMB_TYPE || typeId == V907_TYPE) {
                for(int j=0; j<storageLength-valueInitialLen; j++) {
                    valueToAdd.insert(0, ZERO);
                }
            }
           
            else if(typeId == CHAR_TYPE) {
                    valueToAdd.append(BLANK.repeat(Math.max(0,storageLength- valueInitialLen)));
            }
        }
       
        //pass in default/blank values for non-request fields
        else {
            if(fieldValue != null)
                return fieldValue.replace("\"", "");
           
            ExternalConversion.getTypeAsString(0, 31);
            switch (typeId){
            case CHAR_TYPE: {
                valueToAdd.append(BLANK.repeat(Math.max(0, storageLength)));
                break;
            }
            case SIGNED_NUMB_TYPE:
            case NUMB_TYPE:
            case V907_TYPE:
                for(int j=0;j<storageLength;j++)
                    valueToAdd.insert(0, ZERO);
                break;
            default:
                valueToAdd = new StringBuilder(ZERO);
                break;
            }
           
        }
        return valueToAdd.toString();
       
    }
   
    /*******************************************
     * Get fixed length output as byte array
     *****************************************/
    public byte[] getFixedLengthOutput(ArrayList<HashMap<String, String>> intermediate_map) {
        try {
            StringBuilder fixedOutputEbcdic = new StringBuilder();
           
            for(HashMap<String, String> hm : intermediate_map) {
                int typeId = Integer.parseInt(hm.get("type_id"));
                String cobolvalue = hm.get("value");
                String pic = hm.get("picc");
                String fieldName = hm.get("name");
               
                switch (typeId) {
                    case SIGNED_NUMB_TYPE:
                    case NUMB_TYPE:
                    case CHAR_TYPE:
                    {
                        if(leaveAsHexFieldnameList.contains(fieldName)) {
                            StringBuilder builder = new StringBuilder();
                            for(int i=0; i<cobolvalue.length(); i+=2) {
                                String str = cobolvalue.substring(i,i+2);
                                builder.append((char) Integer.parseInt(str,16));
                            }
                            fixedOutputEbcdic.append(builder.toString());
                        } else {
                            fixedOutputEbcdic.append( convertFormat(cobolvalue, LATIN_1_CHARSET, EBCDIC_CHARSET) );
                        }
                        break;
                    }
                    case COMP_TYPE:
                        String bytesSizeStr = pic.substring(pic.indexOf("(")+1,pic.indexOf(")")).trim();
                        int bytesSize = Integer.parseInt(bytesSizeStr);
                       
                        if(bytesSize <= MAX_SHORT_SIZE) {
                            short value = Short.parseShort(cobolvalue);
                            fixedOutputEbcdic.append(new String(intToBytes(value)));
                        }
                        else if(bytesSize <= MAX_INT_SIZE) {
                            int value = Integer.parseInt(cobolvalue);
                            fixedOutputEbcdic.append(new String(intToBytes(value)));
                        }
                        else {
                            long value = Long.parseLong(cobolvalue);
                            fixedOutputEbcdic.append(new String(longToBytes(value)));
                        }
                        break;
                    case COMP_3_5_TYPE:
                    case COMP_3_4_TYPE:
                    case COMP_3_3_TYPE:
                        int disp = Integer.parseInt(hm.get("display_length"));
                        AS400PackedDecimal packedDecimal = new AS400PackedDecimal(disp, 0);
                        BigDecimal javaBigDecimal = new BigDecimal(cobolvalue);
                        byte[] packedBytes = packedDecimal.toBytes(javaBigDecimal);
                        fixedOutputEbcdic.append(new String(packedBytes, LATIN_1_CHARSET));
                        break;
                    default:
                        fixedOutputEbcdic.append(convertFormat(cobolvalue, LATIN_1_CHARSET, EBCDIC_CHARSET));
                }
            }
            return fixedOutputEbcdic.toString().getBytes(LATIN_1_CHARSET);
        } catch(Exception ex) {
            System.out.println("number format exception at JsonToEbcdic"+ex);
            return null;
        }
    }
   
    /*********************************
     * convert string to different format
     ******************************/
   
    public String convertFormat(String strToConvert, String in, String out) {
        try {
            Charset charset_in = Charset.forName(out);
            Charset charset_out = Charset.forName(in);
            CharsetDecoder decoder = charset_out.newDecoder();
            CharsetEncoder encoder = charset_in.newEncoder();
            CharBuffer uCharBuffer = CharBuffer.wrap(strToConvert);
            ByteBuffer bbuf = encoder.encode(uCharBuffer);
            CharBuffer cbuf = decoder.decode(bbuf);
            return cbuf.toString();
        }
        catch(CharacterCodingException ex) {
            System.out.println("character coding exception occur at JsonToEbcdic" + ex);
            return "";
        }
    }
   
    /***************************
     * convert short to byte array
     **************************/
   
    public byte[] shortToBytes(short x) {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.putShort(x);
        return buffer.array();
    }
   
    /*
     * convert int to byte array
     */
   
    public byte[] intToBytes(int x) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(x);
        return buffer.array();
    }
   
    /*
     * convert long to bytes array
     */
    public byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }
   
}

